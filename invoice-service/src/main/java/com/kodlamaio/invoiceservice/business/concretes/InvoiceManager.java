package com.kodlamaio.invoiceservice.business.concretes;

import com.kodlamaio.commonpackage.utils.mappers.ModelMapperService;
import com.kodlamaio.commonpackage.utils.dto.responses.PageResponse;
import com.kodlamaio.invoiceservice.business.abstracts.InvoiceService;
import com.kodlamaio.invoiceservice.business.dto.responses.get.GetAllInvoicesResponse;
import com.kodlamaio.invoiceservice.business.dto.responses.get.GetInvoiceResponse;
import com.kodlamaio.invoiceservice.business.rules.InvoiceBusinessRules;
import com.kodlamaio.invoiceservice.entities.Invoice;
import com.kodlamaio.invoiceservice.repository.InvoiceRepository;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service //once time of creation?
@AllArgsConstructor
public class InvoiceManager implements InvoiceService
{
    private final InvoiceRepository repository;
    private ModelMapperService mapper;
    private final InvoiceBusinessRules rules;

    @Override
    public PageResponse<GetAllInvoicesResponse> getAll(Pageable pageable)
    {
        var invoices = repository.findAll(pageable)
                .map(invoice -> mapper.forResponse().map(invoice, GetAllInvoicesResponse.class));
        return PageResponse.from(invoices);
    }

    @Override
    public GetInvoiceResponse getById(UUID id)
    {
        rules.checkIfInvoiceExists(id);
        Invoice invoice = repository.findById(id).get();
        GetInvoiceResponse response = mapper.forResponse().map(invoice, GetInvoiceResponse.class);
        return response;
    }

    @Override
    public void add(Invoice invoice)
    {
        if (invoice.getId() == null) {
            invoice.setId(UUID.randomUUID());
        }
        repository.save(invoice);
    }
}
