package com.kodlamaio.maintenanceservice;

import com.kodlamaio.commonpackage.events.maintenance.MaintenanceCompletedEvent;
import com.kodlamaio.commonpackage.events.maintenance.MaintenanceCreatedEvent;
import com.kodlamaio.commonpackage.events.maintenance.MaintenanceDeletedEvent;
import com.kodlamaio.commonpackage.utils.dto.responses.ClientResponse;
import com.kodlamaio.commonpackage.utils.exceptions.BusinessException;
import com.kodlamaio.commonpackage.utils.kafka.producer.KafkaProducer;
import com.kodlamaio.commonpackage.utils.mappers.ModelMapperService;
import com.kodlamaio.maintenanceservice.api.clients.CarClient;
import com.kodlamaio.maintenanceservice.api.clients.CarClientFallback;
import com.kodlamaio.maintenanceservice.api.controllers.MaintenancesController;
import com.kodlamaio.maintenanceservice.business.abstracts.MaintenanceService;
import com.kodlamaio.maintenanceservice.business.concretes.MaintenanceManager;
import com.kodlamaio.maintenanceservice.business.dto.requests.create.CreateMaintenanceRequest;
import com.kodlamaio.maintenanceservice.business.dto.requests.update.UpdateMaintenanceRequest;
import com.kodlamaio.maintenanceservice.business.dto.responses.create.CreateMaintenanceResponse;
import com.kodlamaio.maintenanceservice.business.dto.responses.get.GetAllMaintenanceResponse;
import com.kodlamaio.maintenanceservice.business.dto.responses.get.GetMaintenanceResponse;
import com.kodlamaio.maintenanceservice.business.dto.responses.update.UpdateMaintenanceResponse;
import com.kodlamaio.maintenanceservice.business.rules.MaintenanceBusinessRules;
import com.kodlamaio.maintenanceservice.entities.Maintenance;
import com.kodlamaio.maintenanceservice.repository.MaintenanceRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.boot.SpringApplication;

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
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MaintenanceServiceApplicationTests
{
    @Mock
    private MaintenanceService maintenanceService;
    @Mock
    private MaintenanceRepository repository;
    @Mock
    private ModelMapperService mapperService;
    @Mock
    private ModelMapper mapper;
    @Mock
    private MaintenanceBusinessRules rules;
    @Mock
    private KafkaProducer producer;
    @Mock
    private CarClient carClient;

    @Test
    void mainDelegatesToSpringApplication()
    {
        try (MockedStatic<SpringApplication> springApplication = mockStatic(SpringApplication.class)) {
            String[] args = {"--server.port=0"};

            MaintenanceServiceApplication.main(args);

            springApplication.verify(() -> SpringApplication.run(MaintenanceServiceApplication.class, args));
        }
    }

    @Test
    void controllerDelegatesToService() throws InterruptedException
    {
        MaintenancesController controller = new MaintenancesController(maintenanceService);
        UUID id = UUID.randomUUID();
        CreateMaintenanceRequest createRequest = new CreateMaintenanceRequest("broken wheel", id);
        UpdateMaintenanceRequest updateRequest = new UpdateMaintenanceRequest(id, "fixed", true, LocalDateTime.now(), LocalDateTime.now());

        controller.getAll();
        controller.getById(id);
        controller.add(createRequest);
        controller.returnCarFromMaintenance(id);
        controller.update(id, updateRequest);
        controller.remove(id);

        verify(maintenanceService).getAll();
        verify(maintenanceService).getById(id);
        verify(maintenanceService).add(createRequest);
        verify(maintenanceService).returnCarFromMaintenance(id);
        verify(maintenanceService).update(id, updateRequest);
        verify(maintenanceService).delete(id);
    }

    @Test
    void managerCoversCrudAndStateTransitions() throws InterruptedException
    {
        MaintenanceManager manager = new MaintenanceManager(repository, mapperService, rules, producer);
        UUID id = UUID.randomUUID();
        UUID carId = UUID.randomUUID();
        Maintenance maintenance = new Maintenance();
        maintenance.setId(id);
        maintenance.setCarId(carId);
        maintenance.setCompleted(false);
        CreateMaintenanceRequest createRequest = new CreateMaintenanceRequest("flat tire", carId);
        UpdateMaintenanceRequest updateRequest = new UpdateMaintenanceRequest(carId, "updated", true, LocalDateTime.now(), LocalDateTime.now());
        CreateMaintenanceResponse createResponse = new CreateMaintenanceResponse();
        UpdateMaintenanceResponse updateResponse = new UpdateMaintenanceResponse();
        GetMaintenanceResponse detailResponse = new GetMaintenanceResponse();
        GetAllMaintenanceResponse listResponse = new GetAllMaintenanceResponse();

        when(mapperService.forRequest()).thenReturn(mapper);
        when(mapperService.forResponse()).thenReturn(mapper);
        when(repository.findAll()).thenReturn(List.of(maintenance));
        when(repository.findById(id)).thenReturn(Optional.of(maintenance));
        when(repository.findByCarIdAndIsCompletedIsFalse(carId)).thenReturn(maintenance);
        when(repository.save(any(Maintenance.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(repository.existsByCarIdAndIsCompletedIsFalse(carId)).thenReturn(true);
        when(mapper.map(maintenance, GetAllMaintenanceResponse.class)).thenReturn(listResponse);
        when(mapper.map(maintenance, GetMaintenanceResponse.class)).thenReturn(detailResponse);
        when(mapper.map(createRequest, Maintenance.class)).thenReturn(maintenance);
        when(mapper.map(updateRequest, Maintenance.class)).thenReturn(maintenance);
        when(mapper.map(maintenance, CreateMaintenanceResponse.class)).thenReturn(createResponse);
        when(mapper.map(maintenance, UpdateMaintenanceResponse.class)).thenReturn(updateResponse);

        assertSame(listResponse, manager.getAll().getFirst());
        assertSame(detailResponse, manager.getById(id));
        assertSame(detailResponse, manager.returnCarFromMaintenance(carId));
        assertSame(createResponse, manager.add(createRequest));
        assertSame(updateResponse, manager.update(id, updateRequest));
        manager.delete(id);

        verify(rules, atLeastOnce()).checkIfMaintenanceExists(id);
        verify(rules).checkIfCarIsNotUnderMaintenance(carId);
        verify(rules).checkIfCarIsUnderMaintenance(carId);
        verify(rules).ensureCarIsAvailable(carId);
        verify(producer).sendMessage(any(MaintenanceCompletedEvent.class), eq("maintenance-completed"));
        verify(producer).sendMessage(any(MaintenanceCreatedEvent.class), eq("maintenance-created"));
        verify(producer).sendMessage(any(MaintenanceDeletedEvent.class), eq("maintenance-deleted"));
    }

    @Test
    void businessRulesValidateRepositoryAndRemoteCarAvailability() throws InterruptedException
    {
        MaintenanceBusinessRules businessRules = new MaintenanceBusinessRules(repository, carClient);
        UUID id = UUID.randomUUID();
        UUID carId = UUID.randomUUID();
        ClientResponse successResponse = new ClientResponse(true, null);
        ClientResponse failureResponse = new ClientResponse(false, "Inventory down");

        when(carClient.checkIfCarAvailable(carId)).thenReturn(successResponse, failureResponse);
        when(repository.existsById(id)).thenReturn(true, false);
        when(repository.existsByCarIdAndIsCompletedIsFalse(carId)).thenReturn(true, false, false, true);

        businessRules.ensureCarIsAvailable(carId);
        businessRules.checkIfMaintenanceExists(id);
        businessRules.checkIfCarIsNotUnderMaintenance(carId);
        businessRules.checkIfCarIsUnderMaintenance(carId);

        assertThrows(BusinessException.class, () -> businessRules.ensureCarIsAvailable(carId));
        assertThrows(BusinessException.class, () -> businessRules.checkIfMaintenanceExists(id));
        assertThrows(BusinessException.class, () -> businessRules.checkIfCarIsNotUnderMaintenance(carId));
        assertThrows(BusinessException.class, () -> businessRules.checkIfCarIsUnderMaintenance(carId));
    }

    @Test
    void fallbackThrowsWhenInventoryServiceIsDown()
    {
        CarClientFallback fallback = new CarClientFallback();

        RuntimeException exception = assertThrows(RuntimeException.class, () -> fallback.checkIfCarAvailable(UUID.randomUUID()));

        assertEquals("Inventory Down", exception.getMessage());
    }
}
