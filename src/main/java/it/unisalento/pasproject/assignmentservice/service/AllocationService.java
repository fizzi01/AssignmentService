package it.unisalento.pasproject.assignmentservice.service;

import it.unisalento.pasproject.assignmentservice.domain.*;
import it.unisalento.pasproject.assignmentservice.dto.ResourceMessageDTO;
import it.unisalento.pasproject.assignmentservice.dto.ResourceStatusMessageDTO;
import it.unisalento.pasproject.assignmentservice.dto.TaskMessageDTO;
import it.unisalento.pasproject.assignmentservice.dto.TaskStatusMessageDTO;
import it.unisalento.pasproject.assignmentservice.repositories.AssignedResourceRepository;
import it.unisalento.pasproject.assignmentservice.repositories.ResourceRepository;
import it.unisalento.pasproject.assignmentservice.repositories.TaskAssignmentRepository;
import it.unisalento.pasproject.assignmentservice.repositories.TaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.*;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class AllocationService {

    private final TaskRepository taskRepository;
    private final TaskAssignmentRepository taskAssignmentRepository;
    private final ResourceRepository resourceRepository;
    private final AssignedResourceRepository assignedResourceRepository;

    private final TasksMessageHandler tasksMessageHandler;
    private final ResourceMessageHandler resourcesMessageHandler;

    @Autowired
    public AllocationService(TaskRepository taskRepository, TaskAssignmentRepository taskAssignmentRepository,
                             ResourceRepository resourceRepository, AssignedResourceRepository assignedMemberRepository,
                             TasksMessageHandler tasksMessageHandler, ResourceMessageHandler resourcesMessageHandler) {
        this.taskRepository = taskRepository;
        this.taskAssignmentRepository = taskAssignmentRepository;
        this.resourceRepository = resourceRepository;
        this.assignedResourceRepository = assignedMemberRepository;
        this.tasksMessageHandler = tasksMessageHandler;
        this.resourcesMessageHandler = resourcesMessageHandler;
    }

    public List<Task> getAvailableTasks() {
        //Prendo una lista di TaskAssignment non ancora completati
        List<TaskAssignment> taskAssignments = taskAssignmentRepository.findByIsCompleteFalse();

        //Delle task ottenute (gli id delle task) prendo solo le task che hanno enabled=true e running=true
        return taskRepository.findByIdInAndEnabledTrueAndRunningTrue(taskAssignments.stream().map(TaskAssignment::getIdTask).toList());
    }

    public List<Resource> getAvailableResources() {
        //TODO: Vedere se funziona
        DayOfWeek currentDay = LocalDateTime.now().getDayOfWeek();
        LocalTime currentTime = LocalTime.now();

        List<Resource> availableResources = resourceRepository.findByIsAvailableTrue();

        availableResources = availableResources.stream()
                .filter(resource -> resource.getAvailability().stream()
                        .anyMatch(availability -> (
                                availability.getDayOfWeek().equals(currentDay) &&
                                !currentTime.isBefore(availability.getStartTime()) &&
                                !currentTime.isAfter(availability.getEndTime()))))
                .collect(Collectors.toList());

        return availableResources;
    }

    public List<Task> getRunningTasks() {
        return taskRepository.findByEnabledTrueAndRunningTrue();
    }


    public List<TaskAssignment> getTaskAssignments(String id) {
        return taskAssignmentRepository.findByIdTaskAndIsCompleteFalse(id);
    }

    public void deallocateResources(TaskAssignment taskAssignment) {
        completeTaskAssignment(taskAssignment);
        LocalDateTime now = LocalDateTime.now();

        //Prendo le risorse allocate per il task e le dealloco
        for (AssignedResource assigned : taskAssignment.getAssignedResources()) {

            Resource resource = resourceRepository.findById(assigned.getHardwareId()).orElseThrow();

            if ( assigned.getCompletedTime() == null) //Risorsa non ancora avviata
                continue;

            if ( now.isAfter(assigned.getCompletedTime()) ) {

                if ( !assigned.isHasCompleted() ) {
                    assigned.setHasCompleted(true);
                }


            } else {
                assigned.setCompletedTime(now);
                assigned.setHasCompleted(true);
            }

            assignedResourceRepository.save(assigned);

            deallocateResource(assigned);
            /*if ( !resource.getIsAvailable() ){
                resource.setIsAvailable(true);
                resource.setCurrentTaskId(null);
            }

            resourceRepository.save(resource);*/
            //TODO: forse basterebbe richiamare deallocateResource(assigned) riducendo ridondanza
        }

    }

    public List<AssignedResource> getAssignedMembers() {
        //Restituisce gli assigned member che hanno completedTime > now
        return assignedResourceRepository.findByCompletedTimeAfter(LocalDateTime.now());
    }

    public void deallocateResource(AssignedResource assignedResource) {
        Resource resource = resourceRepository.findById(assignedResource.getHardwareId()).orElseThrow();
        resource.setIsAvailable(true);
        resource.setCurrentTaskId(null);
        resourceRepository.save(resource);
        sendResourceStatusMessage(resource);
    }

    public void updateTask(Task task) {
        taskRepository.save(task);
        sendTaskStatusMessage(task);
    }

    public void completeTaskAssignment(Task task){
        TaskAssignment taskAssignment = taskAssignmentRepository.findByIdTask(task.getId());
        taskAssignment.setIsComplete(true);
        taskAssignment.setCompletedTime(LocalDateTime.now());
        taskAssignmentRepository.save(taskAssignment);
    }

    public void completeTaskAssignment(TaskAssignment taskAssignment){
        taskAssignment.setIsComplete(true);
        taskAssignment.setCompletedTime(LocalDateTime.now());
        taskAssignmentRepository.save(taskAssignment);
    }

    public TaskAssignment getTaskAssignment(Task task) {
        TaskAssignment assignment =  taskAssignmentRepository.findByIdTask(task.getId());

        if ( assignment == null ) {
            task.setStartTime(LocalDateTime.now());
            task.setRunning(true);
            updateTask(task);

            assignment = new TaskAssignment();
            assignment.setIdTask(task.getId());
            assignment.setIsComplete(false);
            assignment.setAssignedResources(List.of());
            taskAssignmentRepository.save(assignment);
        }

        return assignment;
    }

    public Optional<TaskAssignment> getTaskAssignment(String assignmentId) {
        return taskAssignmentRepository.findById(assignmentId);
    }

    public TaskAssignment updateTaskAssignment(TaskAssignment taskAssignment) {
        //TODO: Vedere se funziona l'aggiornamento delle liste di hardware assegnati
        sendTaskStatusMessage(taskAssignment);
        return taskAssignmentRepository.save(taskAssignment);
    }


    public void updateResource(Resource resource) {
        resourceRepository.save(resource);
        sendResourceStatusMessage(resource);
    }

    public AssignedResource assignResource(Resource resource) {
        //TODO: Vedere se serve qui un invio di update, sembra di no
        DayOfWeek currentDay = LocalDateTime.now().getDayOfWeek();
        LocalTime currentTime = LocalTime.now();

        Availability availability = resource.getAvailability().stream()
                .filter(availability1 -> availability1.getDayOfWeek().equals(currentDay) &&
                        !currentTime.isBefore(availability1.getStartTime()) &&
                        !currentTime.isAfter(availability1.getEndTime()))
                .findFirst().orElseThrow();

        AssignedResource assignedResource = new AssignedResource();

        assignedResource.setAssignedSingleScore(resource.getSingleCoreScore());
        assignedResource.setAssignedMultiScore(resource.getMulticoreScore());
        assignedResource.setAssignedOpenclScore(resource.getOpenclScore());
        assignedResource.setAssignedVulkanScore(resource.getVulkanScore());
        assignedResource.setAssignedCudaScore(resource.getCudaScore());
        assignedResource.setAssignedEnergyConsumptionPerHour(resource.getKWh());

        assignedResource.setHardwareId(resource.getId());
        assignedResource.setAssignedWorkingTimeInSeconds(availability.getEndTime().toSecondOfDay() - currentTime.toSecondOfDay());
        assignedResource.setHasCompleted(false);

        return assignedResourceRepository.save(assignedResource);
    }

    public boolean isResourceAlreadyAllocated(Resource resource) {
        //TODO: CAMBIARE QUERY
        // Vedere se è utile capire se la risorsa esiste in base all'assignedTime
        return assignedResourceRepository.existsByHardwareIdAndHasCompletedTrue(resource.getId());
    }

    public Optional<Resource> getResource(String id){
        return resourceRepository.findById(id);
    }

    public Optional<AssignedResource> getAssignedResource(String id){
        return assignedResourceRepository.findById(id);
    }

    public void sendResourceStatusMessage(Resource resource) {
        // Creazione di un oggetto ResourceMessageDTO
        ResourceStatusMessageDTO resourceStatusMessageDTO = new ResourceStatusMessageDTO();
        resourceStatusMessageDTO.setId(resource.getId());
        resourceStatusMessageDTO.setIsAvailable(resource.getIsAvailable());
        resourceStatusMessageDTO.setCurrentTaskId(resource.getCurrentTaskId());

        if (resource.getCurrentTaskId() != null)
            resourcesMessageHandler.handleResourceAssignment(resourceStatusMessageDTO);
        else
            resourcesMessageHandler.handleResourceDeallocation(resourceStatusMessageDTO);
    }

    public void sendTaskStatusMessage(Task task) {
        TaskStatusMessageDTO taskStatusMessageDTO = new TaskStatusMessageDTO();
        taskStatusMessageDTO.setId(task.getId());
        taskStatusMessageDTO.setStartTime(task.getStartTime());
        taskStatusMessageDTO.setEndTime(task.getEndTime());
        taskStatusMessageDTO.setRunning(task.getRunning());

        if (task.getRunning())
            tasksMessageHandler.handleTaskAssignment(taskStatusMessageDTO);
        else
            tasksMessageHandler.endTaskExecution(taskStatusMessageDTO);
    }

    public void sendTaskStatusMessage(TaskAssignment assignmentTask) {
        TaskStatusMessageDTO taskStatusMessageDTO = new TaskStatusMessageDTO();

        List<String> hardwareIds = assignmentTask.getAssignedResources().stream()
                .map(AssignedResource::getHardwareId)
                .collect(Collectors.toList());

        taskStatusMessageDTO.setId(assignmentTask.getIdTask());
        taskStatusMessageDTO.setAssignedResources(hardwareIds);

        if (assignmentTask.getIsComplete())
            tasksMessageHandler.endTaskExecution(taskStatusMessageDTO);
        else
            tasksMessageHandler.handleTaskAssignment(taskStatusMessageDTO);
    }

}
