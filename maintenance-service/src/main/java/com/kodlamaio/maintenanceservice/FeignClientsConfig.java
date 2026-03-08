package com.kodlamaio.maintenanceservice;

import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@EnableFeignClients
@Profile("!test & !gatling")
public class FeignClientsConfig
{
}
