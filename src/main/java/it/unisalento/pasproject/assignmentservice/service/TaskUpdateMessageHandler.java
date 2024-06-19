package it.unisalento.pasproject.assignmentservice.service;

import it.unisalento.pasproject.assignmentservice.domain.Task;
import it.unisalento.pasproject.assignmentservice.dto.task.TaskMessageDTO;
import it.unisalento.pasproject.assignmentservice.repositories.TaskRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class TaskUpdateMessageHandler {


    private static final Logger LOGGER = LoggerFactory.getLogger(TaskUpdateMessageHandler.class);

    private final AllocationService allocationService;
    private final TaskRepository taskRepository;

    @Autowired
    public TaskUpdateMessageHandler(AllocationService allocationService, TaskRepository taskRepository) {
        this.allocationService = allocationService;
        this.taskRepository = taskRepository;
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

            if (newTask.getEnabled() && newTask.getRunning() && newTask.getStartTime() == null) {
                    newTask.setStartTime(LocalDateTime.now());
                }


            taskRepository.save(newTask);

            // If the task is forced to stop, deallocate all the resources without Watcher
            if (Boolean.FALSE.equals(newTask.getRunning())) {
                LOGGER.warn("Task {} is forced to stop", newTask.getId());
                newTask.setEndTime(LocalDateTime.now());
                allocationService.deallocateAllResources(newTask);
            }

        }catch (Exception e){
            LOGGER.error("Error while saving task : {}", e.getMessage());
        }
    }
}
