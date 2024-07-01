package it.unisalento.pasproject.assignmentservice.business.assignment;

import it.unisalento.pasproject.assignmentservice.domain.*;
import it.unisalento.pasproject.assignmentservice.service.AllocationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    public final AllocationService allocationService;

    private static final Logger LOGGER = LoggerFactory.getLogger(AllocationAlgorithm.class);

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

            if(Boolean.FALSE.equals(task.getEnabled())){
                task.setEnabled(true);
                allocationService.updateTask(task);
            }

            for (Resource resource : resources) {

                //Non considera più le task già assegnate
                if ( isAlreadyAssigned(taskAssignment.getAssignedResources(),resource)){
                    LOGGER.debug("Resource {} already assigned to task {} ", resource.getId(), task.getId());
                    continue;
                }

                // Verifichiamo che si siano raggiunti i limiti di potenza computazionale
                if ( task.getMaxComputingPower() - totalComputingPower < POWER_THRESHOLD && task.getMaxComputingPower() > 0.0 ) {
                    LOGGER.debug("Task {} reached max computing power", task.getId());
                    continue;
                } else if ( task.getMaxCudaPower() - totalCudaPower < CUDA_THRESHOLD && task.getMaxCudaPower() > 0.0){
                    LOGGER.debug("Task {} reached max cuda power", task.getId());
                    continue;
                }

                if (isSuitableResource(task, resource, totalComputingPower)) {
                    LOGGER.debug("Assigning resource {} to task {}", resource.getId(), task.getId());
                    // Assegna la risorsa alla task
                    //resource.setIsAvailable(false);
                    resource.setStatus(Resource.Status.BUSY);
                    resource.setCurrentTaskId(task.getId());
                    allocationService.updateResource(resource);

                    //Crea un AssignedResource e lo aggiunge alla lista delle risorse assegnate
                    AssignedResource assigned = allocationService.assignResource(resource, taskAssignment);

                    //Aggiorna la task assignment con la nuova risorsa assegnata
                    List<AssignedResource> assignedResources = new ArrayList<>(taskAssignment.getAssignedResources());
                    if (assignedResources.isEmpty()) {
                        LOGGER.debug("No resources assigned to task {}", task.getId());
                    }
                    assignedResources.add(assigned);
                    taskAssignment.setAssignedResources(assignedResources);

                    taskAssignment = allocationService.updateTaskAssignment(taskAssignment);

                    // Notifica l'aggiornamento (Moved to PayloadController)
                    //allocationService.sendAssignmentData(assigned, resource);

                    LOGGER.debug("Resource {} assigned to task {}", resource.getId(), task.getId());

                    // Aggiorna la potenza computazionale totale
                    totalComputingPower += getComputationalPower(assigned);
                    totalCudaPower += getCudaPower(assigned);

                }
            }
        }
    }


    public static boolean isAlreadyAssigned(List<AssignedResource> assignedResourceList,Resource resource) {

        for (AssignedResource assignedResource : assignedResourceList) {
            if (assignedResource.getHardwareId().equals(resource.getId())) {
                return true;
            }
        }
        return false;
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

        LOGGER.debug("Computational power: {}", actualComputationalPower);
        LOGGER.debug("Current computational power: {}", currentComputingPower);
        LOGGER.debug("Checking if is available");
        // Verifica se la risorsa è disponibile
        /*if (Boolean.FALSE.equals(resource.getIsAvailable())) {
            return false;
        }*/
        if (Resource.Status.BUSY.equals(resource.getStatus()) || Resource.Status.UNAVAILABLE.equals(resource.getStatus())) {
            return false;
        }

        LOGGER.debug("Checking if has min working time");
        //Controllo minWorkingTime
        if (!hasMinWorkingTime(resource, task)) {
            return false;
        }

        LOGGER.debug("Checking if has enough energy");
        LOGGER.debug("Resource energy: {}", resource.getKWh());
        LOGGER.debug("Task energy: {}", task.getMaxEnergyConsumption());
        //Controllo non si superi la maxEnergyConsumption
        if (resource.getKWh() > task.getMaxEnergyConsumption()) {
            return false;
        }

        LOGGER.debug("Checking if has enough power");
        // Controlla se l'aggiunta della risorsa supera la potenza computazionale massima
        double projectedComputingPower = currentComputingPower + actualComputationalPower;
        if (projectedComputingPower > task.getMaxComputingPower()) {
            return false;
        }

        LOGGER.debug("Checking if has enough power");
        // Controlla la potenza computazionale minima e massima
        double minComputingPower = task.getMinComputingPower() != null ? task.getMinComputingPower() : 0;
        if (actualComputationalPower < minComputingPower || actualComputationalPower > task.getMaxComputingPower()) {
            return false;
        }

        LOGGER.debug("Checking if has enough cuda power");
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
