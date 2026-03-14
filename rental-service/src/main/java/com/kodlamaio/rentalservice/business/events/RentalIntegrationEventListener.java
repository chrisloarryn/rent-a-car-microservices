package com.kodlamaio.rentalservice.business.events;

import com.kodlamaio.commonpackage.events.rental.RentalDeletedEvent;
import com.kodlamaio.commonpackage.utils.kafka.producer.KafkaProducer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class RentalIntegrationEventListener
{
    private final KafkaProducer producer;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void handle(RentalCreatedIntegrationEvent event)
    {
        producer.sendMessage(event.rentalCreatedEvent(), "rental-created");
        producer.sendMessage(event.createInvoiceEvent(), "create-invoice");
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void handle(RentalDeletedEvent event)
    {
        producer.sendMessage(event, "rental-deleted");
    }
}
