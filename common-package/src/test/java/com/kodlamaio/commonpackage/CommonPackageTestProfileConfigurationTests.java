package com.kodlamaio.commonpackage;

import com.kodlamaio.commonpackage.configuration.security.TestSecurityConfig;
import com.kodlamaio.commonpackage.events.inventory.BrandDeletedEvent;
import com.kodlamaio.commonpackage.utils.kafka.producer.KafkaProducer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.ActiveProfiles;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(
        classes = {
                CommonPackageApplication.class,
                TestSecurityConfig.class
        },
        webEnvironment = SpringBootTest.WebEnvironment.MOCK,
        properties = {
                "spring.cloud.config.enabled=false",
                "eureka.client.enabled=false"
        })
@ActiveProfiles("test")
class CommonPackageTestProfileConfigurationTests
{
    @Autowired
    private SecurityFilterChain securityFilterChain;

    @Autowired
    private KafkaProducer kafkaProducer;

    @Test
    void testProfileBeansAreAvailableAndNoOpProducerAcceptsEvents()
    {
        assertNotNull(securityFilterChain);
        assertNotNull(kafkaProducer);

        kafkaProducer.sendMessage(new BrandDeletedEvent(UUID.randomUUID()), "inventory-brand-deleted");
    }
}
