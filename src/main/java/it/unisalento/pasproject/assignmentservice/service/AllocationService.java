package it.unisalento.pasproject.assignmentservice.service;

import it.unisalento.pasproject.assignmentservice.domain.*;
import it.unisalento.pasproject.assignmentservice.dto.resource.ResourceStatusMessageDTO;
import it.unisalento.pasproject.assignmentservice.dto.task.TaskStatusMessageDTO;
import it.unisalento.pasproject.assignmentservice.repositories.AssignedResourceRepository;
import it.unisalento.pasproject.assignmentservice.repositories.ResourceRepository;
import it.unisalento.pasproject.assignmentservice.repositories.TaskAssignmentRepository;
import it.unisalento.pasproject.assignmentservice.repositories.TaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Service
public class AllocationService {

    private final TaskRepository taskRepository;
    private final TaskAssignmentRepository taskAssignmentRepository;
    private final ResourceRepository resourceRepository;
    private final AssignedResourceRepository assignedResourceRepository;

    private final TasksMessageHandler tasksMessageHandler;
    private final ResourceMessageHandler resourcesMessageHandler;
    private final AnalyticsMessageHandler analyticsMessageHandler;

    private static final Logger LOGGER = Logger.getLogger(AllocationService.class.getName());

    @Autowired
    public AllocationService(TaskRepository taskRepository, TaskAssignmentRepository taskAssignmentRepository,
                             ResourceRepository resourceRepository, AssignedResourceRepository assignedMemberRepository,
                             TasksMessageHandler tasksMessageHandler, ResourceMessageHandler resourcesMessageHandler, AnalyticsMessageHandler analyticsMessageHandler) {
        this.taskRepository = taskRepository;
        this.taskAssignmentRepository = taskAssignmentRepository;
        this.resourceRepository = resourceRepository;
        this.assignedResourceRepository = assignedMemberRepository;
        this.tasksMessageHandler = tasksMessageHandler;
        this.resourcesMessageHandler = resourcesMessageHandler;
        this.analyticsMessageHandler = analyticsMessageHandler;
    }

    public void deallocateAllResources(Task task) {
        //Dealloca tutte le risorse assegnate alla task
        deallocateResources(getActiveTaskAssignment(task.getId()));
    }

    public List<Task> getAvailableTasks() {
        return taskRepository.findByEnabledTrueAndRunningTrue();
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


    public List<TaskAssignment> getActiveTaskAssignments(String id) {
        return taskAssignmentRepository.findAllByIdTaskAndIsCompleteFalse(id);
    }

    public TaskAssignment getActiveTaskAssignment(String id) {
        return taskAssignmentRepository.findByIdTaskAndIsCompleteFalse(id);
    }

    public TaskAssignment getActiveTaskAssignment(Resource resource) {
        List<AssignedResource> assignedResource = assignedResourceRepository.findByHardwareIdAndHasCompletedFalse(resource.getId());
        if (assignedResource.isEmpty())
            return null;

        AssignedResource assigned = assignedResource.getFirst();

        Optional<TaskAssignment> taskAssignment = taskAssignmentRepository.findById(assigned.getTaskAssignmentId());
        return taskAssignment.orElse(null);
    }

    public void deallocateResources(TaskAssignment taskAssignment) {

        //Prendo le risorse allocate per il task e le dealloco
        for (AssignedResource assigned : taskAssignment.getAssignedResources()) {


            if ( assigned.getCompletedTime() == null) //Risorsa non ancora avviata
                continue;

            //Dealloco Resource, AssignedResource (compreso nel TaskAssignment)
            deallocateResource(assigned);
        }

        completeTaskAssignment(taskAssignment);

    }

    public List<AssignedResource> getAssignedMembers() {
        //Restituisce gli assigned member che hanno completedTime > now
        return assignedResourceRepository.findByCompletedTimeAfterAndHasCompletedFalse(LocalDateTime.now());
    }

    public void deallocateResource(AssignedResource assignedResource) {
        LocalDateTime now = LocalDateTime.now();

        Optional<Resource> retResource = resourceRepository.findById(assignedResource.getHardwareId());
        if (retResource.isEmpty())
            return;

        Resource resource = retResource.get();

        Optional<TaskAssignment> taskAssignment = taskAssignmentRepository.findById(assignedResource.getTaskAssignmentId());

        // Aggiorno AssignedResource per completare il deallocamento della risorsa
        if(assignedResource.getCompletedTime() == null || assignedResource.getCompletedTime().isAfter(now))
            assignedResource.setCompletedTime(now);

        if ( now.isAfter(assignedResource.getCompletedTime()) ) {

            if ( !assignedResource.isHasCompleted() ) {
                assignedResource.setHasCompleted(true);
            }

        } else {
            assignedResource.setCompletedTime(now);
            assignedResource.setHasCompleted(true);
        }

        //Aggiorno TaskAssignment per completare il deallocamento della risorsa
        if (taskAssignment.isPresent()) {
            TaskAssignment taskAssignmentNew = taskAssignment.get();
            List<AssignedResource> assignedResources = new ArrayList<>(taskAssignmentNew.getAssignedResources());

            for (int i = 0; i < assignedResources.size(); i++) {
                AssignedResource res = assignedResources.get(i);
                if (res.getId().equals(assignedResource.getId())) {
                    res.setCompletedTime(assignedResource.getCompletedTime());
                    res.setHasCompleted(true);
                    LOGGER.info("Resource " + res.getHardwareId() + " has completed");
                    assignedResources.set(i, res); // Aggiorna l'elemento nella lista
                }
            }

            taskAssignmentNew.setAssignedResources(assignedResources);

            taskAssignmentRepository.save(taskAssignmentNew);
        }

        assignedResourceRepository.save(assignedResource);

        deallocateResource(resource);

        //Send message of resource deallocation
        updateAssignmentData(assignedResource, resource);

    }

    public void deallocateResource(Resource resource) {
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

        //Send Assignment data
        updateTaskAssignment(taskAssignment);
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

            // Send Assignment data
            sendAssignmentData(assignment);

            return taskAssignmentRepository.save(assignment);
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

    public AssignedResource assignResource(Resource resource, TaskAssignment taskAssignment) {
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
        assignedResource.setAssignedWorkingTimeInSeconds((long)availability.getEndTime().toSecondOfDay() - (long)currentTime.toSecondOfDay());

        assignedResource.setHasCompleted(false);

        assignedResource.setTaskAssignmentId(taskAssignment.getId());

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

    public Optional<AssignedResource> getAssignedResource(Resource resource, TaskAssignment taskAssignment){
        return assignedResourceRepository.findByHardwareIdAndTaskAssignmentId(resource.getId(), taskAssignment.getId());
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
        taskStatusMessageDTO.setId(task.getIdTask());
        taskStatusMessageDTO.setEnabled(task.getEnabled());
        taskStatusMessageDTO.setStartTime(task.getStartTime());
        taskStatusMessageDTO.setEndTime(task.getEndTime());
        taskStatusMessageDTO.setRunning(task.getRunning());

        if (Boolean.TRUE.equals(task.getRunning()))
            tasksMessageHandler.handleTaskAssignment(taskStatusMessageDTO);
        else
            tasksMessageHandler.endTaskExecution(taskStatusMessageDTO);
    }

    public void sendTaskStatusMessage(TaskAssignment assignmentTask) {
        TaskStatusMessageDTO taskStatusMessageDTO = new TaskStatusMessageDTO();

        List<String> hardwareIds = assignmentTask.getAssignedResources().stream()
                .map(AssignedResource::getHardwareId)
                .toList();

        taskStatusMessageDTO.setId(assignmentTask.getIdTask());
        taskStatusMessageDTO.setRunning(assignmentTask.getIsComplete());

        if(Boolean.TRUE.equals(assignmentTask.getIsComplete())) {
            taskStatusMessageDTO.setEndTime(assignmentTask.getCompletedTime());
            taskStatusMessageDTO.setEnabled(false);
        }

        taskStatusMessageDTO.setAssignedResources(hardwareIds);

        if (Boolean.TRUE.equals(assignmentTask.getIsComplete()))
            tasksMessageHandler.endTaskExecution(taskStatusMessageDTO);
        else
            tasksMessageHandler.handleTaskAssignment(taskStatusMessageDTO);
    }

    public List<Resource> getAssignedResources() {
        return resourceRepository.findByIsAvailableFalse();
    }

    public void updateAssignedResource(AssignedResource assignedResource) {
        assignedResourceRepository.save(assignedResource);
    }


    public void updateAssignmentData(AssignedResource assignedResource, Resource resource) {
        analyticsMessageHandler.updateAssignmentData(assignedResource, resource);
    }

    public void sendAssignmentData(AssignedResource assignedResource, Resource resource) {
        analyticsMessageHandler.updateAssignmentData(assignedResource, resource);
    }

    public void updateAssignmentData(TaskAssignment taskAssignment){
        analyticsMessageHandler.updateAssignmentData(taskAssignment);
    }

    public void sendAssignmentData(TaskAssignment taskAssignment){
        analyticsMessageHandler.sendAssignmentData(taskAssignment);
    }
}
