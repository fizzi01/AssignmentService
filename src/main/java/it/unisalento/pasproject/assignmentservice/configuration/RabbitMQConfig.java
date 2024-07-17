package it.unisalento.pasproject.assignmentservice.configuration;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {
    // ------  TASK MESSAGES  ------ //

    @Value("${rabbitmq.queue.newtask.name}")
    private String newTaskQueue;

    @Value("${rabbitmq.routing.newtask.key}")
    private String newTaskTopic;

    @Value("${rabbitmq.queue.taskexecution.name}")
    private String taskExecutionQueue;

    @Value("${rabbitmq.routing.taskexecution.key}")
    private String taskExecutionTopic;

    // Others queue and topics for task messages ...

    @Value("${rabbitmq.exchange.data.name}")
    private String dataExchange;

    @Bean
    public Queue newTaskQueue() {
        return new Queue(newTaskQueue);
    }

    @Bean
    public Queue taskExecutionQueue() {
        return new Queue(taskExecutionQueue);
    }

    @Bean
    public TopicExchange dataExchange() {
        return new TopicExchange(dataExchange);
    }

    @Bean
    public Binding newTaskBinding() {
        return BindingBuilder
                .bind(newTaskQueue())
                .to(dataExchange())
                .with(newTaskTopic);
    }

    @Bean
    public Binding taskExecutionBinding() {
        return BindingBuilder
                .bind(taskExecutionQueue())
                .to(dataExchange())
                .with(taskExecutionTopic);
    }

    // ------  END TASK MESSAGES  ------ //

    // ------  RESOURCE MESSAGES  ------ //
    @Value("${rabbitmq.queue.newresource.name}")
    private String newResourceQueue;

    @Value("${rabbitmq.routing.newresource.key}")
    private String newResourceTopic;

    // Others queues and topics for resource messages

    @Value("${rabbitmq.exchange.resource.name}")
    private String resourceDataExchange;

    @Bean
    public Queue newResourceQueue() {
        return new Queue(newResourceQueue);
    }

    @Bean
    public TopicExchange resourceDataExchange() {
        return new TopicExchange(resourceDataExchange);
    }

    @Bean
    public Binding newResourceBinding() {
        return BindingBuilder
                .bind(newResourceQueue())
                .to(resourceDataExchange())
                .with(newResourceTopic);
    }

    // ------  END RESOURCE MESSAGES  ------ //

    // ------ RESOURCE CHECKOUT MESSAGES ------ //

    /*@Value("${rabbitmq.exchange.transaction.name}")
    private String transactionExchange;

    @Bean
    public TopicExchange transactionExchange() {
        return new TopicExchange(transactionExchange);
    }*/



    /**
     * Creates a message converter for JSON messages.
     *
     * @return a new Jackson2JsonMessageConverter instance.
     */
    @Bean
    public MessageConverter converter() {
        return new Jackson2JsonMessageConverter();
    }

    /**
     * Creates an AMQP template for sending messages.
     *
     * @param connectionFactory the connection factory to use.
     * @return a new RabbitTemplate instance.
     */
    @Bean
    public AmqpTemplate amqpTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(converter());
        return rabbitTemplate;
    }
}
