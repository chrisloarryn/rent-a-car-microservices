package com.kodlamaio.commonpackage;

import com.kodlamaio.commonpackage.configuration.security.ApplicationSecurityProperties;
import com.kodlamaio.commonpackage.configuration.security.InternalApiKeyFeignConfiguration;
import com.kodlamaio.commonpackage.utils.dto.responses.PageResponse;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class CommonPackageSupportUnitTests
{
    @Test
    void pageResponseMapsSpringPageMetadata()
    {
        PageImpl<String> page = new PageImpl<>(
                List.of("a", "b"),
                PageRequest.of(1, 2, Sort.by(Sort.Order.desc("createdDate"), Sort.Order.asc("name"))),
                5);

        PageResponse<String> response = PageResponse.from(page);

        assertIterableEquals(List.of("a", "b"), response.getContent());
        assertEquals(1, response.getPage());
        assertEquals(2, response.getSize());
        assertEquals(5, response.getTotalElements());
        assertEquals(3, response.getTotalPages());
        assertEquals(List.of("createdDate,desc", "name,asc"), response.getSort());
    }

    @Test
    void internalApiKeyFeignInterceptorAddsConfiguredHeader() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException
    {
        ApplicationSecurityProperties properties = new ApplicationSecurityProperties();
        properties.setInternalApiKeyHeader("X-Test-Internal-Key");
        properties.setInternalApiKey("secret-value");
        Method beanMethod = InternalApiKeyFeignConfiguration.class
                .getDeclaredMethod("internalApiKeyRequestInterceptor", ApplicationSecurityProperties.class);
        beanMethod.setAccessible(true);
        RequestInterceptor interceptor = (RequestInterceptor) beanMethod.invoke(new InternalApiKeyFeignConfiguration(), properties);
        RequestTemplate template = new RequestTemplate();

        assertNotNull(interceptor);
        interceptor.apply(template);

        assertIterableEquals(List.of("secret-value"), template.headers().get("X-Test-Internal-Key"));
    }

    @Test
    void securityPropertiesSupportDefaultAndMutableUsers()
    {
        ApplicationSecurityProperties properties = new ApplicationSecurityProperties();
        ApplicationSecurityProperties.BasicUser basicUser = new ApplicationSecurityProperties.BasicUser();

        basicUser.setUsername("runtime-user");
        basicUser.setPassword("runtime-pass");
        basicUser.setRoles(List.of("user", "admin"));
        properties.getBasic().setUser(basicUser);

        assertEquals("http://localhost:3000", properties.getAllowedOrigins().getFirst());
        assertEquals("runtime-user", properties.getBasic().getUser().getUsername());
        assertEquals("runtime-pass", properties.getBasic().getUser().getPassword());
        assertIterableEquals(List.of("user", "admin"), properties.getBasic().getUser().getRoles());
    }
}
