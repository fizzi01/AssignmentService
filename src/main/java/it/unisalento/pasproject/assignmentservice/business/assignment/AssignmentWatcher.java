package it.unisalento.pasproject.assignmentservice.business.assignment;

import it.unisalento.pasproject.assignmentservice.domain.AssignedResource;
import it.unisalento.pasproject.assignmentservice.domain.Task;
import it.unisalento.pasproject.assignmentservice.service.AllocationService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class AssignmentWatcher {

    private final AllocationService allocationService;

    public AssignmentWatcher(AllocationService allocationService) {
        this.allocationService = allocationService;
    }

    //Il metodo è annotato con @Scheduled(fixedRate = 60000) che fa si che il metodo venga eseguito ogni minuto

    //Il metodo watch() prende tutte le task che sono in running e che sono enabled

    //Per ogni task controlla se la task è scaduta e se è scaduta la completa e dealloca le risorse

    //Controlla se tutte le risorse sono finite e se sono finite completa la task

    //Prende tutti i membri assegnati e controlla se sono completati

    @Scheduled(fixedRate = 60000) // esegue ogni minuto
    public void watch() {
        //Prende tutte le Task che sono in running e che sono enabled
        allocationService.getRunningTasks().forEach(task -> {
            if (isTaskExpired(task)) {
                //Completa la task
                task.setRunning(false);
                allocationService.updateTask(task);
                deallocateResources(task);
            }else if (allResourcesFinished(task)) {
                //Completa la task
                task.setRunning(false);
                allocationService.updateTask(task);
                allocationService.completeTaskAssignment(task);
            }
        });

        //Prende tutti i membri assegnati e controlla se sono completati
        //Se completati dealloca le risorse, ma le lascia assegnate alla task per il calcolo del completamento
        allocationService.getAssignedMembers().forEach(member -> {
            if (isResourceCompleted(member)) {
                //Dealloca le risorsa
                allocationService.deallocateResource(member);
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
        return allocationService.getTaskAssignments(task.getId()).stream().allMatch(member -> now.isAfter(member.getCompletedTime()));
    }

    /**
     * Check if the resource is completed
     * @param member the member to check
     * @return true if the resource is completed, false otherwise
     */
    private boolean isResourceCompleted(AssignedResource member) {
        LocalDateTime now = LocalDateTime.now();
        return now.isAfter(member.getCompletedTime()) || member.isHasCompleted();
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
    private void deallocateResources(Task task) {
        //Prende il task assignment che ha come taskId il task.id e che non è completed
        //Dealloca le risorse
        allocationService.getTaskAssignments(task.getId()).forEach(allocationService::deallocateResources);
    }

}
