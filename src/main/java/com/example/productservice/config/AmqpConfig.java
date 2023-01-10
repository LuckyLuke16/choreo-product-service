package com.example.productservice.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class AmqpConfig {

    static final String topicExchangeName = "choreography-exchange";

    static final String updateStockQueue = "update-stock-queue";

    @Bean
    Queue updateStockQueue() {
        return new Queue(updateStockQueue, true);
    }

    @Bean
    TopicExchange exchange() {
        return new TopicExchange(topicExchangeName);
    }

    @Bean
    Binding addCartItemBinding(Queue addQueue, TopicExchange exchange) {
        return BindingBuilder.bind(addQueue).to(exchange).with("order.items.queried");
    }

    @Bean
    public Jackson2JsonMessageConverter jackson2MessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
