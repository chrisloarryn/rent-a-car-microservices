package com.kodlamaio.commonpackage.events;

import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
public abstract class BaseEvent implements Event
{
    private UUID eventId = UUID.randomUUID();
    private OffsetDateTime occurredAt = OffsetDateTime.now();
    private int schemaVersion = 1;
    private String correlationId = eventId.toString();
}
