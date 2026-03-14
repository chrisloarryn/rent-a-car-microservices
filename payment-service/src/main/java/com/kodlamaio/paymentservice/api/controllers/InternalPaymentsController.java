package com.kodlamaio.paymentservice.api.controllers;

import com.kodlamaio.commonpackage.utils.dto.requests.CreateRentalPaymentRequest;
import com.kodlamaio.commonpackage.utils.dto.responses.ClientResponse;
import com.kodlamaio.paymentservice.business.abstracts.PaymentService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
@Validated
@RequestMapping("/api/internal/payments")
@Tag(name = "Internal Payments", description = "Service-to-service payment operations")
public class InternalPaymentsController
{
    private final PaymentService service;

    @PostMapping("/rental-processing")
    public ClientResponse processRentalPayment(@Valid @RequestBody CreateRentalPaymentRequest request)
    {
        return service.processRentalPayment(request);
    }
}
