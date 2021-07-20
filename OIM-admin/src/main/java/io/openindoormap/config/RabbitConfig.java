package io.openindoormap.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.openindoormap.security.Crypt;

@Configuration
public class RabbitConfig {
    @Autowired
    private PropertiesConfig propertiesConfig;

    @Bean
    Queue queue() {
        return new Queue(propertiesConfig.getQueueName(), true);
    }

    @Bean
    TopicExchange exchange() {
        return new TopicExchange(propertiesConfig.getExchange());
    }

    @Bean
    Binding binding(Queue queue, TopicExchange topicExchange) {
        return BindingBuilder.bind(queue).to(topicExchange).with(propertiesConfig.getQueueName());
    }

    @Bean
    ConnectionFactory connectionFactory() {
        CachingConnectionFactory connectionFactory = new CachingConnectionFactory(propertiesConfig.getQueueServerHost());
        connectionFactory.setUsername(Crypt.decrypt(propertiesConfig.getQueueUser()));
        connectionFactory.setPassword(Crypt.decrypt(propertiesConfig.getQueuePassword()));
        connectionFactory.setPort(Integer.parseInt(propertiesConfig.getQueueServerPort()));
        return connectionFactory;
    }

    @Bean
    RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setRoutingKey(propertiesConfig.getQueueName());
        rabbitTemplate.setMessageConverter(new Jackson2JsonMessageConverter());
        return rabbitTemplate;
    }

//    @Bean
//    MessageConverter messageConverter() {
//        return new Jackson2JsonMessageConverter();
//    }
}
