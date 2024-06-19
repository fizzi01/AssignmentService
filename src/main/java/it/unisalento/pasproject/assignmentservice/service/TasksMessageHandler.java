package it.unisalento.pasproject.assignmentservice.service;

import it.unisalento.pasproject.assignmentservice.business.io.producer.MessageProducer;
import it.unisalento.pasproject.assignmentservice.business.io.producer.MessageProducerStrategy;
import it.unisalento.pasproject.assignmentservice.domain.Task;
import it.unisalento.pasproject.assignmentservice.dto.task.TaskMessageDTO;
import it.unisalento.pasproject.assignmentservice.dto.task.TaskStatusMessageDTO;
import it.unisalento.pasproject.assignmentservice.repositories.TaskRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class TasksMessageHandler {
    // It receive new Tasks created, with all their infos, and stores them in the database
    // Deve sincronizzare le informazioni con il TaskManager, quindi si devono inviare messaggi
    // al TaskManager per aggiornare le informazioni delle task e viceversa

    //Deve anche notificare il TaskManager dell'assegnazione degli utenti alla task
    //Deve anche notificare il TaskManager della rimozione degli utenti dalla task

    @Value("${rabbitmq.routing.taskassignment.key}")
    private String taskAssingmentTopic;

    @Value("${rabbitmq.routing.taskexecution.key}")
    private String taskExecutionTopic;

    @Value("${rabbitmq.exchange.data.name}")
    private String dataExchange;

    private static final Logger LOGGER = LoggerFactory.getLogger(TasksMessageHandler.class);


    private final MessageProducer messageProducer;

    @Autowired
    public TasksMessageHandler(TaskRepository taskRepository, MessageProducer messageProducer, @Qualifier("RabbitMQProducer") MessageProducerStrategy strategy, AllocationService allocationService) {
        this.messageProducer = messageProducer;
        messageProducer.setStrategy(strategy);
    }


    /**
     * This method is called when a user is assigned to a task
     * @param message the message containing the task id and the list of users assigned
     */
    public void handleTaskAssignment(TaskStatusMessageDTO message) {
       messageProducer.sendMessage(message, taskAssingmentTopic, dataExchange);
    }

    /**
     * This method is called when a task execution is stopped
     * @param message the message containing the task id and the running status
     */
    public void endTaskExecution(TaskStatusMessageDTO message) {
        messageProducer.sendMessage(message, taskExecutionTopic, dataExchange);
    }


}
