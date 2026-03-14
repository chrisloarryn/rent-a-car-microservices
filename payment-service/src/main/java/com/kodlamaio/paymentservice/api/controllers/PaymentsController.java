package com.kodlamaio.paymentservice.api.controllers;

import com.kodlamaio.commonpackage.utils.dto.responses.ClientResponse;
import com.kodlamaio.commonpackage.utils.dto.requests.CreateRentalPaymentRequest;
import com.kodlamaio.commonpackage.utils.constants.Roles;
import com.kodlamaio.commonpackage.utils.dto.responses.PageResponse;
import com.kodlamaio.paymentservice.business.abstracts.PaymentService;
import com.kodlamaio.paymentservice.business.dto.requests.create.CreatePaymentRequest;
import com.kodlamaio.paymentservice.business.dto.requests.update.UpdatePaymentRequest;
import com.kodlamaio.paymentservice.business.dto.responses.create.CreatePaymentResponse;
import com.kodlamaio.paymentservice.business.dto.responses.get.GetAllPaymentsResponse;
import com.kodlamaio.paymentservice.business.dto.responses.get.GetPaymentResponse;
import com.kodlamaio.paymentservice.business.dto.responses.update.UpdatePaymentResponse;
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
@RequestMapping("/api/payments")
@Validated
@Tag(name = "Payments", description = "Payment operations")
public class PaymentsController
{
    private final PaymentService service;

    @GetMapping
    @PreAuthorize(Roles.UserOrAbove)
    public PageResponse<GetAllPaymentsResponse> getAll(@PageableDefault(size = 20, sort = "id") Pageable pageable)
    {
        return service.getAll(pageable);
    }

    @GetMapping("/{id}")
    @PreAuthorize(Roles.UserOrAbove)
    public GetPaymentResponse getById(@PathVariable @NotNull UUID id)
    {
        return service.getById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize(Roles.AdminOrModerator)
    public CreatePaymentResponse add(@Valid @RequestBody CreatePaymentRequest request)
    {
        return service.add(request);
    }

    @PutMapping("/{id}")
    @PreAuthorize(Roles.AdminOrModerator)
    public UpdatePaymentResponse update(@PathVariable @NotNull UUID id, @Valid @RequestBody UpdatePaymentRequest request)
    {
        return service.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize(Roles.AdminOrModerator)
    public void delete(@PathVariable @NotNull UUID id)
    {
        service.delete(id);
    }

    @Deprecated(since = "v2", forRemoval = false)
    @PostMapping("/process-rental-payment")
    @PreAuthorize(Roles.AdminOrModerator)
    public ClientResponse processRentalPayment(@Valid @RequestBody CreateRentalPaymentRequest request)
    {
        return service.processRentalPayment(request);
    }
}
