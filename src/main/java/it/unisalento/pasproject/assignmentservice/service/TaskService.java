package it.unisalento.pasproject.assignmentservice.service;

import it.unisalento.pasproject.assignmentservice.domain.Task;
import it.unisalento.pasproject.assignmentservice.dto.TaskDTO;
import it.unisalento.pasproject.assignmentservice.repositories.TaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

@Service
public class TaskService {

    private final TaskRepository taskRepository;

    private final MongoTemplate mongoTemplate;



    @Autowired
    public TaskService(TaskRepository taskRepository, MongoTemplate mongoTemplate) {
        this.taskRepository = taskRepository;
        this.mongoTemplate = mongoTemplate;
    }

    public TaskDTO getTaskDTO(Task task) {
        TaskDTO taskDTO = new TaskDTO();
        taskDTO.setIdTask(task.getIdTask());
        taskDTO.setEmailUtente(task.getEmailUtente());
        taskDTO.setMaxComputingPower(task.getMaxComputingPower());
        taskDTO.setMinComputingPower(task.getMinComputingPower());
        taskDTO.setMaxCudaPower(task.getMaxCudaPower());
        taskDTO.setMinCudaPower(task.getMinCudaPower());
        taskDTO.setTaskDuration(task.getTaskDuration());
        taskDTO.setMaxEnergyConsumption(task.getMaxEnergyConsumption());
        taskDTO.setMinEnergyConsumption(task.getMinEnergyConsumption());
        taskDTO.setMinWorkingTime(task.getMinWorkingTime());
        taskDTO.setRunning(task.getRunning());
        taskDTO.setEnabled(task.getEnabled());
        return taskDTO;
    }

    public Task getByIdTask(String idTask) {
        return taskRepository.findByIdTask(idTask);
    }


}
