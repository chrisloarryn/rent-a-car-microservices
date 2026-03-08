package com.kodlamaio.commonpackage.configuration.kafka.producer;

import com.kodlamaio.commonpackage.events.Event;
import com.kodlamaio.commonpackage.utils.kafka.producer.KafkaProducer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile({"test", "gatling"})
public class TestKafkaProducerConfig
{
    @Bean
    KafkaProducer getKafkaProducer()
    {
        return new KafkaProducer(null)
        {
            @Override
            public <T extends Event> void sendMessage(T event, String topic)
            {
                // No-op under test profiles to keep HTTP suites self-contained.
            }
        };
    }
}
