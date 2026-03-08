package karate;

import com.intuit.karate.junit5.Karate;
import com.kodlamaio.filterservice.FilterServiceApplication;
import com.kodlamaio.filterservice.entities.Filter;
import com.kodlamaio.filterservice.repository.FilterRepository;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Arrays;
import java.util.UUID;

@SpringBootTest(
        classes = FilterServiceApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "spring.config.import=optional:configserver:http://localhost:8888",
                "spring.cloud.config.fail-fast=false",
                "eureka.client.enabled=false"
        })
@ActiveProfiles("test")
@Tag("karate")
@Testcontainers
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ApiContractsKarateTest
{
    private static final MongoDBContainer MONGO_DB_CONTAINER = new MongoDBContainer("mongo:8.0");

    static
    {
        MONGO_DB_CONTAINER.start();
    }

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry)
    {
        registry.add("spring.data.mongodb.uri", MONGO_DB_CONTAINER::getReplicaSetUrl);
    }

    @LocalServerPort
    private int port;

    @Autowired
    private FilterRepository repository;

    @BeforeAll
    void configureBaseUrlAndSeedData()
    {
        repository.deleteAll();

        Filter filter = new Filter(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                "Model 3",
                "Tesla",
                "34 ABC 123",
                2024,
                175.0,
                "AVAILABLE");
        repository.save(filter);

        System.setProperty("karate.baseUrl", "http://127.0.0.1:" + port);
        System.setProperty("karate.seedId", filter.getId().toString());
        System.setProperty("karate.seedPlate", filter.getPlate());
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
