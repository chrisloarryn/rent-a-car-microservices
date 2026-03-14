package com.kodlamaio.commonpackage;

import com.kodlamaio.commonpackage.configuration.exceptions.RestExceptionHandler;
import com.kodlamaio.commonpackage.configuration.kafka.producer.KafkaProducerConfig;
import com.kodlamaio.commonpackage.configuration.mappers.ModelMapperConfig;
import com.kodlamaio.commonpackage.configuration.security.ApplicationSecurityProperties;
import com.kodlamaio.commonpackage.configuration.security.SecurityConfig;
import com.kodlamaio.commonpackage.events.BaseEvent;
import com.kodlamaio.commonpackage.events.inventory.BrandDeletedEvent;
import com.kodlamaio.commonpackage.utils.annotations.NotFutureYearValidator;
import com.kodlamaio.commonpackage.utils.constants.ExceptionTypes;
import com.kodlamaio.commonpackage.utils.constants.InternalApiHeaders;
import com.kodlamaio.commonpackage.utils.constants.Paths;
import com.kodlamaio.commonpackage.utils.constants.Regex;
import com.kodlamaio.commonpackage.utils.constants.Roles;
import com.kodlamaio.commonpackage.utils.dto.requests.CreateRentalPaymentRequest;
import com.kodlamaio.commonpackage.utils.dto.responses.CarClientResponse;
import com.kodlamaio.commonpackage.utils.dto.responses.ClientResponse;
import com.kodlamaio.commonpackage.utils.dto.responses.PageResponse;
import com.kodlamaio.commonpackage.utils.dto.responses.SystemPingResponse;
import com.kodlamaio.commonpackage.utils.exceptions.BusinessException;
import com.kodlamaio.commonpackage.utils.kafka.producer.KafkaProducer;
import com.kodlamaio.commonpackage.utils.mappers.ModelMapperManager;
import com.kodlamaio.commonpackage.utils.results.ApiErrorResponse;
import com.kodlamaio.commonpackage.utils.results.ExceptionResult;
import com.kodlamaio.commonpackage.utils.security.KeycloakRoleConverter;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Path;
import jakarta.validation.Valid;
import jakarta.validation.ValidationException;
import jakarta.validation.constraints.NotBlank;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.mockito.MockedStatic;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.mock.http.MockHttpInputMessage;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindException;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.lang.reflect.Constructor;
import java.time.Year;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(
        classes = CommonPackageApplicationTests.SecurityTestApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.MOCK,
        properties = {
                "spring.cloud.config.enabled=false",
                "eureka.client.enabled=false"
        })
class CommonPackageApplicationTests
{
    @Autowired
    private CorsConfigurationSource corsConfigurationSource;

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    @org.junit.jupiter.api.BeforeEach
    void setUpMockMvc()
    {
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .addFilters(context.getBean("springSecurityFilterChain", jakarta.servlet.Filter.class))
                .build();
    }

    @Test
    void mainDelegatesToSpringApplication()
    {
        try (MockedStatic<SpringApplication> springApplication = mockStatic(SpringApplication.class)) {
            String[] args = {"--server.port=0"};

            CommonPackageApplication.main(args);

            springApplication.verify(() -> SpringApplication.run(CommonPackageApplication.class, args));
        }
    }

    @Test
    void securityAllowsTechnicalPingEndpoint() throws Exception
    {
        mockMvc.perform(get("/api/system/ping"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }

    @Test
    void securityProtectsOtherApiEndpoints() throws Exception
    {
        mockMvc.perform(get("/api/private"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void internalEndpointsRequireApiKey() throws Exception
    {
        mockMvc.perform(get("/api/internal/private"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.type").value(ExceptionTypes.Exception.Authorization));

        mockMvc.perform(get("/api/internal/private")
                        .header(InternalApiHeaders.ApiKey, "rent-a-car-internal-dev-key"))
                .andExpect(status().isOk());
    }

    @Test
    void validationErrorsUseSharedErrorContract() throws Exception
    {
        mockMvc.perform(authenticatedPost("/api/filters")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.type").value(ExceptionTypes.Exception.Validation))
                .andExpect(jsonPath("$.message").value("Request validation failed."))
                .andExpect(jsonPath("$.details.name").value("Name is required."));
    }

    @Test
    void invalidPathParametersUseSharedErrorContract() throws Exception
    {
        mockMvc.perform(authenticatedGet("/api/cars/check-car-available/not-a-uuid"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.type").value(ExceptionTypes.Exception.RequestFormat))
                .andExpect(jsonPath("$.message").value("Request parameter has an invalid format."));
    }

    @Test
    void businessExceptionsUseSharedErrorContract() throws Exception
    {
        mockMvc.perform(authenticatedGet("/api/filters/business"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.type").value(ExceptionTypes.Exception.Business))
                .andExpect(jsonPath("$.message").value("BUSINESS_ERROR"));
    }

    @Test
    void dataIntegrityViolationsUseSharedErrorContract() throws Exception
    {
        mockMvc.perform(authenticatedGet("/api/filters/conflict"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.type").value(ExceptionTypes.Exception.DataIntegrityViolation));
    }

    @Test
    void runtimeExceptionsUseSharedErrorContract() throws Exception
    {
        mockMvc.perform(authenticatedGet("/api/filters/runtime"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.type").value(ExceptionTypes.Exception.Runtime));
    }

    @Test
    void corsConfigurationSourceUsesExpectedOrigins()
    {
        CorsConfiguration configuration = corsConfigurationSource.getCorsConfiguration(new MockHttpServletRequest());

        assertNotNull(configuration);
        assertTrue(configuration.getAllowedOrigins().contains("http://localhost:3000"));
        assertTrue(configuration.getAllowedMethods().contains("GET"));
    }

    @Test
    void keycloakRoleConverterExtractsGrantedAuthorities()
    {
        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "none")
                .claim("realm_access", Map.of("roles", List.of("user", "admin")))
                .build();

        Collection<GrantedAuthority> authorities = new KeycloakRoleConverter().convert(jwt);

        assertEquals(2, authorities.size());
        assertTrue(authorities.stream().anyMatch(authority -> authority.getAuthority().equals("ROLE_user")));
        assertTrue(authorities.stream().anyMatch(authority -> authority.getAuthority().equals("ROLE_admin")));
    }

    @Test
    void notFutureYearValidatorAcceptsCurrentYearAndRejectsFutureYear()
    {
        NotFutureYearValidator validator = new NotFutureYearValidator();
        int currentYear = Year.now().getValue();

        assertTrue(validator.isValid(currentYear, null));
        assertFalse(validator.isValid(currentYear + 1, null));
        assertTrue(validator.isValid(null, null));
    }

    @Test
    void modelMapperManagerConfiguresResponseAndRequestStrategies()
    {
        ModelMapper mapper = new ModelMapper();
        ModelMapperManager manager = new ModelMapperManager(mapper);

        assertSame(mapper, manager.forResponse());
        assertEquals(MatchingStrategies.LOOSE, mapper.getConfiguration().getMatchingStrategy());
        assertSame(mapper, manager.forRequest());
        assertEquals(MatchingStrategies.STANDARD, mapper.getConfiguration().getMatchingStrategy());
    }

    @Test
    void modelMapperConfigCreatesExpectedBeans()
    {
        ModelMapperConfig config = new ModelMapperConfig();

        ModelMapper mapper = config.getModelMapper();
        Object service = config.getModelMapperService(mapper);

        assertNotNull(mapper);
        assertTrue(service instanceof ModelMapperManager);
    }

    @Test
    void kafkaProducerDelegatesToKafkaTemplate()
    {
        @SuppressWarnings("unchecked")
        KafkaTemplate<String, Object> template = mock(KafkaTemplate.class);
        KafkaProducer producer = new KafkaProducer(template);
        BrandDeletedEvent payload = new BrandDeletedEvent(UUID.randomUUID());

        producer.sendMessage(payload, "topic");

        verify(template).send(org.mockito.ArgumentMatchers.<Message<?>>argThat(message ->
                payload.equals(message.getPayload()) &&
                        "topic".equals(message.getHeaders().get(KafkaHeaders.TOPIC))));
    }

    @Test
    void kafkaProducerConfigCreatesProducer()
    {
        @SuppressWarnings("unchecked")
        KafkaTemplate<String, Object> template = mock(KafkaTemplate.class);
        KafkaProducerConfig config = new KafkaProducerConfig();

        KafkaProducer producer = ReflectionTestUtils.invokeMethod(config, "getKafkaProducer", template);

        assertNotNull(producer);
    }

    @Test
    void resultAndResponseTypesExposeTheirState()
    {
        ExceptionResult<String> exceptionResult = new ExceptionResult<>(ExceptionTypes.Exception.Business, "failure");
        ApiErrorResponse apiErrorResponse = new ApiErrorResponse(400, "Bad Request", ExceptionTypes.Exception.Validation, "Validation failed", Map.of("name", "required"), "/api/test");
        SystemPingResponse systemPingResponse = new SystemPingResponse("test-service");
        ClientResponse clientResponse = new ClientResponse(true, "ok");
        CarClientResponse carClientResponse = new CarClientResponse("Model S", "Brand", "12 ABC 123", 2024);
        PageResponse<String> pageResponse = new PageResponse<>(List.of("one"), 0, 20, 1, 1, true, true, List.of("id,asc"));
        ApplicationSecurityProperties securityProperties = new ApplicationSecurityProperties();
        CreateRentalPaymentRequest request = new CreateRentalPaymentRequest("1234567812345678", "John Doe", 2026, 8, "123", 10.0);
        BusinessException businessException = new BusinessException("boom");
        BrandDeletedEvent event = new BrandDeletedEvent(UUID.randomUUID());

        assertEquals(ExceptionTypes.Exception.Business, exceptionResult.getType());
        assertEquals("failure", exceptionResult.getMessage());
        assertEquals("Validation failed", apiErrorResponse.getMessage());
        assertEquals("test-service", systemPingResponse.getService());
        assertTrue(clientResponse.isSuccess());
        assertEquals("ok", clientResponse.getMessage());
        assertEquals("Model S", carClientResponse.getModelName());
        assertEquals("one", pageResponse.getContent().getFirst());
        assertEquals("rent-a-car-internal-dev-key", securityProperties.getInternalApiKey());
        assertEquals("1234567812345678", request.getCardNumber());
        assertEquals("boom", businessException.getMessage());
        assertNotNull(event.getEventId());
        assertTrue(event instanceof BaseEvent);
    }

    @Test
    void privateUtilityConstructorsCanBeInvokedReflectively() throws Exception
    {
        assertDoesNotThrow(() -> instantiate(CommonPackageApplication.class));
        assertDoesNotThrow(() -> instantiate(Paths.class));
        assertDoesNotThrow(() -> instantiate(Paths.Inventory.class));
        assertDoesNotThrow(() -> instantiate(Paths.Filter.class));
        assertDoesNotThrow(() -> instantiate(Paths.Rental.class));
        assertDoesNotThrow(() -> instantiate(Paths.Payment.class));
        assertDoesNotThrow(() -> instantiate(Paths.Maintenance.class));
        assertDoesNotThrow(() -> instantiate(Paths.Invoice.class));
        assertDoesNotThrow(() -> instantiate(Regex.class));
        assertDoesNotThrow(() -> instantiate(Roles.class));
        assertDoesNotThrow(() -> instantiate(InternalApiHeaders.class));
        assertDoesNotThrow(() -> instantiate(ExceptionTypes.class));
        assertDoesNotThrow(() -> instantiate(ExceptionTypes.Exception.class));
    }

    @Test
    void pathsConstantsExposeExpectedBasePackages()
    {
        assertEquals("com.kodlamaio.commonpackage.configuration", Paths.ConfigurationBasePackage);
        assertEquals("com.kodlamaio.inventoryservice", Paths.Inventory.ServiceBasePackage);
        assertEquals("com.kodlamaio.filterservice", Paths.Filter.ServiceBasePackage);
        assertEquals("com.kodlamaio.rentalservice", Paths.Rental.ServiceBasePackage);
        assertEquals("com.kodlamaio.paymentservice", Paths.Payment.ServiceBasePackage);
        assertEquals("com.kodlamaio.maintenanceservice", Paths.Maintenance.ServiceBasePackage);
        assertEquals("com.kodlamaio.invoiceservice", Paths.Invoice.ServiceBasePackage);
    }

    @Test
    void directExceptionHandlersCoverConstraintValidationAndRequestFormatScenarios()
    {
        RestExceptionHandler handler = new RestExceptionHandler();
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/test");
        @SuppressWarnings("unchecked")
        ConstraintViolation<Object> violation = mock(ConstraintViolation.class);
        Path propertyPath = mock(Path.class);

        when(violation.getPropertyPath()).thenReturn(propertyPath);
        when(propertyPath.toString()).thenReturn("payload.name");
        when(violation.getMessage()).thenReturn("must not be blank");

        ResponseEntity<ApiErrorResponse> constraintResponse = handler.handleConstraintViolationException(
                new ConstraintViolationException(Set.of(violation)),
                request);
        ResponseEntity<ApiErrorResponse> validationResponse = handler.handleValidationException(
                new ValidationException("invalid payload"),
                request);
        ResponseEntity<ApiErrorResponse> bindResponse = handler.handleValidationException(
                new BindException(new BeanPropertyBindingResult(new Object(), "payload")),
                request);
        ResponseEntity<ApiErrorResponse> unreadableResponse = handler.handleHttpMessageNotReadableException(
                new HttpMessageNotReadableException("Malformed JSON request", new MockHttpInputMessage(new byte[0])),
                request);
        ResponseEntity<ApiErrorResponse> typeMismatchResponse = handler.handleMethodArgumentTypeMismatchException(
                new MethodArgumentTypeMismatchException("abc", null, "id", null, null),
                request);

        assertEquals(400, constraintResponse.getStatusCode().value());
        assertEquals(ExceptionTypes.Exception.ConstraintViolation, constraintResponse.getBody().getType());
        assertEquals("Request validation failed.", constraintResponse.getBody().getMessage());
        assertEquals(400, validationResponse.getStatusCode().value());
        assertEquals(ExceptionTypes.Exception.Validation, validationResponse.getBody().getType());
        assertEquals(400, bindResponse.getStatusCode().value());
        assertEquals(ExceptionTypes.Exception.Validation, bindResponse.getBody().getType());
        assertEquals(400, unreadableResponse.getStatusCode().value());
        assertEquals(ExceptionTypes.Exception.RequestFormat, unreadableResponse.getBody().getType());
        assertEquals(400, typeMismatchResponse.getStatusCode().value());
        assertEquals(List.of("id must be a valid unknown."), typeMismatchResponse.getBody().getDetails());
    }

    private static void instantiate(Class<?> type) throws Exception
    {
        Constructor<?> constructor = type.getDeclaredConstructor();
        constructor.setAccessible(true);
        constructor.newInstance();
    }

    private static org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder authenticatedGet(String path)
    {
        return get(path).header("Authorization", "Bearer test-token");
    }

    private static org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder authenticatedPost(String path)
    {
        return post(path).header("Authorization", "Bearer test-token");
    }

    @SpringBootConfiguration
    @EnableAutoConfiguration
    @Import({SecurityConfig.class, RestExceptionHandler.class})
    static class SecurityTestApplication
    {
        @Bean
        JwtDecoder jwtDecoder()
        {
            return token -> Jwt.withTokenValue(token)
                    .header("alg", "none")
                    .claim("sub", "test-user")
                    .claim("realm_access", Map.of("roles", List.of("user")))
                    .build();
        }

        @RestController
        static class TestController
        {
            @GetMapping("/api/system/ping")
            SystemPingResponse ping()
            {
                return new SystemPingResponse("test-service");
            }

            @GetMapping("/api/private")
            String secure()
            {
                return "secure";
            }

            @GetMapping("/api/internal/private")
            String internal()
            {
                return "internal";
            }

            @PostMapping("/api/filters")
            String validate(@Valid @RequestBody ValidationPayload payload)
            {
                return payload.name();
            }

            @GetMapping("/api/filters/business")
            String business()
            {
                throw new BusinessException("BUSINESS_ERROR");
            }

            @GetMapping("/api/filters/conflict")
            String conflict()
            {
                throw new org.springframework.dao.DataIntegrityViolationException("duplicate");
            }

            @GetMapping("/api/filters/runtime")
            String runtime()
            {
                throw new RuntimeException("unexpected");
            }

            @GetMapping("/api/cars/check-car-available/{id}")
            String checkCarAvailable(@PathVariable UUID id)
            {
                return id.toString();
            }
        }
    }

    record ValidationPayload(@NotBlank(message = "Name is required.") String name) {
    }
}
