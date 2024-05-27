package it.unisalento.pasproject.assignmentservice.business.assignment;

import it.unisalento.pasproject.assignmentservice.domain.*;
import it.unisalento.pasproject.assignmentservice.service.AllocationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import static it.unisalento.pasproject.assignmentservice.business.assignment.AssignmentConstants.CUDA_THRESHOLD;
import static it.unisalento.pasproject.assignmentservice.business.assignment.AssignmentConstants.POWER_THRESHOLD;

@Component
public class AllocationAlgorithm {

    public AllocationService allocationService;

    @Autowired
    public AllocationAlgorithm(AllocationService allocationService) {
        this.allocationService = allocationService;
    }

    /**
     * Assegna le risorse alle task in modo da massimizzare la potenza computazionale e il numero di risorse assegnate
     * @param tasks lista di task a cui assegnare
     * @param resources lista di risorse disponibili
     */
    public void assignResources(List<Task> tasks, List<Resource> resources) {
        for (Task task : tasks) {

            TaskAssignment taskAssignment = allocationService.getTaskAssignment(task);

            double totalComputingPower = getCurrentComputingPower(taskAssignment.getAssignedResources());
            double totalCudaPower = getCurrentCudaPower(taskAssignment.getAssignedResources());

            for (Resource resource : resources) {

                //Non considera più le task già assegnate
                if ( isAlreadyAssigned(taskAssignment.getAssignedResources(),resource)){
                    continue;
                }

                // Verifichiamo che si siano raggiunti i limiti di potenza computazionale
                if ( task.getMaxComputingPower() - totalComputingPower < POWER_THRESHOLD && task.getMaxComputingPower() > 0.0 ) {
                    return;
                } else if ( task.getMaxCudaPower() - totalCudaPower < CUDA_THRESHOLD && task.getMaxCudaPower() > 0.0){
                    return;
                }

                if (isSuitableResource(task, resource, totalComputingPower)) {

                    // Assegna la risorsa alla task
                    resource.setIsAvailable(false);
                    resource.setCurrentTaskId(task.getId());
                    allocationService.updateResource(resource);

                    //Crea un AssignedResource e lo aggiunge alla lista delle risorse assegnate
                    AssignedResource assigned = allocationService.assignResource(resource);

                    //Aggiorna la task assignment con la nuova risorsa assegnata
                    taskAssignment.getAssignedResources().add(assigned);
                    taskAssignment = allocationService.updateTaskAssignment(taskAssignment);

                    // Aggiorna la potenza computazionale totale
                    totalComputingPower += getComputationalPower(assigned);
                    totalCudaPower += getCudaPower(assigned);

                }
            }
        }
    }


    public static boolean isAlreadyAssigned(List<AssignedResource> assignedResourceList,Resource resource) {
        return assignedResourceList.contains(resource);
    }

    public static double getCurrentComputingPower(List<AssignedResource> assignedResources) {
        double totalComputingPower = 0;
        for (AssignedResource assignedResource : assignedResources) {
            totalComputingPower += getComputationalPower(assignedResource);
        }
        return totalComputingPower;
    }

    public static double getCurrentCudaPower(List<AssignedResource> assignedResources) {
        double totalCudaPower = 0;
        for (AssignedResource assignedResource : assignedResources) {
            totalCudaPower += getCudaPower(assignedResource);
        }
        return totalCudaPower;
    }

    public static double getCudaPower(AssignedResource resource) {
        return resource.getAssignedCudaScore();
    }

    public static double getCudaPower(Resource resource) {
        return resource.getCudaScore();
    }


    public static double getComputationalPower(AssignedResource resource) {
        return resource.getAssignedMultiScore() != 0.0 ? (Math.max(resource.getAssignedMultiScore(),resource.getAssignedSingleScore())) : (Math.max(resource.getAssignedOpenclScore(), resource.getAssignedVulkanScore()));
    }

    public static double getComputationalPower(Resource resource) {
        return resource.getMulticoreScore() != 0.0 ? (Math.max(resource.getMulticoreScore(),resource.getSingleCoreScore())) : (Math.max(resource.getOpenclScore(), resource.getVulkanScore()));
    }

    public static boolean hasMinWorkingTime(Resource resource, Task task) {
        DayOfWeek currentDay = LocalDateTime.now().getDayOfWeek();
        LocalTime currentTime = LocalTime.now();

        Availability availability = resource.getAvailability().stream()
                .filter(availability1 -> availability1.getDayOfWeek().equals(currentDay) &&
                        !currentTime.isBefore(availability1.getStartTime()) &&
                        !currentTime.isAfter(availability1.getEndTime()))
                .findFirst().orElseThrow();

        return availability.getEndTime().minusSeconds(availability.getStartTime().toSecondOfDay()).toSecondOfDay() >= task.getTaskDuration();
    }

    /**
     * Verifica se la risorsa è adatta per la task
     * @param task task
     * @param resource risorsa
     * @param currentComputingPower potenza computazionale attuale
     * @return true se la risorsa è adatta, false altrimenti
     */
    public boolean isSuitableResource(Task task, Resource resource,double currentComputingPower) {
        double actualComputationalPower = getComputationalPower(resource);

        // Verifica se la risorsa è disponibile
        if (!resource.getIsAvailable()) {
            return false;
        }

        //Controllo minWorkingTime
        if (!hasMinWorkingTime(resource, task)) {
            return false;
        }

        // Controlla se l'aggiunta della risorsa supera la potenza computazionale massima
        double projectedComputingPower = currentComputingPower + actualComputationalPower;
        if (projectedComputingPower > task.getMaxComputingPower()) {
            return false;
        }

        // Controlla la potenza computazionale minima e massima
        double minComputingPower = task.getMinComputingPower() != null ? task.getMinComputingPower() : 0;
        if (actualComputationalPower < minComputingPower || actualComputationalPower > task.getMaxComputingPower()) {
            return false;
        }

        // Controlla la potenza CUDA minima e massima, se specificate
        if ((task.getMinCudaPower() != null && task.getMinCudaPower() != 0.0) && resource.getCudaScore() < task.getMinCudaPower()) {
            return false;
        }
        if ((task.getMaxCudaPower() != null && task.getMaxCudaPower() != 0.0) && resource.getCudaScore() > task.getMaxCudaPower()) {
            return false;
        }

        return 3600 >= task.getTaskDuration();
    }
}
