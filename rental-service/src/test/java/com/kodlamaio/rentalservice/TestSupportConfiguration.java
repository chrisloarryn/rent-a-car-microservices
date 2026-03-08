package com.kodlamaio.rentalservice;

import com.kodlamaio.commonpackage.utils.dto.requests.CreateRentalPaymentRequest;
import com.kodlamaio.commonpackage.utils.dto.responses.CarClientResponse;
import com.kodlamaio.commonpackage.utils.dto.responses.ClientResponse;
import com.kodlamaio.rentalservice.api.clients.CarClient;
import com.kodlamaio.rentalservice.api.clients.PaymentClient;
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
        return new CarClient()
        {
            @Override
            public ClientResponse checkIfCarAvailable(java.util.UUID carId)
            {
                return new ClientResponse(true, null);
            }

            @Override
            public CarClientResponse getCar(java.util.UUID carId)
            {
                CarClientResponse response = new CarClientResponse("Model S", "Tesla", "34 ABC 123", 2024);
                response.setSuccess(true);
                return response;
            }
        };
    }

    @Bean
    @Primary
    PaymentClient paymentClient()
    {
        return (CreateRentalPaymentRequest request) -> new ClientResponse(true, null);
    }
}
