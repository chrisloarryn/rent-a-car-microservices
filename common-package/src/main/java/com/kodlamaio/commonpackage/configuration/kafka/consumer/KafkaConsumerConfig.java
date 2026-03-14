package com.kodlamaio.commonpackage.configuration.kafka.consumer;

import org.apache.kafka.common.TopicPartition;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.core.KafkaOperations;
import org.springframework.kafka.listener.CommonErrorHandler;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.util.backoff.ExponentialBackOff;

@Configuration
@Profile("!test & !gatling")
@ConditionalOnBean(KafkaOperations.class)
public class KafkaConsumerConfig
{
    @Bean
    CommonErrorHandler kafkaCommonErrorHandler(KafkaOperations<Object, Object> kafkaOperations)
    {
        DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(
                kafkaOperations,
                (record, exception) -> new TopicPartition(record.topic() + ".dlq", record.partition()));

        ExponentialBackOff backOff = new ExponentialBackOff(1_000L, 2.0d);
        backOff.setMaxInterval(5_000L);
        backOff.setMaxElapsedTime(15_000L);

        DefaultErrorHandler errorHandler = new DefaultErrorHandler(recoverer, backOff);
        errorHandler.setCommitRecovered(true);
        return errorHandler;
    }
}
