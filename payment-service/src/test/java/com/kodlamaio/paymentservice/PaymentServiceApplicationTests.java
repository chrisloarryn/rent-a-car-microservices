package com.kodlamaio.paymentservice;

import com.kodlamaio.commonpackage.utils.dto.requests.CreateRentalPaymentRequest;
import com.kodlamaio.commonpackage.utils.dto.responses.ClientResponse;
import com.kodlamaio.commonpackage.utils.exceptions.BusinessException;
import com.kodlamaio.commonpackage.utils.mappers.ModelMapperService;
import com.kodlamaio.paymentservice.adapters.FakePosServiceAdapter;
import com.kodlamaio.paymentservice.api.controllers.PaymentsController;
import com.kodlamaio.paymentservice.business.abstracts.PaymentService;
import com.kodlamaio.paymentservice.business.abstracts.PostService;
import com.kodlamaio.paymentservice.business.concretes.PaymentManager;
import com.kodlamaio.paymentservice.business.dto.requests.create.CreatePaymentRequest;
import com.kodlamaio.paymentservice.business.dto.requests.update.UpdatePaymentRequest;
import com.kodlamaio.paymentservice.business.dto.responses.create.CreatePaymentResponse;
import com.kodlamaio.paymentservice.business.dto.responses.get.GetAllPaymentsResponse;
import com.kodlamaio.paymentservice.business.dto.responses.get.GetPaymentResponse;
import com.kodlamaio.paymentservice.business.dto.responses.update.UpdatePaymentResponse;
import com.kodlamaio.paymentservice.business.rules.PaymentBusinessRules;
import com.kodlamaio.paymentservice.entities.Payment;
import com.kodlamaio.paymentservice.repository.PaymentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.boot.SpringApplication;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentServiceApplicationTests
{
    @Mock
    private PaymentService paymentService;
    @Mock
    private PaymentRepository repository;
    @Mock
    private ModelMapperService mapperService;
    @Mock
    private ModelMapper mapper;
    @Mock
    private PostService postService;
    @Mock
    private PaymentBusinessRules rules;

    @Test
    void mainDelegatesToSpringApplication()
    {
        try (MockedStatic<SpringApplication> springApplication = mockStatic(SpringApplication.class)) {
            String[] args = {"--server.port=0"};

            PaymentServiceApplication.main(args);

            springApplication.verify(() -> SpringApplication.run(PaymentServiceApplication.class, args));
        }
    }

    @Test
    void controllerDelegatesToService()
    {
        PaymentsController controller = new PaymentsController(paymentService);
        UUID id = UUID.randomUUID();
        CreatePaymentRequest createRequest = new CreatePaymentRequest("1234567812345678", "John Doe", 2026, 12, "123", 1000.0);
        UpdatePaymentRequest updateRequest = new UpdatePaymentRequest("1234567812345678", "John Doe", 2026, 12, "123", 500.0);
        CreateRentalPaymentRequest rentalRequest = new CreateRentalPaymentRequest("1234567812345678", "John Doe", 2026, 12, "123", 100.0);

        controller.getAll();
        controller.getById(id);
        controller.add(createRequest);
        controller.update(id, updateRequest);
        controller.delete(id);
        controller.processRentalPayment(rentalRequest);

        verify(paymentService).getAll();
        verify(paymentService).getById(id);
        verify(paymentService).add(createRequest);
        verify(paymentService).update(id, updateRequest);
        verify(paymentService).delete(id);
        verify(paymentService).processRentalPayment(rentalRequest);
    }

    @Test
    void managerCoversCrudAndPaymentProcessing()
    {
        PaymentManager manager = new PaymentManager(repository, mapperService, postService, rules);
        UUID id = UUID.randomUUID();
        Payment payment = new Payment();
        payment.setId(id);
        payment.setBalance(500.0);
        CreatePaymentRequest createRequest = new CreatePaymentRequest("1234567812345678", "John Doe", 2026, 12, "123", 500.0);
        UpdatePaymentRequest updateRequest = new UpdatePaymentRequest("1234567812345678", "John Doe", 2026, 12, "123", 250.0);
        CreateRentalPaymentRequest rentalRequest = new CreateRentalPaymentRequest("1234567812345678", "John Doe", 2026, 12, "123", 100.0);
        CreatePaymentResponse createResponse = new CreatePaymentResponse();
        UpdatePaymentResponse updateResponse = new UpdatePaymentResponse();
        GetPaymentResponse detailResponse = new GetPaymentResponse();
        GetAllPaymentsResponse listResponse = new GetAllPaymentsResponse();

        when(mapperService.forRequest()).thenReturn(mapper);
        when(mapperService.forResponse()).thenReturn(mapper);
        when(repository.findAll()).thenReturn(List.of(payment));
        when(repository.findById(id)).thenReturn(Optional.of(payment));
        when(repository.findByCardNumber(rentalRequest.getCardNumber())).thenReturn(payment);
        when(mapper.map(payment, GetAllPaymentsResponse.class)).thenReturn(listResponse);
        when(mapper.map(payment, GetPaymentResponse.class)).thenReturn(detailResponse);
        when(mapper.map(createRequest, Payment.class)).thenReturn(payment);
        when(mapper.map(updateRequest, Payment.class)).thenReturn(payment);
        when(mapper.map(payment, CreatePaymentResponse.class)).thenReturn(createResponse);
        when(mapper.map(payment, UpdatePaymentResponse.class)).thenReturn(updateResponse);

        assertSame(listResponse, manager.getAll().getFirst());
        assertSame(detailResponse, manager.getById(id));
        assertSame(createResponse, manager.add(createRequest));
        assertSame(updateResponse, manager.update(id, updateRequest));
        assertTrue(manager.processRentalPayment(rentalRequest).isSuccess());

        doThrow(new BusinessException("NOT_VALID_PAYMENT")).when(rules).checkIfPaymentIsValid(rentalRequest);
        ClientResponse failedResponse = manager.processRentalPayment(rentalRequest);
        assertFalse(failedResponse.isSuccess());
        assertEquals("NOT_VALID_PAYMENT", failedResponse.getMessage());

        manager.delete(id);

        verify(rules, atLeastOnce()).checkIfPaymentExists(id);
        verify(rules).checkIfCardExistsByCardNumber(createRequest);
        verify(postService).pay();
        verify(repository, atLeastOnce()).save(payment);
        verify(repository).deleteById(id);
    }

    @Test
    void businessRulesValidateRepositoryState()
    {
        PaymentBusinessRules businessRules = new PaymentBusinessRules(repository);
        UUID id = UUID.randomUUID();
        CreatePaymentRequest createRequest = new CreatePaymentRequest("1234567812345678", "John Doe", 2026, 12, "123", 500.0);
        CreateRentalPaymentRequest rentalRequest = new CreateRentalPaymentRequest("1234567812345678", "John Doe", 2026, 12, "123", 100.0);

        when(repository.existsById(id)).thenReturn(true, false);
        when(repository.existsByCardNumber(createRequest.getCardNumber())).thenReturn(false, true);
        when(repository.existsByCardNumberAndCardHolderAndCardExpirationYearAndCardExpirationMonthAndCardCvv(
                rentalRequest.getCardNumber(),
                rentalRequest.getCardHolder(),
                rentalRequest.getCardExpirationYear(),
                rentalRequest.getCardExpirationMonth(),
                rentalRequest.getCardCvv())).thenReturn(true, false);

        businessRules.checkIfPaymentExists(id);
        businessRules.checkIfCardExistsByCardNumber(createRequest);
        businessRules.checkIfPaymentIsValid(rentalRequest);
        businessRules.checkIfBalanceIsEnough(100.0, 150.0);

        assertThrows(BusinessException.class, () -> businessRules.checkIfPaymentExists(id));
        assertThrows(BusinessException.class, () -> businessRules.checkIfCardExistsByCardNumber(createRequest));
        assertThrows(BusinessException.class, () -> businessRules.checkIfPaymentIsValid(rentalRequest));
        assertThrows(BusinessException.class, () -> businessRules.checkIfBalanceIsEnough(200.0, 100.0));
    }

    @Test
    void fakePosAdapterCompletesWithoutError()
    {
        assertDoesNotThrow(() -> new FakePosServiceAdapter().pay());
    }

    private static void assertDoesNotThrow(ThrowingRunnable runnable)
    {
        try {
            runnable.run();
        } catch (Exception exception) {
            throw new AssertionError(exception);
        }
    }

    @FunctionalInterface
    private interface ThrowingRunnable
    {
        void run() throws Exception;
    }
}
