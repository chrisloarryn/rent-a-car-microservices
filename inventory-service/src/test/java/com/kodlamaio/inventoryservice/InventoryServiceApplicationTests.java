package com.kodlamaio.inventoryservice;

import com.kodlamaio.commonpackage.events.inventory.BrandDeletedEvent;
import com.kodlamaio.commonpackage.events.inventory.CarCreatedEvent;
import com.kodlamaio.commonpackage.events.inventory.CarDeletedEvent;
import com.kodlamaio.commonpackage.events.inventory.ModelDeletedEvent;
import com.kodlamaio.commonpackage.events.maintenance.MaintenanceCompletedEvent;
import com.kodlamaio.commonpackage.events.maintenance.MaintenanceCreatedEvent;
import com.kodlamaio.commonpackage.events.maintenance.MaintenanceDeletedEvent;
import com.kodlamaio.commonpackage.events.rental.RentalCreatedEvent;
import com.kodlamaio.commonpackage.events.rental.RentalDeletedEvent;
import com.kodlamaio.commonpackage.utils.dto.responses.CarClientResponse;
import com.kodlamaio.commonpackage.utils.dto.responses.ClientResponse;
import com.kodlamaio.commonpackage.utils.exceptions.BusinessException;
import com.kodlamaio.commonpackage.utils.kafka.producer.KafkaProducer;
import com.kodlamaio.commonpackage.utils.mappers.ModelMapperService;
import com.kodlamaio.inventoryservice.api.controllers.BrandsController;
import com.kodlamaio.inventoryservice.api.controllers.CarsController;
import com.kodlamaio.inventoryservice.api.controllers.ModelsController;
import com.kodlamaio.inventoryservice.business.abstracts.BrandService;
import com.kodlamaio.inventoryservice.business.abstracts.CarService;
import com.kodlamaio.inventoryservice.business.abstracts.ModelService;
import com.kodlamaio.inventoryservice.business.concretes.BrandManager;
import com.kodlamaio.inventoryservice.business.concretes.CarManager;
import com.kodlamaio.inventoryservice.business.concretes.ModelManager;
import com.kodlamaio.inventoryservice.business.dto.requests.create.CreateBrandRequest;
import com.kodlamaio.inventoryservice.business.dto.requests.create.CreateCarRequest;
import com.kodlamaio.inventoryservice.business.dto.requests.create.CreateModelRequest;
import com.kodlamaio.inventoryservice.business.dto.requests.update.UpdateBrandRequest;
import com.kodlamaio.inventoryservice.business.dto.requests.update.UpdateCarRequest;
import com.kodlamaio.inventoryservice.business.dto.requests.update.UpdateModelRequest;
import com.kodlamaio.inventoryservice.business.dto.responses.create.CreateBrandResponse;
import com.kodlamaio.inventoryservice.business.dto.responses.create.CreateCarResponse;
import com.kodlamaio.inventoryservice.business.dto.responses.create.CreateModelResponse;
import com.kodlamaio.inventoryservice.business.dto.responses.get.GetAllBrandsResponse;
import com.kodlamaio.inventoryservice.business.dto.responses.get.GetAllCarsResponse;
import com.kodlamaio.inventoryservice.business.dto.responses.get.GetAllModelsResponse;
import com.kodlamaio.inventoryservice.business.dto.responses.get.GetBrandResponse;
import com.kodlamaio.inventoryservice.business.dto.responses.get.GetCarResponse;
import com.kodlamaio.inventoryservice.business.dto.responses.get.GetModelResponse;
import com.kodlamaio.inventoryservice.business.dto.responses.update.UpdateBrandResponse;
import com.kodlamaio.inventoryservice.business.dto.responses.update.UpdateCarResponse;
import com.kodlamaio.inventoryservice.business.dto.responses.update.UpdateModelResponse;
import com.kodlamaio.inventoryservice.business.kafka.consumer.MaintenanceConsumer;
import com.kodlamaio.inventoryservice.business.kafka.consumer.RentalConsumer;
import com.kodlamaio.inventoryservice.business.rules.BrandBusinessRules;
import com.kodlamaio.inventoryservice.business.rules.CarBusinessRules;
import com.kodlamaio.inventoryservice.business.rules.ModelBusinessRules;
import com.kodlamaio.inventoryservice.entities.Brand;
import com.kodlamaio.inventoryservice.entities.Car;
import com.kodlamaio.inventoryservice.entities.Model;
import com.kodlamaio.inventoryservice.entities.enums.State;
import com.kodlamaio.inventoryservice.repository.BrandRepository;
import com.kodlamaio.inventoryservice.repository.CarRepository;
import com.kodlamaio.inventoryservice.repository.ModelRepository;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InventoryServiceApplicationTests
{
    @Mock
    private BrandService brandService;
    @Mock
    private CarService carService;
    @Mock
    private ModelService modelService;
    @Mock
    private BrandRepository brandRepository;
    @Mock
    private CarRepository carRepository;
    @Mock
    private ModelRepository modelRepository;
    @Mock
    private ModelMapperService mapperService;
    @Mock
    private ModelMapper mapper;
    @Mock
    private BrandBusinessRules brandBusinessRules;
    @Mock
    private CarBusinessRules carBusinessRules;
    @Mock
    private ModelBusinessRules modelBusinessRules;
    @Mock
    private KafkaProducer producer;

    @Test
    void mainDelegatesToSpringApplication()
    {
        try (MockedStatic<SpringApplication> springApplication = mockStatic(SpringApplication.class)) {
            String[] args = {"--server.port=0"};

            InventoryServiceApplication.main(args);

            springApplication.verify(() -> SpringApplication.run(InventoryServiceApplication.class, args));
        }
    }

    @Test
    void controllersDelegateToServices()
    {
        BrandsController brandsController = new BrandsController(brandService);
        CarsController carsController = new CarsController(carService);
        ModelsController modelsController = new ModelsController(modelService);
        UUID id = UUID.randomUUID();
        CreateBrandRequest createBrandRequest = new CreateBrandRequest("Audi");
        UpdateBrandRequest updateBrandRequest = new UpdateBrandRequest("BMW");
        CreateCarRequest createCarRequest = new CreateCarRequest(UUID.randomUUID(), 2024, "34 ABC 123", 100.0);
        UpdateCarRequest updateCarRequest = new UpdateCarRequest(UUID.randomUUID(), 2024, "34 ABC 123", State.AVAILABLE, 100.0);
        CreateModelRequest createModelRequest = new CreateModelRequest(UUID.randomUUID(), "A3");
        UpdateModelRequest updateModelRequest = new UpdateModelRequest(UUID.randomUUID(), "A4");

        brandsController.getAll();
        brandsController.getById(id);
        brandsController.add(createBrandRequest);
        brandsController.update(id, updateBrandRequest);
        brandsController.delete(id);
        carsController.getAll();
        carsController.getById(id);
        carsController.add(createCarRequest);
        carsController.update(id, updateCarRequest);
        carsController.delete(id);
        carsController.checkIfCarAvailable(id);
        carsController.getCarForInvoice(id);
        modelsController.getAll();
        modelsController.getById(id);
        modelsController.add(createModelRequest);
        modelsController.update(id, updateModelRequest);
        modelsController.delete(id);

        verify(brandService).getAll();
        verify(brandService).getById(id);
        verify(brandService).add(createBrandRequest);
        verify(brandService).update(id, updateBrandRequest);
        verify(brandService).delete(id);
        verify(carService).getAll();
        verify(carService).getById(id);
        verify(carService).add(createCarRequest);
        verify(carService).update(id, updateCarRequest);
        verify(carService).delete(id);
        verify(carService).checkIfCarAvailable(id);
        verify(carService).getCarForInvoice(id);
        verify(modelService).getAll();
        verify(modelService).getById(id);
        verify(modelService).add(createModelRequest);
        verify(modelService).update(id, updateModelRequest);
        verify(modelService).delete(id);
    }

    @Test
    void brandManagerCoversCrudFlowAndPublishesDeleteEvent()
    {
        BrandManager manager = new BrandManager(brandRepository, mapperService, brandBusinessRules, producer);
        UUID id = UUID.randomUUID();
        Brand brand = new Brand();
        CreateBrandRequest createRequest = new CreateBrandRequest("Audi");
        UpdateBrandRequest updateRequest = new UpdateBrandRequest("BMW");
        CreateBrandResponse createResponse = new CreateBrandResponse();
        UpdateBrandResponse updateResponse = new UpdateBrandResponse();
        GetBrandResponse detailResponse = new GetBrandResponse();
        GetAllBrandsResponse listResponse = new GetAllBrandsResponse();

        when(mapperService.forRequest()).thenReturn(mapper);
        when(mapperService.forResponse()).thenReturn(mapper);
        when(brandRepository.findAll()).thenReturn(List.of(brand));
        when(brandRepository.findById(id)).thenReturn(Optional.of(brand));
        when(mapper.map(brand, GetAllBrandsResponse.class)).thenReturn(listResponse);
        when(mapper.map(brand, GetBrandResponse.class)).thenReturn(detailResponse);
        when(mapper.map(createRequest, Brand.class)).thenReturn(brand);
        when(mapper.map(updateRequest, Brand.class)).thenReturn(brand);
        when(mapper.map(brand, CreateBrandResponse.class)).thenReturn(createResponse);
        when(mapper.map(brand, UpdateBrandResponse.class)).thenReturn(updateResponse);

        assertSame(listResponse, manager.getAll().getFirst());
        assertSame(detailResponse, manager.getById(id));
        assertSame(createResponse, manager.add(createRequest));
        assertSame(updateResponse, manager.update(id, updateRequest));
        manager.delete(id);

        verify(brandBusinessRules, atLeastOnce()).checkIfBrandExists(id);
        verify(brandRepository, atLeastOnce()).save(brand);
        verify(brandRepository).deleteById(id);
        verify(producer).sendMessage(any(BrandDeletedEvent.class), eq("brand-deleted"));
    }

    @Test
    void carManagerCoversCrudAndAvailabilityScenarios()
    {
        CarManager manager = new CarManager(carRepository, mapperService, carBusinessRules, producer);
        UUID id = UUID.randomUUID();
        Car car = new Car();
        car.setId(id);
        car.setState(State.AVAILABLE);
        CreateCarRequest createRequest = new CreateCarRequest(UUID.randomUUID(), 2024, "34 ABC 123", 100.0);
        UpdateCarRequest updateRequest = new UpdateCarRequest(UUID.randomUUID(), 2024, "34 ABC 123", State.RENTED, 200.0);
        CreateCarResponse createResponse = new CreateCarResponse();
        UpdateCarResponse updateResponse = new UpdateCarResponse();
        GetCarResponse detailResponse = new GetCarResponse();
        GetAllCarsResponse listResponse = new GetAllCarsResponse();
        CarClientResponse clientResponse = new CarClientResponse();

        when(mapperService.forRequest()).thenReturn(mapper);
        when(mapperService.forResponse()).thenReturn(mapper);
        when(carRepository.findAll()).thenReturn(List.of(car));
        when(carRepository.findById(id)).thenReturn(Optional.of(car));
        when(carRepository.save(any(Car.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(mapper.map(car, GetAllCarsResponse.class)).thenReturn(listResponse);
        when(mapper.map(car, GetCarResponse.class)).thenReturn(detailResponse);
        when(mapper.map(car, CarClientResponse.class)).thenReturn(clientResponse);
        when(mapper.map(createRequest, Car.class)).thenReturn(car);
        when(mapper.map(updateRequest, Car.class)).thenReturn(car);
        when(mapper.map(any(Car.class), eq(CreateCarResponse.class))).thenReturn(createResponse);
        when(mapper.map(any(Car.class), eq(UpdateCarResponse.class))).thenReturn(updateResponse);
        when(mapper.map(any(Car.class), eq(CarCreatedEvent.class))).thenReturn(new CarCreatedEvent());

        assertSame(listResponse, manager.getAll().getFirst());
        assertSame(detailResponse, manager.getById(id));
        assertSame(clientResponse, manager.getCarForInvoice(id));
        assertTrue(manager.getCarForInvoice(id).isSuccess());
        assertSame(createResponse, manager.add(createRequest));
        assertSame(updateResponse, manager.update(id, updateRequest));
        assertTrue(manager.checkIfCarAvailable(id).isSuccess());

        doThrow(new BusinessException("CAR_NOT_AVAILABLE")).when(carBusinessRules).checkCarAvailability(id);
        ClientResponse unavailableResponse = manager.checkIfCarAvailable(id);
        assertFalse(unavailableResponse.isSuccess());
        assertEquals("CAR_NOT_AVAILABLE", unavailableResponse.getMessage());

        manager.changeStateByCarId(State.RENTED, id);
        manager.delete(id);

        verify(carBusinessRules, atLeastOnce()).checkIfCarExists(id);
        verify(carRepository).changeStateByCarId(State.RENTED, id);
        verify(producer).sendMessage(any(CarCreatedEvent.class), eq("car-created"));
        verify(producer).sendMessage(any(CarDeletedEvent.class), eq("car-deleted"));
    }

    @Test
    void modelManagerCoversCrudFlowAndPublishesDeleteEvent()
    {
        ModelManager manager = new ModelManager(modelRepository, mapperService, modelBusinessRules, producer);
        UUID id = UUID.randomUUID();
        Model model = new Model();
        CreateModelRequest createRequest = new CreateModelRequest(UUID.randomUUID(), "A3");
        UpdateModelRequest updateRequest = new UpdateModelRequest(UUID.randomUUID(), "A4");
        CreateModelResponse createResponse = new CreateModelResponse();
        UpdateModelResponse updateResponse = new UpdateModelResponse();
        GetModelResponse detailResponse = new GetModelResponse();
        GetAllModelsResponse listResponse = new GetAllModelsResponse();

        when(mapperService.forRequest()).thenReturn(mapper);
        when(mapperService.forResponse()).thenReturn(mapper);
        when(modelRepository.findAll()).thenReturn(List.of(model));
        when(modelRepository.findById(id)).thenReturn(Optional.of(model));
        when(mapper.map(model, GetAllModelsResponse.class)).thenReturn(listResponse);
        when(mapper.map(model, GetModelResponse.class)).thenReturn(detailResponse);
        when(mapper.map(createRequest, Model.class)).thenReturn(model);
        when(mapper.map(updateRequest, Model.class)).thenReturn(model);
        when(mapper.map(model, CreateModelResponse.class)).thenReturn(createResponse);
        when(mapper.map(model, UpdateModelResponse.class)).thenReturn(updateResponse);

        assertSame(listResponse, manager.getAll().getFirst());
        assertSame(detailResponse, manager.getById(id));
        assertSame(createResponse, manager.add(createRequest));
        assertSame(updateResponse, manager.update(id, updateRequest));
        manager.delete(id);

        verify(modelBusinessRules, atLeastOnce()).checkIfModelExists(id);
        verify(modelRepository, atLeastOnce()).save(model);
        verify(modelRepository).deleteById(id);
        verify(producer).sendMessage(any(ModelDeletedEvent.class), eq("model-deleted"));
    }

    @Test
    void businessRulesValidateRepositoryState()
    {
        UUID id = UUID.randomUUID();
        BrandBusinessRules brandRules = new BrandBusinessRules(brandRepository);
        CarBusinessRules carRules = new CarBusinessRules(carRepository);
        ModelBusinessRules modelRules = new ModelBusinessRules(modelRepository);
        Car availableCar = new Car();
        availableCar.setState(State.AVAILABLE);
        Car rentedCar = new Car();
        rentedCar.setState(State.RENTED);

        when(brandRepository.existsById(id)).thenReturn(true, false);
        when(carRepository.existsById(id)).thenReturn(true, false);
        when(modelRepository.existsById(id)).thenReturn(true, false);
        when(carRepository.findById(id)).thenReturn(Optional.of(availableCar), Optional.of(rentedCar));

        brandRules.checkIfBrandExists(id);
        carRules.checkIfCarExists(id);
        modelRules.checkIfModelExists(id);
        carRules.checkCarAvailability(id);

        assertThrows(BusinessException.class, () -> brandRules.checkIfBrandExists(id));
        assertThrows(BusinessException.class, () -> carRules.checkIfCarExists(id));
        assertThrows(BusinessException.class, () -> modelRules.checkIfModelExists(id));
        assertThrows(BusinessException.class, () -> carRules.checkCarAvailability(id));
    }

    @Test
    void consumersChangeCarStateBasedOnEvents()
    {
        MaintenanceConsumer maintenanceConsumer = new MaintenanceConsumer(carService);
        RentalConsumer rentalConsumer = new RentalConsumer(carService);
        UUID carId = UUID.randomUUID();

        maintenanceConsumer.consume(new MaintenanceCreatedEvent(carId));
        maintenanceConsumer.consume(new MaintenanceDeletedEvent(carId));
        maintenanceConsumer.consume(new MaintenanceCompletedEvent(carId));
        rentalConsumer.consume(new RentalCreatedEvent(carId));
        rentalConsumer.consume(new RentalDeletedEvent(carId));

        verify(carService).changeStateByCarId(State.MAINTENANCE, carId);
        verify(carService, atLeastOnce()).changeStateByCarId(State.AVAILABLE, carId);
        verify(carService).changeStateByCarId(State.RENTED, carId);
    }
}
