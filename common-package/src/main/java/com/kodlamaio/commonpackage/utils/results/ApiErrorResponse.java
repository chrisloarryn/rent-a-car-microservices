package com.kodlamaio.commonpackage.utils.results;

import java.time.LocalDateTime;

public class ApiErrorResponse
{
    private final LocalDateTime timestamp;
    private final int status;
    private final String error;
    private final String type;
    private final String message;
    private final Object details;
    private final String path;

    public ApiErrorResponse(int status, String error, String type, String message, Object details, String path)
    {
        this.timestamp = LocalDateTime.now();
        this.status = status;
        this.error = error;
        this.type = type;
        this.message = message;
        this.details = details;
        this.path = path;
    }

    public LocalDateTime getTimestamp()
    {
        return timestamp;
    }

    public int getStatus()
    {
        return status;
    }

    public String getError()
    {
        return error;
    }

    public String getType()
    {
        return type;
    }

    public String getMessage()
    {
        return message;
    }

    public Object getDetails()
    {
        return details;
    }

    public String getPath()
    {
        return path;
    }
}
