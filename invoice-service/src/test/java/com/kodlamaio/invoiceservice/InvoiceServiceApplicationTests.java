package com.kodlamaio.invoiceservice;

import com.kodlamaio.commonpackage.events.invoice.CreateInvoiceEvent;
import com.kodlamaio.commonpackage.utils.exceptions.BusinessException;
import com.kodlamaio.commonpackage.utils.mappers.ModelMapperService;
import com.kodlamaio.invoiceservice.api.controllers.InvoicesController;
import com.kodlamaio.invoiceservice.business.abstracts.InvoiceService;
import com.kodlamaio.invoiceservice.business.concretes.InvoiceManager;
import com.kodlamaio.invoiceservice.business.dto.responses.get.GetAllInvoicesResponse;
import com.kodlamaio.invoiceservice.business.dto.responses.get.GetInvoiceResponse;
import com.kodlamaio.invoiceservice.business.kafka.consumer.RentalConsumer;
import com.kodlamaio.invoiceservice.business.rules.InvoiceBusinessRules;
import com.kodlamaio.invoiceservice.entities.Invoice;
import com.kodlamaio.invoiceservice.repository.InvoiceRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.boot.SpringApplication;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InvoiceServiceApplicationTests
{
    @Mock
    private InvoiceService invoiceService;
    @Mock
    private InvoiceRepository repository;
    @Mock
    private ModelMapperService mapperService;
    @Mock
    private ModelMapper mapper;
    @Mock
    private InvoiceBusinessRules rules;

    @Test
    void mainDelegatesToSpringApplication()
    {
        try (MockedStatic<SpringApplication> springApplication = mockStatic(SpringApplication.class)) {
            String[] args = {"--server.port=0"};

            InvoiceServiceApplication.main(args);

            springApplication.verify(() -> SpringApplication.run(InvoiceServiceApplication.class, args));
        }
    }

    @Test
    void controllerDelegatesToService()
    {
        InvoicesController controller = new InvoicesController(invoiceService);
        UUID id = UUID.randomUUID();

        controller.getAll(Pageable.unpaged());
        controller.getById(id);

        verify(invoiceService).getAll(any(Pageable.class));
        verify(invoiceService).getById(id);
    }

    @Test
    void managerCoversQueriesAndAdd()
    {
        InvoiceManager manager = new InvoiceManager(repository, mapperService, rules);
        UUID id = UUID.randomUUID();
        Invoice invoice = new Invoice();
        GetAllInvoicesResponse listResponse = new GetAllInvoicesResponse();
        GetInvoiceResponse detailResponse = new GetInvoiceResponse();

        when(mapperService.forResponse()).thenReturn(mapper);
        when(repository.findAll(any(Pageable.class))).thenReturn(new PageImpl<>(List.of(invoice)));
        when(repository.findById(id)).thenReturn(Optional.of(invoice));
        when(mapper.map(invoice, GetAllInvoicesResponse.class)).thenReturn(listResponse);
        when(mapper.map(invoice, GetInvoiceResponse.class)).thenReturn(detailResponse);

        assertSame(listResponse, manager.getAll().getFirst());
        assertSame(detailResponse, manager.getById(id));
        manager.add(invoice);

        verify(rules, atLeastOnce()).checkIfInvoiceExists(id);
        verify(repository).save(invoice);
    }

    @Test
    void businessRulesValidateRepositoryState()
    {
        InvoiceBusinessRules businessRules = new InvoiceBusinessRules(repository);
        UUID id = UUID.randomUUID();

        when(repository.existsById(id)).thenReturn(true, false);

        businessRules.checkIfInvoiceExists(id);

        BusinessException exception = assertThrows(BusinessException.class, () -> businessRules.checkIfInvoiceExists(id));
        assertEquals("Messages.Invoice.NotFound", exception.getMessage());
    }

    @Test
    void consumerMapsInvoiceEventsAndDelegatesToService()
    {
        RentalConsumer consumer = new RentalConsumer(invoiceService, mapperService);
        CreateInvoiceEvent event = new CreateInvoiceEvent();
        Invoice invoice = new Invoice();

        when(mapperService.forRequest()).thenReturn(mapper);
        when(mapper.map(event, Invoice.class)).thenReturn(invoice);

        consumer.consume(event);

        verify(invoiceService).add(invoice);
    }
}
