package com.kodlamaio.inventoryservice.api.controllers;

import com.kodlamaio.commonpackage.utils.dto.responses.CarClientResponse;
import com.kodlamaio.commonpackage.utils.dto.responses.ClientResponse;
import com.kodlamaio.inventoryservice.business.abstracts.CarService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@AllArgsConstructor
@Validated
@RequestMapping("/api/internal/cars")
@Tag(name = "Internal Cars", description = "Service-to-service inventory operations")
public class InternalCarsController
{
    private final CarService service;

    @GetMapping("/{carId}/availability")
    public ClientResponse checkIfCarAvailable(@PathVariable @NotNull UUID carId)
    {
        return service.checkIfCarAvailable(carId);
    }

    @GetMapping("/{carId}/invoice-details")
    public CarClientResponse getCarForInvoice(@PathVariable @NotNull UUID carId)
    {
        return service.getCarForInvoice(carId);
    }
}
