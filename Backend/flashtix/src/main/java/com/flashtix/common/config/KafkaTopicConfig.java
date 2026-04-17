package com.flashtix.common.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {

    @Bean
    public NewTopic orderTopic() {
        return TopicBuilder.name("order_topic")
                .partitions(3) // Allows 3 consumers to process orders at the same time if we scale up
                .replicas(1)   // Since you are running locally, 1 replica is perfect
                .build();
    }
}