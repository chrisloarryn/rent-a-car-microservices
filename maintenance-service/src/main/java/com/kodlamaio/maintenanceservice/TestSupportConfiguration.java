package com.kodlamaio.maintenanceservice;

import com.kodlamaio.commonpackage.utils.dto.responses.ClientResponse;
import com.kodlamaio.maintenanceservice.api.clients.CarClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile({"test", "gatling"})
public class TestSupportConfiguration
{
    @Bean
    @Primary
    CarClient carClient()
    {
        return carId -> new ClientResponse(true, null);
    }
}
