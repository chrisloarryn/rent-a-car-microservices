package com.kodlamaio.inventoryservice.business.concretes;

import com.kodlamaio.commonpackage.events.inventory.CarCreatedEvent;
import com.kodlamaio.commonpackage.events.inventory.CarDeletedEvent;
import com.kodlamaio.commonpackage.utils.dto.responses.PageResponse;
import com.kodlamaio.commonpackage.utils.dto.responses.CarClientResponse;
import com.kodlamaio.commonpackage.utils.dto.responses.ClientResponse;
import com.kodlamaio.commonpackage.utils.exceptions.BusinessException;
import com.kodlamaio.commonpackage.utils.mappers.ModelMapperService;
import com.kodlamaio.inventoryservice.business.abstracts.CarService;
import com.kodlamaio.inventoryservice.business.dto.requests.create.CreateCarRequest;
import com.kodlamaio.inventoryservice.business.dto.requests.update.UpdateCarRequest;
import com.kodlamaio.inventoryservice.business.dto.responses.create.CreateCarResponse;
import com.kodlamaio.inventoryservice.business.dto.responses.get.GetAllCarsResponse;
import com.kodlamaio.inventoryservice.business.dto.responses.get.GetCarResponse;
import com.kodlamaio.inventoryservice.business.dto.responses.update.UpdateCarResponse;
import com.kodlamaio.inventoryservice.business.rules.CarBusinessRules;
import com.kodlamaio.inventoryservice.entities.Car;
import com.kodlamaio.inventoryservice.entities.enums.State;
import com.kodlamaio.inventoryservice.repository.CarRepository;
import lombok.AllArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@AllArgsConstructor
public class CarManager implements CarService
{
    private final CarRepository repository;
    private final ModelMapperService mapper;
    private final CarBusinessRules rules;
    private final ApplicationEventPublisher applicationEventPublisher;

    @Override
    public PageResponse<GetAllCarsResponse> getAll(Pageable pageable)
    {
        var cars = repository.findAll(pageable)
                .map(car -> mapper.forResponse().map(car, GetAllCarsResponse.class));

        return PageResponse.from(cars);
    }

    @Override
    public GetCarResponse getById(UUID id)
    {
        rules.checkIfCarExists(id);
        var car = repository.findById(id).orElseThrow();
        var response = mapper.forResponse().map(car, GetCarResponse.class);

        return response;
    }

    @Override
    public CarClientResponse getCarForInvoice(UUID id)
    {
        rules.checkIfCarExists(id);
        var car = repository.findById(id).orElseThrow();
        var response = mapper.forResponse().map(car, CarClientResponse.class);
        response.setSuccess(true);

        return response;
    }

    @Override
    @Transactional
    public CreateCarResponse add(CreateCarRequest request)
    {
        var car = mapper.forRequest().map(request, Car.class);
        car.setId(UUID.randomUUID());
        car.setState(State.AVAILABLE);
        var createdCar = repository.save(car);

        var event = mapper.forResponse().map(createdCar, CarCreatedEvent.class);
        applicationEventPublisher.publishEvent(event);

        var response = mapper.forResponse().map(createdCar, CreateCarResponse.class);
        return response;
    }

    @Override
    @Transactional
    public UpdateCarResponse update(UUID id, UpdateCarRequest request)
    {
        rules.checkIfCarExists(id);
        var car = mapper.forRequest().map(request, Car.class);
        car.setId(id);
        repository.save(car);
        var response = mapper.forResponse().map(car, UpdateCarResponse.class);

        return response;
    }

    @Override
    @Transactional
    public void delete(UUID id)
    {
        rules.checkIfCarExists(id);
        repository.deleteById(id);

        applicationEventPublisher.publishEvent(new CarDeletedEvent(id));
    }

    @Override
    public ClientResponse checkIfCarAvailable(UUID id)
    {
        var response = new ClientResponse();
        validateCarAvailability(id, response);

        return response;
    }

    @Override
    @Transactional
    public void changeStateByCarId(State state, UUID id)
    {
        repository.changeStateByCarId(state, id);
    }

    private void validateCarAvailability(UUID id, ClientResponse response)
    {
        try
        {
            rules.checkIfCarExists(id);
            rules.checkCarAvailability(id);
            response.setSuccess(true);
        }
        catch(BusinessException exception)
        {
            response.setSuccess(false);
            response.setMessage(exception.getMessage());
        }
    }
}
