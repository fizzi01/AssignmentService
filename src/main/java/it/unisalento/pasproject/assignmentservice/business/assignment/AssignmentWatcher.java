package it.unisalento.pasproject.assignmentservice.business.assignment;

import it.unisalento.pasproject.assignmentservice.domain.AssignedResource;
import it.unisalento.pasproject.assignmentservice.domain.Task;
import it.unisalento.pasproject.assignmentservice.domain.TaskAssignment;
import it.unisalento.pasproject.assignmentservice.service.AllocationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Component
public class AssignmentWatcher {

    private final AllocationService allocationService;

    public AssignmentWatcher(AllocationService allocationService) {
        this.allocationService = allocationService;
    }

    //Il metodo è annotato con @Scheduled(fixedRate = 30000) che fa si che il metodo venga eseguito ogni 30s

    //Il metodo watch() prende tutte le task che sono in running e che sono enabled

    //Per ogni task controlla se la task è scaduta e se è scaduta la completa e dealloca le risorse

    //Controlla se tutte le risorse sono finite e se sono finite completa la task

    //Prende tutti i membri assegnati e controlla se sono completati
    private static final Logger LOGGER = LoggerFactory.getLogger(AssignmentWatcher.class);

    @Scheduled(fixedRate = 10000) // esegue ogni minuto
    public void watch() {
        //Prende tutte le Task che sono in running e che sono enabled
        allocationService.getRunningTasks().forEach(task -> {
            if (isTaskExpired(task)) {
                LOGGER.info("Task {} expired", task.getId());
                //Completa la task
                task.setRunning(false);
                task.setEndTime(LocalDateTime.now());

                deallocateAllResources(task);
                allocationService.updateTask(task);
            }else if (allResourcesFinished(task)) {
                LOGGER.info("Task {} completed, all resources finished", task.getId());
                //Completa la task
                task.setRunning(false);
                task.setEndTime(LocalDateTime.now());
                allocationService.completeTaskAssignment(task);
                allocationService.updateTask(task);
            }
        });

        //Prende tutte le AssignedResources che hanno completed time passato e che non hanno isComplete=true
        //Quindi dealloca forzatamente le risorse
        allocationService.getAssignedMembers().forEach(allocationService::deallocateResource);

        //Prende tutte le risorse che sono isAvailable=false e controlla se il TaskAssignment a cui sono associate sia completed
        //Se completed le dealloca
        allocationService.getNotAssignableResources().forEach(resource -> {
            LOGGER.info("Checking resource availability {}", resource.getId());
            TaskAssignment taskAssignment = allocationService.getActiveTaskAssignment(resource);

            if (taskAssignment != null && taskAssignment.getIsComplete()) {

                Optional<AssignedResource> assignedResource = allocationService.getAssignedResource(resource, taskAssignment);

                if(assignedResource.isPresent()) {
                    AssignedResource assigned = assignedResource.get();
                    allocationService.deallocateResource(assigned);
                }else{ //Fallback dealloca una risorsa assegnata volatile
                    allocationService.deallocateResource(resource);
                }

                LOGGER.info("Resource {} deallocated", resource.getId());
            }
        });
    }

    /**
     * Check if all the resources allocated to the task are finished
     * @param task the task to check
     * @return true if all the resources are finished, false otherwise
     */
    private boolean allResourcesFinished(Task task) {
        LocalDateTime now = LocalDateTime.now();
        TaskAssignment taskAssignment = allocationService.getActiveTaskAssignment(task.getId());
        if(taskAssignment == null){
            return false;
        }
        List<AssignedResource> assigned = taskAssignment.getAssignedResources();
        if (assigned.isEmpty()){
            return false;
        }

        return assigned.stream().allMatch(resource -> resource.isHasCompleted() || ( resource.getCompletedTime() != null && now.isAfter(resource.getCompletedTime())));
    }

    /**
     * Check if the resource is completed
     * @param resource the member to check
     * @return true if the resource is completed, false otherwise
     */
    private boolean isResourceCompleted(AssignedResource resource) {
        LocalDateTime now = LocalDateTime.now();

        if (resource.getCompletedTime() == null){
            return false;
        }

        return now.isAfter(resource.getCompletedTime()) || resource.isHasCompleted();
    }


    /**
     * Check if the task is expired
     * @return true if the task is expired, false otherwise
     */
    private boolean isTaskExpired(Task task) {
        LocalDateTime now = LocalDateTime.now();
        return now.isAfter(task.getStartTime().plusSeconds(task.getTaskDuration().longValue()));
    }

    /**
     * Deallocate the resources used by the task
     * @param task the task to deallocate the resources
     */
    private void deallocateAllResources(Task task) {
        //Prende il task assignment che ha come taskId il task.id e che non è completed
        //Dealloca le risorse
        allocationService.deallocateResources(allocationService.getActiveTaskAssignment(task.getIdTask()));

    }

}
