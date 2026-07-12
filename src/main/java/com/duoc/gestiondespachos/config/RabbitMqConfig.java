package com.duoc.gestiondespachos.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.ExchangeBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.boot.ApplicationRunner;

import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;

@Configuration
public class RabbitMqConfig {

    public static final String COLA_GUIAS = "guias.procesamiento";
    public static final String COLA_ERRORES = "guias.errores";

    public static final String EXCHANGE_GUIAS = "guias.exchange";
    public static final String EXCHANGE_ERRORES = "guias.dlx";

    public static final String ROUTING_GUIAS = "guias.creada";
    public static final String ROUTING_ERRORES = "guias.error";

    @Bean
    public DirectExchange exchangeGuias() {
        return ExchangeBuilder
                .directExchange(EXCHANGE_GUIAS)
                .durable(true)
                .build();
    }

    @Bean
    public DirectExchange exchangeErrores() {
        return ExchangeBuilder
                .directExchange(EXCHANGE_ERRORES)
                .durable(true)
                .build();
    }

    @Bean
    public Queue colaGuias() {
        return QueueBuilder
                .durable(COLA_GUIAS)
                .withArgument("x-dead-letter-exchange", EXCHANGE_ERRORES)
                .withArgument("x-dead-letter-routing-key", ROUTING_ERRORES)
                .build();
    }

    @Bean
    public Queue colaErrores() {
        return QueueBuilder
                .durable(COLA_ERRORES)
                .build();
    }

    @Bean
    public Binding bindingColaGuias(
            @Qualifier("colaGuias") Queue colaGuias,
            @Qualifier("exchangeGuias") DirectExchange exchangeGuias) {

        return BindingBuilder
                .bind(colaGuias)
                .to(exchangeGuias)
                .with(ROUTING_GUIAS);
    }

    @Bean
    public Binding bindingColaErrores(
            @Qualifier("colaErrores") Queue colaErrores,
            @Qualifier("exchangeErrores") DirectExchange exchangeErrores) {

        return BindingBuilder
                .bind(colaErrores)
                .to(exchangeErrores)
                .with(ROUTING_ERRORES);
    }

    @Bean
    public RabbitAdmin rabbitAdmin(ConnectionFactory connectionFactory) {
        RabbitAdmin rabbitAdmin = new RabbitAdmin(connectionFactory);
        rabbitAdmin.setAutoStartup(true);
        return rabbitAdmin;
    }

    @Bean
    public ApplicationRunner inicializarRabbitMq(RabbitAdmin rabbitAdmin) {
        return args -> rabbitAdmin.initialize();
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new JacksonJsonMessageConverter(
                "com.duoc.gestiondespachos.dto"
        );
    }
}