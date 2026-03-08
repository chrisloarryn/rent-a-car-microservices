package com.kodlamaio.inventoryservice.performance;

import java.time.Duration;

public final class GatlingSettings
{
    private GatlingSettings()
    {
    }

    public static String baseUrl()
    {
        return System.getProperty("gatling.baseUrl", "http://127.0.0.1:1205");
    }

    public static int users()
    {
        return Math.max(1, Integer.getInteger("gatling.users", 5));
    }

    public static Duration rampDuration()
    {
        return Duration.ofSeconds(Math.max(1, Integer.getInteger("gatling.rampSeconds", 5)));
    }

    public static Duration holdDuration()
    {
        return Duration.ofSeconds(Math.max(1, Integer.getInteger("gatling.holdSeconds", 10)));
    }
}
