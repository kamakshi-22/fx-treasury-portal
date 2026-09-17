package com.fxtreasury.valuation.config;

import org.apache.kafka.common.TopicPartition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaOperations;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

@Configuration
public class KafkaConsumerConfig {

    private static final Logger log = LoggerFactory.getLogger(KafkaConsumerConfig.class);

    @Bean
    public DefaultErrorHandler errorHandler(KafkaOperations<Object, Object> template) {
        // Publish poison pill to fx.trades.DLT after 2 failed attempts
        DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(template,
                (record, ex) -> {
                    log.error("Routing failed message to DLT: Topic={}, Offset={}, Exception={}",
                            record.topic(), record.offset(), ex.getMessage());
                    return new TopicPartition("fx.trades.DLT", record.partition());
                });

        // 2 attempts, spaced 1 second apart, before invoking recoverer
        return new DefaultErrorHandler(recoverer, new FixedBackOff(1000L, 2));
    }
}