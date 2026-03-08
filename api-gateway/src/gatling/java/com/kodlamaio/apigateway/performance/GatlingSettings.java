package com.kodlamaio.apigateway.performance;

import java.time.Duration;

public final class GatlingSettings
{
    private GatlingSettings()
    {
    }

    public static String baseUrl()
    {
        return System.getProperty("gatling.baseUrl", "http://127.0.0.1:1201");
    }

    public static int users()
    {
        return positiveIntProperty("gatling.users", 5);
    }

    public static Duration rampDuration()
    {
        return Duration.ofSeconds(positiveIntProperty("gatling.rampSeconds", 5));
    }

    public static Duration holdDuration()
    {
        return Duration.ofSeconds(positiveIntProperty("gatling.holdSeconds", 10));
    }

    private static int positiveIntProperty(String name, int defaultValue)
    {
        return Math.max(1, Integer.getInteger(name, defaultValue));
    }
}
