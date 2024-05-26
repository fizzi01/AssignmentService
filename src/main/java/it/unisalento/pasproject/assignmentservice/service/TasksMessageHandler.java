package it.unisalento.pasproject.assignmentservice.service;

import it.unisalento.pasproject.assignmentservice.business.io.exchanger.MessageExchangeStrategy;
import it.unisalento.pasproject.assignmentservice.business.io.exchanger.MessageExchanger;
import it.unisalento.pasproject.assignmentservice.dto.MessageDTO;
import it.unisalento.pasproject.assignmentservice.domain.Task;
import it.unisalento.pasproject.assignmentservice.dto.TaskMessageDTO;
import it.unisalento.pasproject.assignmentservice.dto.TaskStatusMessageDTO;
import it.unisalento.pasproject.assignmentservice.repositories.TaskRepository;
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

    @Value("${rabbitmq.exchange.notification.name}")
    private String notificationExchange;

    @Value("${rabbitmq.routing.notification.key}")
    private String notificationTopic;


    private final MessageExchanger messageExchanger;
    private final TaskRepository taskRepository;

    @Autowired
    public TasksMessageHandler(TaskRepository taskRepository, MessageExchanger messageExchanger, @Qualifier("RabbitMQExchange")MessageExchangeStrategy strategy) {
        this.taskRepository = taskRepository;
        this.messageExchanger = messageExchanger;
        messageExchanger.setStrategy(strategy);
    }

    /**
     * This method is called when a new task is created or updated and sent through the Topic
     * @param taskMessageDTO the task message
     */
    @RabbitListener(queues = "${rabbitmq.queue.newtask.name}")
    public void handleNewTask(TaskMessageDTO taskMessageDTO) {

        Optional<Task> task = Optional.ofNullable(taskRepository.findByIdTask(taskMessageDTO.getId()));

        Task newTask = new Task();

        // If the task is already present in the database, update it
        if(task.isPresent()) {
            newTask = task.get();
        }

        //Non viene controllato se manca qualche campo, perchè il TaskManager dovrebbe inviare sempre tutti i campi
        //anche quando la task viene aggiornata. SOLO DATA CONSITENCY

        newTask.setIdTask(taskMessageDTO.getId());
        newTask.setEmailUtente(taskMessageDTO.getEmailUtente());
        newTask.setMaxComputingPower(taskMessageDTO.getMaxComputingPower());
        newTask.setMaxCudaPower(taskMessageDTO.getMaxCudaPower());
        newTask.setMinCudaPower(taskMessageDTO.getMinCudaPower());
        newTask.setMinComputingPower(taskMessageDTO.getMinComputingPower());
        newTask.setTaskDuration(taskMessageDTO.getTaskDuration());
        newTask.setMaxEnergyConsumption(taskMessageDTO.getMaxEnergyConsumption());
        newTask.setMinEnergyConsumption(taskMessageDTO.getMinEnergyConsumption());
        newTask.setMinWorkingTime(taskMessageDTO.getMinWorkingTime());
        newTask.setRunning(taskMessageDTO.getRunning());
        newTask.setEnabled(taskMessageDTO.getEnabled());

        if (newTask.getEnabled() && newTask.getRunning()) {
            if (newTask.getStartTime() == null) {
                newTask.setStartTime(LocalDateTime.now());
                newTask.setEndTime(newTask.getStartTime().plusSeconds(newTask.getTaskDuration().longValue()));
            }
        } else {
            newTask.setStartTime(null);
        }

        taskRepository.save(newTask);
    }



    /**
     * This method is called when a user is assigned to a task
     * @param message the message containing the task id and the list of users assigned
     */
    public void handleTaskAssignment(TaskStatusMessageDTO message) {
       MessageDTO result =  messageExchanger.exchangeMessage(message, taskAssingmentTopic, dataExchange, MessageDTO.class);
       if(result.getCode() != 200) {
           throw new RuntimeException("Error in sending the message");
       }
    }

    /**
     * This method is called when a task execution is stopped
     * @param message the message containing the task id and the running status
     */
    public void endTaskExecution(TaskStatusMessageDTO message) {
        MessageDTO result =  messageExchanger.exchangeMessage(message, taskExecutionTopic, dataExchange, MessageDTO.class);
        if(result.getCode() != 200) {
            throw new RuntimeException("Error in sending the message");
        }
    }


}
