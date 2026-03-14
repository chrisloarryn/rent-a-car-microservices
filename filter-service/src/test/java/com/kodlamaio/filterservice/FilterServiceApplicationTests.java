package com.kodlamaio.filterservice;

import com.kodlamaio.commonpackage.events.inventory.BrandDeletedEvent;
import com.kodlamaio.commonpackage.events.inventory.CarCreatedEvent;
import com.kodlamaio.commonpackage.events.inventory.CarDeletedEvent;
import com.kodlamaio.commonpackage.events.inventory.ModelDeletedEvent;
import com.kodlamaio.commonpackage.events.maintenance.MaintenanceCompletedEvent;
import com.kodlamaio.commonpackage.events.maintenance.MaintenanceCreatedEvent;
import com.kodlamaio.commonpackage.events.maintenance.MaintenanceDeletedEvent;
import com.kodlamaio.commonpackage.events.rental.RentalCreatedEvent;
import com.kodlamaio.commonpackage.events.rental.RentalDeletedEvent;
import com.kodlamaio.commonpackage.utils.exceptions.BusinessException;
import com.kodlamaio.commonpackage.utils.mappers.ModelMapperService;
import com.kodlamaio.filterservice.api.controllers.FiltersController;
import com.kodlamaio.filterservice.business.abstracts.FilterService;
import com.kodlamaio.filterservice.business.concretes.FilterManager;
import com.kodlamaio.filterservice.business.dto.responses.GetAllFiltersResponse;
import com.kodlamaio.filterservice.business.dto.responses.GetFilterResponse;
import com.kodlamaio.filterservice.business.kafka.consumer.InventoryConsumer;
import com.kodlamaio.filterservice.business.kafka.consumer.MaintenanceConsumer;
import com.kodlamaio.filterservice.business.kafka.consumer.RentalConsumer;
import com.kodlamaio.filterservice.entities.Filter;
import com.kodlamaio.filterservice.repository.FilterRepository;
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
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FilterServiceApplicationTests
{
    @Mock
    private FilterService filterService;
    @Mock
    private FilterRepository repository;
    @Mock
    private ModelMapperService mapperService;
    @Mock
    private ModelMapper mapper;

    @Test
    void mainDelegatesToSpringApplication()
    {
        try (MockedStatic<SpringApplication> springApplication = mockStatic(SpringApplication.class)) {
            String[] args = {"--server.port=0"};

            FilterServiceApplication.main(args);

            springApplication.verify(() -> SpringApplication.run(FilterServiceApplication.class, args));
        }
    }

    @Test
    void controllerDelegatesToService()
    {
        FiltersController controller = new FiltersController(filterService);
        UUID id = UUID.randomUUID();

        controller.getAll(Pageable.unpaged());
        controller.getById(id);

        verify(filterService).getAll(any(Pageable.class));
        verify(filterService).getById(id);
    }

    @Test
    void managerCoversQueriesAndMutations()
    {
        FilterManager manager = new FilterManager(repository, mapperService);
        UUID id = UUID.randomUUID();
        UUID carId = UUID.randomUUID();
        Filter filter = new Filter();
        filter.setCarId(carId);
        GetAllFiltersResponse listResponse = new GetAllFiltersResponse();
        GetFilterResponse detailResponse = new GetFilterResponse();

        when(mapperService.forResponse()).thenReturn(mapper);
        when(repository.findAll(any(Pageable.class))).thenReturn(new PageImpl<>(List.of(filter)));
        when(repository.findById(id)).thenReturn(Optional.of(filter));
        when(repository.findByCarId(carId)).thenReturn(filter);
        when(mapper.map(filter, GetAllFiltersResponse.class)).thenReturn(listResponse);
        when(mapper.map(filter, GetFilterResponse.class)).thenReturn(detailResponse);

        assertSame(listResponse, manager.getAll().getFirst());
        assertSame(detailResponse, manager.getById(id));
        manager.add(filter);
        manager.delete(id);
        manager.deleteAllByBrandId(id);
        manager.deleteAllByModelId(id);
        manager.deleteByCarId(carId);
        assertSame(filter, manager.getByCarId(carId));

        verify(repository).save(filter);
        verify(repository).deleteById(id);
        verify(repository).deleteAllByBrandId(id);
        verify(repository).deleteAllByModelId(id);
        verify(repository).deleteByCarId(carId);
    }

    @Test
    void managerThrowsWhenFilterIsMissing()
    {
        FilterManager manager = new FilterManager(repository, mapperService);
        UUID id = UUID.randomUUID();

        when(repository.findById(id)).thenReturn(Optional.empty());
        when(repository.findByCarId(id)).thenReturn(null);

        assertThrows(BusinessException.class, () -> manager.getById(id));
        assertThrows(BusinessException.class, () -> manager.getByCarId(id));
    }

    @Test
    void consumersUpdateFiltersFromEvents()
    {
        InventoryConsumer inventoryConsumer = new InventoryConsumer(filterService, mapperService);
        MaintenanceConsumer maintenanceConsumer = new MaintenanceConsumer(filterService, mapperService);
        RentalConsumer rentalConsumer = new RentalConsumer(filterService);
        UUID carId = UUID.randomUUID();
        UUID brandId = UUID.randomUUID();
        UUID modelId = UUID.randomUUID();
        Filter filter = new Filter();
        filter.setCarId(carId);

        when(mapperService.forRequest()).thenReturn(mapper);
        when(mapper.map(any(CarCreatedEvent.class), any())).thenReturn(filter);
        when(filterService.getByCarId(carId)).thenReturn(filter);

        inventoryConsumer.consume(new CarCreatedEvent(carId, modelId, brandId, "A3", "Audi", "34 ABC 123", 2024, 100.0, "AVAILABLE"));
        inventoryConsumer.consume(new CarDeletedEvent(carId));
        inventoryConsumer.consume(new ModelDeletedEvent(modelId));
        inventoryConsumer.consume(new BrandDeletedEvent(brandId));
        maintenanceConsumer.consume(new MaintenanceCreatedEvent(carId));
        maintenanceConsumer.consume(new MaintenanceDeletedEvent(carId));
        maintenanceConsumer.consume(new MaintenanceCompletedEvent(carId));
        rentalConsumer.consume(new RentalCreatedEvent(carId));
        rentalConsumer.consume(new RentalDeletedEvent(carId));

        verify(filterService, atLeast(6)).add(filter);
        verify(filterService).deleteByCarId(carId);
        verify(filterService).deleteAllByModelId(modelId);
        verify(filterService).deleteAllByBrandId(brandId);
        assertEquals("AVAILABLE", filter.getState());
    }
}
