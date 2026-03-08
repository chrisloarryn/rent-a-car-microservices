package com.kodlamaio.commonpackage.utils.dto.responses;

import java.time.OffsetDateTime;

public class SystemPingResponse
{
    private final String service;
    private final String status;
    private final OffsetDateTime timestamp;

    public SystemPingResponse(String service)
    {
        this.service = service;
        this.status = "UP";
        this.timestamp = OffsetDateTime.now();
    }

    public String getService()
    {
        return service;
    }

    public String getStatus()
    {
        return status;
    }

    public OffsetDateTime getTimestamp()
    {
        return timestamp;
    }
}
