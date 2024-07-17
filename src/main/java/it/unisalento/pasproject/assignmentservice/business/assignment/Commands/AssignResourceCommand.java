package it.unisalento.pasproject.assignmentservice.business.assignment.Commands;

import it.unisalento.pasproject.assignmentservice.domain.*;
import it.unisalento.pasproject.assignmentservice.service.AllocationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static it.unisalento.pasproject.assignmentservice.business.assignment.AssignmentConstants.CUDA_THRESHOLD;
import static it.unisalento.pasproject.assignmentservice.business.assignment.AssignmentConstants.POWER_THRESHOLD;

public class AssignResourceCommand implements Command {
    private final Task task;
    private final List<Resource> resources;
    private final AllocationService allocationService;
    private TaskAssignment taskAssignment;
    private double totalComputingPower;
    private double totalCudaPower;

    private static final Logger LOGGER = LoggerFactory.getLogger(AssignResourceCommand.class);

    public AssignResourceCommand(Task task, List<Resource> resources, AllocationService allocationService) {
        this.task = task;
        this.resources = resources;
        this.allocationService = allocationService;
    }

    @Override
    public void execute() {
        taskAssignment = allocationService.getTaskAssignment(task);

        totalComputingPower = getCurrentComputingPower(taskAssignment.getAssignedResources());
        totalCudaPower = getCurrentCudaPower(taskAssignment.getAssignedResources());

        for (Resource resource : resources) {

            if (shouldSkipResource(resource)) {
                continue;
            }

            if (isSuitableResource(task, resource, totalComputingPower)) {
                assignResourceToTask(resource);
            }

        }
    }

    public boolean shouldSkipResource(Resource resource) {
        if (isAlreadyAssigned(taskAssignment.getAssignedResources(), resource)) {
            LOGGER.debug("Resource {} already assigned to task {}", resource.getId(), task.getId());
            return true;
        }

        if (hasReachedMaxComputingPower()) {
            LOGGER.debug("Task {} reached max computing power", task.getId());
            return true;
        }

        if (hasReachedMaxCudaPower()) {
            LOGGER.debug("Task {} reached max cuda power", task.getId());
            return true;
        }

        return false;
    }

    private boolean hasReachedMaxComputingPower() {
        //return task.getMaxComputingPower() - totalComputingPower < POWER_THRESHOLD && task.getMaxComputingPower() > 0.0;
        return task.getMaxComputingPower() - totalComputingPower == 0.0 && task.getMaxComputingPower() > 0.0;
    }

    private boolean hasReachedMaxCudaPower() {
        //return task.getMaxCudaPower() - totalCudaPower < CUDA_THRESHOLD && task.getMaxCudaPower() > 0.0;
        return task.getMaxCudaPower() - totalCudaPower == 0.0 && task.getMaxCudaPower() > 0.0;
    }

    public void assignResourceToTask(Resource resource) {
        LOGGER.debug("Assigning resource {} to task {}", resource.getId(), task.getId());

        // Assegna la risorsa alla task
        resource.setStatus(Resource.Status.BUSY);
        resource.setCurrentTaskId(task.getId());
        allocationService.updateResource(resource);

        // Crea un AssignedResource e lo aggiunge alla lista delle risorse assegnate
        AssignedResource assigned = allocationService.assignResource(resource, taskAssignment);
        updateTaskAssignment(assigned);

        // Aggiorna la potenza computazionale totale
        totalComputingPower += getComputationalPower(assigned);
        totalCudaPower += getCudaPower(assigned);

        LOGGER.debug("Resource {} assigned to task {}", resource.getId(), task.getId());
    }

    private void updateTaskAssignment(AssignedResource assigned) {
        List<AssignedResource> assignedResources = new ArrayList<>(taskAssignment.getAssignedResources());
        if (assignedResources.isEmpty()) {
            LOGGER.debug("No resources assigned to task {}", task.getId());
        }
        assignedResources.add(assigned);
        taskAssignment.setAssignedResources(assignedResources);
        taskAssignment = allocationService.updateTaskAssignment(taskAssignment);
    }

    private double getCurrentComputingPower(List<AssignedResource> assignedResources) {
        double tot = 0;
        for (AssignedResource assignedResource : assignedResources) {
            tot += getComputationalPower(assignedResource);
        }
        return tot;
    }

    private double getCurrentCudaPower(List<AssignedResource> assignedResources) {
        double tot = 0;
        for (AssignedResource assignedResource : assignedResources) {
            tot += getCudaPower(assignedResource);
        }
        return tot;
    }

    private double getCudaPower(AssignedResource resource) {
        return resource.getAssignedCudaScore();
    }

    private double getComputationalPower(AssignedResource resource) {
        return resource.getAssignedMultiScore() != 0.0 ? (Math.max(resource.getAssignedMultiScore(),resource.getAssignedSingleScore())) : (Math.max(resource.getAssignedOpenclScore(), resource.getAssignedVulkanScore()));
    }

    public static double getComputationalPower(Resource resource) {
        return resource.getMulticoreScore() != 0.0 ? (Math.max(resource.getMulticoreScore(),resource.getSingleCoreScore())) : (Math.max(resource.getOpenclScore(), resource.getVulkanScore()));
    }

    public static boolean hasMinWorkingTime(Resource resource, Task task) {
        DayOfWeek currentDay = LocalDateTime.now().getDayOfWeek();
        LocalTime currentTime = LocalTime.now();

        Optional<Availability> availability = resource.getAvailability().stream()
                .filter(availability1 -> availability1.getDayOfWeek().equals(currentDay) &&
                        !currentTime.isBefore(availability1.getStartTime()) &&
                        !currentTime.isAfter(availability1.getEndTime()))
                .findFirst();

        return availability.filter(value -> value.getEndTime().minusSeconds(value.getStartTime().toSecondOfDay()).toSecondOfDay() >= task.getTaskDuration()).isPresent();
    }

    public static boolean isAlreadyAssigned(List<AssignedResource> assignedResourceList,Resource resource) {

        for (AssignedResource assignedResource : assignedResourceList) {
            if (assignedResource.getHardwareId().equals(resource.getId())) {
                return true;
            }
        }
        return false;
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
