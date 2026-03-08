package com.kodlamaio.rentalservice;

import com.kodlamaio.commonpackage.events.invoice.CreateInvoiceEvent;
import com.kodlamaio.commonpackage.events.rental.RentalCreatedEvent;
import com.kodlamaio.commonpackage.events.rental.RentalDeletedEvent;
import com.kodlamaio.commonpackage.utils.dto.requests.CreateRentalPaymentRequest;
import com.kodlamaio.commonpackage.utils.dto.responses.CarClientResponse;
import com.kodlamaio.commonpackage.utils.dto.responses.ClientResponse;
import com.kodlamaio.commonpackage.utils.exceptions.BusinessException;
import com.kodlamaio.commonpackage.utils.kafka.producer.KafkaProducer;
import com.kodlamaio.commonpackage.utils.mappers.ModelMapperService;
import com.kodlamaio.rentalservice.api.clients.CarClient;
import com.kodlamaio.rentalservice.api.clients.CarClientFallback;
import com.kodlamaio.rentalservice.api.clients.PaymentClient;
import com.kodlamaio.rentalservice.api.clients.PaymentClientFallback;
import com.kodlamaio.rentalservice.api.controllers.RentalsController;
import com.kodlamaio.rentalservice.business.abstracts.RentalService;
import com.kodlamaio.rentalservice.business.concretes.RentalManager;
import com.kodlamaio.rentalservice.business.dto.requests.CreateRentalRequest;
import com.kodlamaio.rentalservice.business.dto.requests.UpdateRentalRequest;
import com.kodlamaio.rentalservice.business.dto.responses.CreateRentalResponse;
import com.kodlamaio.rentalservice.business.dto.responses.GetAllRentalsResponse;
import com.kodlamaio.rentalservice.business.dto.responses.GetRentalResponse;
import com.kodlamaio.rentalservice.business.dto.responses.UpdateRentalResponse;
import com.kodlamaio.rentalservice.business.rules.RentalBusinessRules;
import com.kodlamaio.rentalservice.entities.Rental;
import com.kodlamaio.rentalservice.repository.RentalRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.boot.SpringApplication;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RentalServiceApplicationTests
{
    @Mock
    private RentalService rentalService;
    @Mock
    private RentalRepository repository;
    @Mock
    private ModelMapperService mapperService;
    @Mock
    private ModelMapper mapper;
    @Mock
    private RentalBusinessRules rules;
    @Mock
    private CarClient carClient;
    @Mock
    private PaymentClient paymentClient;
    @Mock
    private KafkaProducer producer;

    @Test
    void mainDelegatesToSpringApplication()
    {
        try (MockedStatic<SpringApplication> springApplication = mockStatic(SpringApplication.class)) {
            String[] args = {"--server.port=0"};

            RentalServiceApplication.main(args);

            springApplication.verify(() -> SpringApplication.run(RentalServiceApplication.class, args));
        }
    }

    @Test
    void controllerDelegatesToService() throws InterruptedException
    {
        RentalsController controller = new RentalsController(rentalService);
        UUID id = UUID.randomUUID();
        CreateRentalRequest createRequest = new CreateRentalRequest(id, 100.0, 3, "1234567812345678", "John Doe", 2026, 12, "123");
        UpdateRentalRequest updateRequest = new UpdateRentalRequest(id, 100.0, 3, LocalDate.now());

        controller.getAll();
        controller.getById(id);
        controller.add(createRequest);
        controller.update(id, updateRequest);
        controller.delete(id);

        verify(rentalService).getAll();
        verify(rentalService).getById(id);
        verify(rentalService).add(createRequest);
        verify(rentalService).update(id, updateRequest);
        verify(rentalService).delete(id);
    }

    @Test
    void managerCoversCrudAndInvoicePublication() throws InterruptedException
    {
        RentalManager manager = new RentalManager(repository, mapperService, rules, carClient, producer);
        UUID id = UUID.randomUUID();
        UUID carId = UUID.randomUUID();
        Rental rental = new Rental();
        rental.setId(id);
        rental.setCarId(carId);
        rental.setDailyPrice(100.0);
        rental.setRentedForDays(2);
        CreateRentalRequest createRequest = new CreateRentalRequest(carId, 100.0, 2, "1234567812345678", "John Doe", 2026, 12, "123");
        UpdateRentalRequest updateRequest = new UpdateRentalRequest(carId, 120.0, 2, LocalDate.now());
        CreateRentalPaymentRequest paymentRequest = new CreateRentalPaymentRequest(createRequest.getCardNumber(), createRequest.getCardHolder(), createRequest.getCardExpirationYear(), createRequest.getCardExpirationMonth(), createRequest.getCardCvv(), 200.0);
        CreateInvoiceEvent invoiceEvent = new CreateInvoiceEvent();
        CreateRentalResponse createResponse = new CreateRentalResponse();
        UpdateRentalResponse updateResponse = new UpdateRentalResponse();
        GetRentalResponse detailResponse = new GetRentalResponse();
        GetAllRentalsResponse listResponse = new GetAllRentalsResponse();
        CarClientResponse carClientResponse = new CarClientResponse();
        carClientResponse.setBrandName("Audi");
        carClientResponse.setModelName("A3");
        carClientResponse.setPlate("34 ABC 123");
        carClientResponse.setModelYear(2024);

        when(mapperService.forRequest()).thenReturn(mapper);
        when(mapperService.forResponse()).thenReturn(mapper);
        when(repository.findAll()).thenReturn(List.of(rental));
        when(repository.findById(id)).thenReturn(Optional.of(rental));
        when(repository.save(any(Rental.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(carClient.getCar(carId)).thenReturn(carClientResponse);
        when(mapper.map(rental, GetAllRentalsResponse.class)).thenReturn(listResponse);
        when(mapper.map(rental, GetRentalResponse.class)).thenReturn(detailResponse);
        when(mapper.map(createRequest, Rental.class)).thenReturn(rental);
        when(mapper.map(updateRequest, Rental.class)).thenReturn(rental);
        when(mapper.map(rental, CreateRentalResponse.class)).thenReturn(createResponse);
        when(mapper.map(rental, UpdateRentalResponse.class)).thenReturn(updateResponse);
        doAnswer(invocation -> null)
                .when(mapper).map(eq(createRequest), any(CreateRentalPaymentRequest.class));
        doAnswer(invocation -> null)
                .when(mapper).map(eq(createRequest), any(CreateInvoiceEvent.class));
        doAnswer(invocation -> null)
                .when(mapper).map(eq(carClientResponse), any(CreateInvoiceEvent.class));
        doAnswer(invocation -> null)
                .when(mapper).map(eq(rental), any(CreateInvoiceEvent.class));

        assertSame(listResponse, manager.getAll().getFirst());
        assertSame(detailResponse, manager.getById(id));
        assertSame(createResponse, manager.add(createRequest));
        assertSame(updateResponse, manager.update(id, updateRequest));
        manager.delete(id);

        verify(rules, atLeastOnce()).checkIfRentalExists(id);
        verify(rules).ensureCarIsAvailable(carId);
        verify(rules).ensurePaymentIsProcessed(any(CreateRentalPaymentRequest.class));
        verify(producer).sendMessage(any(RentalCreatedEvent.class), eq("rental-created"));
        verify(producer).sendMessage(any(RentalDeletedEvent.class), eq("rental-deleted"));
        verify(producer).sendMessage(any(CreateInvoiceEvent.class), eq("create-invoice"));
    }

    @Test
    void businessRulesValidateRepositoryAndRemoteCalls() throws InterruptedException
    {
        RentalBusinessRules businessRules = new RentalBusinessRules(repository, carClient, paymentClient);
        UUID id = UUID.randomUUID();
        UUID carId = UUID.randomUUID();
        CreateRentalPaymentRequest paymentRequest = new CreateRentalPaymentRequest("1234567812345678", "John Doe", 2026, 12, "123", 50.0);

        when(repository.existsById(id)).thenReturn(true, false);
        when(carClient.checkIfCarAvailable(carId)).thenReturn(new ClientResponse(true, null), new ClientResponse(false, "Car unavailable"));
        when(paymentClient.processRentalPayment(paymentRequest)).thenReturn(new ClientResponse(true, null), new ClientResponse(false, "Payment failed"));

        businessRules.checkIfRentalExists(id);
        businessRules.ensureCarIsAvailable(carId);
        businessRules.ensurePaymentIsProcessed(paymentRequest);

        assertThrows(BusinessException.class, () -> businessRules.checkIfRentalExists(id));
        assertThrows(BusinessException.class, () -> businessRules.ensureCarIsAvailable(carId));
        assertThrows(BusinessException.class, () -> businessRules.ensurePaymentIsProcessed(paymentRequest));
    }

    @Test
    void fallbackClientsThrowWhenRemoteServicesAreDown()
    {
        RuntimeException carException = assertThrows(RuntimeException.class, () -> new CarClientFallback().checkIfCarAvailable(UUID.randomUUID()));
        RuntimeException paymentException = assertThrows(RuntimeException.class, () -> new PaymentClientFallback().processRentalPayment(
                new CreateRentalPaymentRequest("1234567812345678", "John Doe", 2026, 12, "123", 50.0)));

        assertEquals("Inventory Down", carException.getMessage());
        assertEquals("PAYMENT DOWN", paymentException.getMessage());
    }
}
