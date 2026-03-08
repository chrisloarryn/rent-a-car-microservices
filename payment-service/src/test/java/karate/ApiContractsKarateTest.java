package karate;

import com.intuit.karate.junit5.Karate;
import com.kodlamaio.paymentservice.PaymentServiceApplication;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.TestInstance;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

import java.util.Arrays;

@SpringBootTest(
        classes = PaymentServiceApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "spring.config.import=optional:configserver:http://localhost:8888",
                "spring.cloud.config.fail-fast=false",
                "eureka.client.enabled=false"
        })
@ActiveProfiles("test")
@Tag("karate")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ApiContractsKarateTest
{
    @LocalServerPort
    private int port;

    @BeforeAll
    void configureBaseUrl()
    {
        System.setProperty("karate.baseUrl", "http://127.0.0.1:" + port);
    }

    @Karate.Test
    Karate contracts()
    {
        Karate runner = new Karate().path("classpath:karate/contracts");
        String configuredTags = System.getProperty("karate.tags");
        if (configuredTags == null || configuredTags.isBlank()) {
            return runner.tags("@regression");
        }
        return runner.tags(Arrays.stream(configuredTags.split(","))
                .map(String::trim)
                .filter(tag -> !tag.isBlank())
                .toArray(String[]::new));
    }
}
