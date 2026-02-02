package com.chaars.order.messaging;

import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.DefaultJackson2JavaTypeMapper;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class RabbitCommonConfig {

    @Bean
    public TopicExchange eventsExchange() {
        return new TopicExchange(RabbitNames.EXCHANGE, true, false);
    }

    @Bean
    public MessageConverter jsonMessageConverter(){
        Jackson2JsonMessageConverter converter = new Jackson2JsonMessageConverter();
        DefaultJackson2JavaTypeMapper typeMapper = new DefaultJackson2JavaTypeMapper();
        typeMapper.setTrustedPackages("com.chaars.*");

        Map<String, Class<?>> idClassMapping = new HashMap<>();
        idClassMapping.put("com.chaars.payment.messaging.events.PaymentAuthorizedEvent", com.chaars.order.messaging.events.PaymentAuthorizedEvent.class);
        idClassMapping.put("com.chaars.payment.messaging.events.PaymentRejectedEvent", com.chaars.order.messaging.events.PaymentRejectedEvent.class);
        idClassMapping.put("com.chaars.inventory.messaging.events.InventoryReservedEvent", com.chaars.order.messaging.events.InventoryReservedEvent.class);
        idClassMapping.put("com.chaars.inventory.messaging.events.InventoryFailedEvent", com.chaars.order.messaging.events.InventoryFailedEvent.class);

        typeMapper.setIdClassMapping(idClassMapping);
        converter.setJavaTypeMapper(typeMapper);
        return converter;
    }
}
