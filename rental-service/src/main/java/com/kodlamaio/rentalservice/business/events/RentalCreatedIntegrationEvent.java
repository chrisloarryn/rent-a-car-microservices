package com.kodlamaio.rentalservice.business.events;

import com.kodlamaio.commonpackage.events.invoice.CreateInvoiceEvent;
import com.kodlamaio.commonpackage.events.rental.RentalCreatedEvent;

public record RentalCreatedIntegrationEvent(
        RentalCreatedEvent rentalCreatedEvent,
        CreateInvoiceEvent createInvoiceEvent)
{
}
