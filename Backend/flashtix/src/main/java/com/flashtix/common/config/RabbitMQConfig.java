package com.flashtix.common.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    // Queue, exchange, and routing key constants used across the application
    public static final String QUEUE = "pdf_generation_queue";
    public static final String EXCHANGE = "ticketing_exchange";
    public static final String ROUTING_KEY = "pdf_routing_key";

    @Bean
    public Queue pdfGenerationQueue() {
        // durable = true: queue survives a RabbitMQ restart
        return QueueBuilder.durable(QUEUE).build();
    }

    @Bean
    public TopicExchange ticketingExchange() {
        return ExchangeBuilder.topicExchange(EXCHANGE).durable(true).build();
    }

    @Bean
    public Binding pdfQueueBinding(Queue pdfGenerationQueue, TopicExchange ticketingExchange) {
        return BindingBuilder.bind(pdfGenerationQueue).to(ticketingExchange).with(ROUTING_KEY);
    }

    /**
     * Configures Jackson JSON serialization for RabbitMQ messages.
     * Without this, Spring uses Java serialization which is fragile and version-sensitive.
     */
    @Bean
    public MessageConverter jacksonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    /**
     * FIX: Return type changed from AmqpTemplate to RabbitTemplate.
     * OrderConsumer injects RabbitTemplate directly — returning the concrete type
     * ensures Spring autowires without ambiguity and enables RabbitTemplate-specific features.
     */
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(jacksonMessageConverter());
        return rabbitTemplate;
    }
}