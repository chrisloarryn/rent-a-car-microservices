package com.kodlamaio.inventoryservice.api.controllers;

import com.kodlamaio.commonpackage.utils.constants.Roles;
import com.kodlamaio.commonpackage.utils.dto.responses.CarClientResponse;
import com.kodlamaio.commonpackage.utils.dto.responses.ClientResponse;
import com.kodlamaio.commonpackage.utils.dto.responses.PageResponse;
import com.kodlamaio.inventoryservice.business.abstracts.CarService;
import com.kodlamaio.inventoryservice.business.dto.requests.create.CreateCarRequest;
import com.kodlamaio.inventoryservice.business.dto.requests.update.UpdateCarRequest;
import com.kodlamaio.inventoryservice.business.dto.responses.create.CreateCarResponse;
import com.kodlamaio.inventoryservice.business.dto.responses.get.GetAllCarsResponse;
import com.kodlamaio.inventoryservice.business.dto.responses.get.GetCarResponse;
import com.kodlamaio.inventoryservice.business.dto.responses.update.UpdateCarResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@AllArgsConstructor
@RequestMapping("/api/cars")
@Validated
@Tag(name = "Cars", description = "Inventory car operations")
public class CarsController {
    private final CarService service;

    @GetMapping
    @PreAuthorize(Roles.UserOrAbove)
    public PageResponse<GetAllCarsResponse> getAll(@PageableDefault(size = 20, sort = "id") Pageable pageable) {
        return service.getAll(pageable);
    }

    @GetMapping("/{id}")
    @PreAuthorize(Roles.UserOrAbove)
    public GetCarResponse getById(@PathVariable @NotNull UUID id) {
        return service.getById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize(Roles.AdminOrModerator)
    public CreateCarResponse add(@Valid @RequestBody CreateCarRequest request) {
        return service.add(request);
    }

    @PutMapping("/{id}")
    @PreAuthorize(Roles.AdminOrModerator)
    public UpdateCarResponse update(@PathVariable @NotNull UUID id, @Valid @RequestBody UpdateCarRequest request) {
        return service.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize(Roles.AdminOrModerator)
    public void delete(@PathVariable @NotNull UUID id) {
        service.delete(id);
    }

    @Deprecated(since = "v2", forRemoval = false)
    @GetMapping("/check-car-available/{id}")
    @PreAuthorize(Roles.AdminOrModerator)
    public ClientResponse checkIfCarAvailable(@PathVariable @NotNull UUID id)
    {
         return service.checkIfCarAvailable(id);
    }

    @Deprecated(since = "v2", forRemoval = false)
    @GetMapping("/get-car-for-invoice/{carId}")
    @PreAuthorize(Roles.AdminOrModerator)
    public CarClientResponse getCarForInvoice(@PathVariable @NotNull UUID carId)
    {
        return service.getCarForInvoice(carId);
    }
}
