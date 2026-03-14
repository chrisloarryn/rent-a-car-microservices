package com.kodlamaio.rentalservice.api.clients;

import com.kodlamaio.commonpackage.utils.dto.responses.CarClientResponse;
import com.kodlamaio.commonpackage.utils.dto.responses.ClientResponse;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@FeignClient(name = "inventory-service",
        fallback = CarClientFallback.class) //optional name,,,, fallback = CarClientFallback.class
@Retry(name="rentalServiceCallsInventory")
public interface CarClient
{
    @GetMapping(value = "/api/internal/cars/{carId}/availability")
    ClientResponse checkIfCarAvailable(@PathVariable UUID carId) throws InterruptedException;

    @GetMapping(value = "/api/internal/cars/{carId}/invoice-details")
    CarClientResponse getCar(@PathVariable UUID carId) throws InterruptedException;
}
