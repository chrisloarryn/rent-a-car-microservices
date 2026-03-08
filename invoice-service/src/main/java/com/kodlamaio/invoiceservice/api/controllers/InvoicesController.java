package com.kodlamaio.invoiceservice.api.controllers;

import com.kodlamaio.invoiceservice.business.abstracts.InvoiceService;
import com.kodlamaio.invoiceservice.business.dto.responses.get.GetAllInvoicesResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@AllArgsConstructor
@RequestMapping("/api/invoices")
@Tag(name = "Invoices", description = "Invoice query operations")
public class InvoicesController
{
    private final InvoiceService service;
    @GetMapping
    public List<GetAllInvoicesResponse> getAll()
    {
        return service.getAll();
    }
}
