package it.unisalento.pasproject.assignmentservice.service;

import it.unisalento.pasproject.assignmentservice.business.io.producer.MessageProducer;
import it.unisalento.pasproject.assignmentservice.business.io.producer.MessageProducerStrategy;
import it.unisalento.pasproject.assignmentservice.domain.AssignedResource;
import it.unisalento.pasproject.assignmentservice.domain.Resource;
import it.unisalento.pasproject.assignmentservice.domain.Task;
import it.unisalento.pasproject.assignmentservice.domain.TaskAssignment;
import it.unisalento.pasproject.assignmentservice.dto.analytics.AnalyticsMessageDTO;
import it.unisalento.pasproject.assignmentservice.dto.analytics.AssignedAnalyticsDTO;
import it.unisalento.pasproject.assignmentservice.dto.analytics.AssignedResourceAnalyticsDTO;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class AnalyticsMessageHandler {

    @Value("${rabbitmq.exchange.analytics.name}")
    private String analyticsExchange;

    @Value("${rabbitmq.routing.sendUpdatedAssignmentData.key}")
    private String sendUpdatedAssignmentDataRoutingKey;

    private final MessageProducer messageProducer;
    private final AllocationService allocationService;
    private final TaskService taskService;

    public AnalyticsMessageHandler(MessageProducer messageProducer, @Qualifier("RabbitMQProducer") MessageProducerStrategy messageProducerStrategy, AllocationService allocationService, TaskService taskService) {
        this.messageProducer = messageProducer;
        this.messageProducer.setStrategy(messageProducerStrategy);
        this.allocationService = allocationService;
        this.taskService = taskService;
    }


    public void updateAssignmentData(AssignedResource assignedResource, Resource resource) {
        //Data fusion dei dati di assegnamento e di risorsa nel AssignedResourceAnalyticsDTO

        //Recupero il TaskAssignment
        Optional<TaskAssignment> taskAssignment = allocationService.getTaskAssignment(assignedResource.getTaskAssignmentId());

        if (taskAssignment.isEmpty()) {
            return;
        }

        TaskAssignment taskAssignmentEntity = taskAssignment.get();

        Optional<Task> task = taskService.getTask(taskAssignmentEntity.getIdTask());
        if (task.isEmpty()) {
            return;
        }
        Task retTask = task.get();

        AssignedAnalyticsDTO assignedAnalyticsDTO = getAssignedAnalyticsDTO(taskAssignmentEntity,retTask);
        AssignedResourceAnalyticsDTO assignedResourceAnalyticsDTO = getAssignedResAnalyticsDTO(taskAssignmentEntity, assignedResource, resource);

        AnalyticsMessageDTO analyticsMessageDTO = new AnalyticsMessageDTO();

        analyticsMessageDTO.setAssignedResource(assignedResourceAnalyticsDTO);
        analyticsMessageDTO.setAssignment(assignedAnalyticsDTO);

        //Invio il messaggio
        messageProducer.sendMessage(analyticsMessageDTO, sendUpdatedAssignmentDataRoutingKey, analyticsExchange);

    }

    public void updateAssignmentData(TaskAssignment taskAssignment){
        AnalyticsMessageDTO analyticsMessageDTO = new AnalyticsMessageDTO();

        Optional<Task> task = taskService.getTask(taskAssignment.getIdTask());
        if (task.isEmpty()) {
            return;
        }
        Task retTask = task.get();

        analyticsMessageDTO.setAssignment(getAssignedAnalyticsDTO(taskAssignment,retTask));

        messageProducer.sendMessage(analyticsMessageDTO, sendUpdatedAssignmentDataRoutingKey, analyticsExchange);
    }

    public void sendAssignmentData(TaskAssignment taskAssignment){
        LocalDateTime now = LocalDateTime.now();

        Optional<Task> task = taskService.getTask(taskAssignment.getIdTask());
        if (task.isEmpty()) {
            return;
        }
        Task retTask = task.get();

        AssignedAnalyticsDTO assignedAnalyticsDTO = getAssignedAnalyticsDTO(taskAssignment, retTask);
        assignedAnalyticsDTO.setAssignedTime(now);

        AnalyticsMessageDTO analyticsMessageDTO = new AnalyticsMessageDTO();
        analyticsMessageDTO.setAssignment(assignedAnalyticsDTO);

        messageProducer.sendMessage(analyticsMessageDTO, sendUpdatedAssignmentDataRoutingKey, analyticsExchange);
    }

    public static AssignedAnalyticsDTO getAssignedAnalyticsDTO(TaskAssignment taskAssignmentEntity, Task task){
        LocalDateTime now = LocalDateTime.now();
        AssignedAnalyticsDTO assignedAnalyticsDTO = new AssignedAnalyticsDTO();
        assignedAnalyticsDTO.setTaskId(taskAssignmentEntity.getIdTask());
        assignedAnalyticsDTO.setCompletedTime(taskAssignmentEntity.getCompletedTime());
        assignedAnalyticsDTO.setLastUpdate(now);
        assignedAnalyticsDTO.setComplete(taskAssignmentEntity.getIsComplete());

        assignedAnalyticsDTO.setEmailUtente(task.getEmailUtente());


        return assignedAnalyticsDTO;
    }

    public static AssignedResourceAnalyticsDTO getAssignedResAnalyticsDTO(TaskAssignment taskAssignment, AssignedResource assignedResource, Resource resource){
        LocalDateTime now = LocalDateTime.now();
        AssignedResourceAnalyticsDTO assignedResourceAnalyticsDTO = new AssignedResourceAnalyticsDTO();
        assignedResourceAnalyticsDTO.setId(assignedResource.getId());
        assignedResourceAnalyticsDTO.setTaskId(taskAssignment.getIdTask());
        assignedResourceAnalyticsDTO.setHardwareId(assignedResource.getHardwareId());
        assignedResourceAnalyticsDTO.setHardwareName(resource.getName());
        assignedResourceAnalyticsDTO.setMemberEmail(resource.getMemberEmail());
        assignedResourceAnalyticsDTO.setAssignedSingleScore(assignedResource.getAssignedSingleScore());
        assignedResourceAnalyticsDTO.setAssignedMultiScore(assignedResource.getAssignedMultiScore());
        assignedResourceAnalyticsDTO.setAssignedOpenclScore(assignedResource.getAssignedOpenclScore());
        assignedResourceAnalyticsDTO.setAssignedCudaScore(assignedResource.getAssignedCudaScore());

        assignedResourceAnalyticsDTO.setAssignedEnergyConsumptionPerHour(assignedResource.getAssignedEnergyConsumptionPerHour());

        assignedResourceAnalyticsDTO.setAssignedTime(assignedResource.getAssignedTime());
        assignedResourceAnalyticsDTO.setCompletedTime(assignedResource.getCompletedTime());
        assignedResourceAnalyticsDTO.setHasCompleted(assignedResource.isHasCompleted());
        assignedResourceAnalyticsDTO.setLastUpdate(now);

        return assignedResourceAnalyticsDTO;
    }
}
