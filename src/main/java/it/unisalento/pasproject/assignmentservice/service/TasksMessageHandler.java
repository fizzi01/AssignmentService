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
    private final TaskRepository taskRepository;
    private final AllocationService allocationService;

    @Autowired
    public TasksMessageHandler(TaskRepository taskRepository, MessageProducer messageProducer, @Qualifier("RabbitMQProducer") MessageProducerStrategy strategy, AllocationService allocationService) {
        this.taskRepository = taskRepository;
        this.messageProducer = messageProducer;
        this.allocationService = allocationService;
        messageProducer.setStrategy(strategy);
    }

    /**
     * This method is called when a new task is created or updated and sent through the Topic
     * @param taskMessageDTO the task message
     */
    @RabbitListener(queues = "${rabbitmq.queue.newtask.name}")
    public void handleNewTask(TaskMessageDTO taskMessageDTO) {

        try {
            Optional<Task> task = taskRepository.findByIdTask(taskMessageDTO.getId());

            Task newTask = new Task();

            // If the task is already present in the database, update it
            if (task.isPresent()) {
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

            // If the task is forced to stop, deallocate all the resources without Watcher
            if (Boolean.FALSE.equals(newTask.getRunning())) {
                LOGGER.warn("Task {} is forced to stop", newTask.getId());
                newTask.setEndTime(LocalDateTime.now());
                deallocateAllResources(newTask);
            }

            taskRepository.save(newTask);
        }catch (Exception e){
            LOGGER.error("Error while saving task : {}", e.getMessage());
        }
    }

    private void deallocateAllResources(Task task) {
        //Dealloca tutte le risorse assegnate alla task
        allocationService.deallocateResources(allocationService.getActiveTaskAssignment(task.getId()));
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
