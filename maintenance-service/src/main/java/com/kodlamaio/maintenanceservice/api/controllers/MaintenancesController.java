package com.kodlamaio.maintenanceservice.api.controllers;

import com.kodlamaio.maintenanceservice.business.abstracts.MaintenanceService;
import com.kodlamaio.maintenanceservice.business.dto.requests.create.CreateMaintenanceRequest;
import com.kodlamaio.maintenanceservice.business.dto.requests.update.UpdateMaintenanceRequest;
import com.kodlamaio.maintenanceservice.business.dto.responses.create.CreateMaintenanceResponse;
import com.kodlamaio.maintenanceservice.business.dto.responses.get.GetAllMaintenanceResponse;
import com.kodlamaio.maintenanceservice.business.dto.responses.get.GetMaintenanceResponse;
import com.kodlamaio.maintenanceservice.business.dto.responses.update.UpdateMaintenanceResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/maintenances")
@AllArgsConstructor
@Validated
@Tag(name = "Maintenances", description = "Maintenance operations")
public class MaintenancesController
{
    private final MaintenanceService service;

    @GetMapping
    public List<GetAllMaintenanceResponse> getAll()
    {
        return service.getAll();
    }

    @GetMapping(path = "/{id}")
    public GetMaintenanceResponse getById(@PathVariable("id") @NotNull UUID id)
    {
        return service.getById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CreateMaintenanceResponse add(@Valid @RequestBody CreateMaintenanceRequest request) throws InterruptedException
    {
        return service.add(request);
    }

    @PutMapping(path = "/return")
    public GetMaintenanceResponse returnCarFromMaintenance(@RequestParam @NotNull UUID carId)
    {
        return service.returnCarFromMaintenance(carId);
    }

    @PutMapping(path = "/{id}")
    public UpdateMaintenanceResponse update(@PathVariable("id") @NotNull UUID id, @Valid @RequestBody UpdateMaintenanceRequest request)
    {
        return service.update(id, request);
    }

    @DeleteMapping(path="/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void remove(@PathVariable("id") @NotNull UUID id)
    {
        service.delete(id);
    }
}
