package com.wisewallet.account.infrastructure.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaConfig {

    @Bean
    public NewTopic accountCreatedTopic() {
        return TopicBuilder.name("account.created").partitions(1).replicas(1).build();
    }

    @Bean
    public NewTopic accountBalanceLowTopic() {
        return TopicBuilder.name("account.balance-low").partitions(1).replicas(1).build();
    }
}
