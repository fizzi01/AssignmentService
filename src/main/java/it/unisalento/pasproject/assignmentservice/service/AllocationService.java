package it.unisalento.pasproject.assignmentservice.service;

import it.unisalento.pasproject.assignmentservice.business.CheckOutUtils;
import it.unisalento.pasproject.assignmentservice.domain.*;
import it.unisalento.pasproject.assignmentservice.dto.resource.ResourceStatusMessageDTO;
import it.unisalento.pasproject.assignmentservice.dto.task.TaskStatusMessageDTO;
import it.unisalento.pasproject.assignmentservice.exceptions.AssignedResourceNotFoundException;
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

import static it.unisalento.pasproject.assignmentservice.service.NotificationConstants.INFO_NOTIFICATION_TYPE;
import static it.unisalento.pasproject.assignmentservice.service.NotificationConstants.SUCCESS_NOTIFICATION_TYPE;

@Service
public class AllocationService {

    private final TaskRepository taskRepository;
    private final TaskAssignmentRepository taskAssignmentRepository;
    private final ResourceRepository resourceRepository;
    private final AssignedResourceRepository assignedResourceRepository;

    private final TasksMessageHandler tasksMessageHandler;
    private final ResourceMessageHandler resourcesMessageHandler;
    private final AnalyticsMessageHandler analyticsMessageHandler;
    private final NotificationMessageHandler notificationMessageHandler;
    private final CheckoutMessageHandler checkoutMessageHandler;

    private final CheckOutUtils checkOutUtils;

    private static final Logger LOGGER = Logger.getLogger(AllocationService.class.getName());

    @Autowired
    public AllocationService(TaskRepository taskRepository, TaskAssignmentRepository taskAssignmentRepository,
                             ResourceRepository resourceRepository, AssignedResourceRepository assignedMemberRepository,
                             TasksMessageHandler tasksMessageHandler, ResourceMessageHandler resourcesMessageHandler,
                             AnalyticsMessageHandler analyticsMessageHandler,
                             NotificationMessageHandler notificationMessageHandler,
                             CheckoutMessageHandler checkoutMessageHandler,
                             CheckOutUtils checkOutUtils
                                ) {
        this.taskRepository = taskRepository;
        this.taskAssignmentRepository = taskAssignmentRepository;
        this.resourceRepository = resourceRepository;
        this.assignedResourceRepository = assignedMemberRepository;
        this.tasksMessageHandler = tasksMessageHandler;
        this.resourcesMessageHandler = resourcesMessageHandler;
        this.analyticsMessageHandler = analyticsMessageHandler;
        this.notificationMessageHandler = notificationMessageHandler;
        this.checkoutMessageHandler = checkoutMessageHandler;
        this.checkOutUtils = checkOutUtils;
    }

    public void deallocateAllResources(Task task) {
        //Dealloca tutte le risorse assegnate alla task
        deallocateResources(getActiveTaskAssignment(task.getIdTask()));
    }

    public List<Task> getAvailableTasks() {
        return taskRepository.findByEnabledTrueAndRunningTrue();
    }

    public List<Resource> getAvailableResources() {
        //TODO: Vedere se funziona
        DayOfWeek currentDay = LocalDateTime.now().getDayOfWeek();
        LocalTime currentTime = LocalTime.now();

        //List<Resource> availableResources = resourceRepository.findByIsAvailableTrue();
        List<Resource> availableResources = resourceRepository.findByStatus(Resource.Status.AVAILABLE);

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

        if(taskAssignment == null)
            return;

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
        LOGGER.info("Deallocating resource " + assignedResource.getHardwareId());
        Optional<Resource> retResource = resourceRepository.findById(assignedResource.getHardwareId());
        if (retResource.isEmpty())
            return;

        Resource resource = retResource.get();
        Optional<TaskAssignment> taskAssignment = taskAssignmentRepository.findById(assignedResource.getTaskAssignmentId());

        if(assignedResource.getAssignedTime() == null) {
            assignedResource.setAssignedTime(now);
        } else {
            //Send message of resource deallocation only if the resource has been used
            updateAssignmentData(assignedResource, resource);
        }

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

            LOGGER.info("Starting checkout for resource " + assignedResource.getHardwareId());
            //Send Checkout Message
            checkoutResource(taskAssignmentNew.getIdTask(), resource.getMemberEmail(), assignedResource);

            taskAssignmentRepository.save(taskAssignmentNew);
        }

        assignedResourceRepository.save(assignedResource);

        deallocateResource(resource);


    }

    private void checkoutResource(String idTask, String memberEmail, AssignedResource assignedResource) {
        Optional<Task> task = taskRepository.findByIdTask(idTask);

        if(task.isEmpty())
            return;

        LocalDateTime start = assignedResource.getAssignedTime();
        LocalDateTime end = assignedResource.getCompletedTime();
        double energyPerHour = assignedResource.getAssignedEnergyConsumptionPerHour();
        double computationalPower = assignedResource.getAssignedMultiScore()
                + assignedResource.getAssignedSingleScore()
                + assignedResource.getAssignedOpenclScore()
                + assignedResource.getAssignedVulkanScore()
                + assignedResource.getAssignedCudaScore();

        double credits = checkOutUtils.getCreditAmount(start,end,energyPerHour,computationalPower);

        if(credits <= 0) // Resource not used
            return;

        Task taskNew = task.get();

        checkoutMessageHandler.startCheckout(taskNew.getEmailUtente(), memberEmail, credits);

    }

    public void deallocateResource(Resource resource) {
        //resource.setIsAvailable(true);
        if (resource.getStatus().equals(Resource.Status.BUSY))
            resource.setStatus(Resource.Status.AVAILABLE);

        resource.setCurrentTaskId(null);
        resourceRepository.save(resource);
        sendResourceStatusMessage(resource);
    }

    public void updateTask(Task task) {
        taskRepository.save(task);
        sendTaskStatusMessage(task);
    }

    public void completeTaskAssignment(Task task){
        TaskAssignment taskAssignment = taskAssignmentRepository.findByIdTask(task.getIdTask());
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

        TaskAssignment assignment =  taskAssignmentRepository.findByIdTask(task.getIdTask());

        if ( assignment == null ) {
            LOGGER.info("Task assignment not found, creating new one");
            task.setStartTime(LocalDateTime.now());
            task.setRunning(true);
            updateTask(task);

            assignment = new TaskAssignment();
            assignment.setIdTask(task.getIdTask());
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
        sendTaskStatusMessage(taskAssignment);
        return taskAssignmentRepository.save(taskAssignment);
    }


    public void updateResource(Resource resource) {
        resourceRepository.save(resource);
        sendResourceStatusMessage(resource);
    }

    public AssignedResource assignResource(Resource resource, TaskAssignment taskAssignment) {
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

        assignedResource = assignedResourceRepository.save(assignedResource);

        sendNotificationRequest(
                resource.getMemberEmail(),
                "Resource assignment",
                "The " + resource.getName() + " has been assigned with ID: " + assignedResource.getId() + " and is now in use",
                "",
                SUCCESS_NOTIFICATION_TYPE,
                false,
                true
        );

        // Send Notification to the user
        Optional<Task> task = taskRepository.findByIdTask(taskAssignment.getIdTask());

        AssignedResource finalAssignedResource = assignedResource;
        task.ifPresent(value -> sendNotificationRequest(
                resource.getMemberEmail(),
                "Resource assignment instructions",
                "Download the script from the following link: " + value.getScriptLink()
                        + ".\n The assigned ID is: " + finalAssignedResource.getId() + " \n"
                        + "Download the script and execute it, use the given ID to effectively work on the task",
                "",
                SUCCESS_NOTIFICATION_TYPE,
                true,
                false
        ));

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
        resourceStatusMessageDTO.setId(resource.getIdResource());
        //resourceStatusMessageDTO.setIsAvailable(resource.getIsAvailable());
        resourceStatusMessageDTO.setStatus(ResourceStatusMessageDTO.Status.valueOf(resource.getStatus().name()));
        resourceStatusMessageDTO.setCurrentTaskId(resource.getCurrentTaskId());

        LOGGER.info("Message to Resource with STATUS: " + resourceStatusMessageDTO.getStatus());

        if (resource.getCurrentTaskId() != null) {

            resourcesMessageHandler.handleResourceAssignment(resourceStatusMessageDTO);
        }
        else {
            resourcesMessageHandler.handleResourceDeallocation(resourceStatusMessageDTO);
            sendNotificationRequest(
                    resource.getMemberEmail(),
                    "Resource deallocation",
                    "The " + resource.getName() + " has been deallocated and is now available",
                    "",
                    SUCCESS_NOTIFICATION_TYPE,
                    false,
                    true
            );
        }
    }

    public void sendTaskStatusMessage(Task task) {
        TaskStatusMessageDTO taskStatusMessageDTO = new TaskStatusMessageDTO();
        taskStatusMessageDTO.setId(task.getIdTask());
        taskStatusMessageDTO.setEnabled(task.getEnabled());
        taskStatusMessageDTO.setStartTime(task.getStartTime());
        taskStatusMessageDTO.setEndTime(task.getEndTime());
        taskStatusMessageDTO.setRunning(task.getRunning());

        if (Boolean.TRUE.equals(task.getRunning())) {

            tasksMessageHandler.handleTaskAssignment(taskStatusMessageDTO);
            sendNotificationRequest(
                    task.getEmailUtente(),
                    "Task assignment",
                    "Task " + task.getIdTask() + " has been submitted and prepared for execution",
                    "",
                    INFO_NOTIFICATION_TYPE,
                    false,
                    true
            );
        }
        else {
            tasksMessageHandler.endTaskExecution(taskStatusMessageDTO);
            sendNotificationRequest(
                    task.getEmailUtente(),
                    "Task definitely completed",
                    "All operations for task " + task.getIdTask() + " has been completed",
                    "",
                    SUCCESS_NOTIFICATION_TYPE,
                    false,
                    true
            );
        }
    }

    public void sendTaskStatusMessage(TaskAssignment assignmentTask) {
        TaskStatusMessageDTO taskStatusMessageDTO = new TaskStatusMessageDTO();

        List<String> hardwareIds = assignmentTask.getAssignedResources().stream()
                .map(AssignedResource::getHardwareId)
                .toList();

        taskStatusMessageDTO.setId(assignmentTask.getIdTask());
        taskStatusMessageDTO.setRunning(!assignmentTask.getIsComplete());
        taskStatusMessageDTO.setAssignedResources(hardwareIds);

        if (Boolean.TRUE.equals(assignmentTask.getIsComplete())) {
            taskStatusMessageDTO.setEndTime(assignmentTask.getCompletedTime());
            taskStatusMessageDTO.setEnabled(false);

            Optional<Task> task = taskRepository.findByIdTask(assignmentTask.getIdTask());

            if(task.isEmpty())
                throw new AssignedResourceNotFoundException("Task with id " + assignmentTask.getIdTask() + " not found");

            String email = task.get().getEmailUtente();

            tasksMessageHandler.endTaskExecution(taskStatusMessageDTO);
            sendNotificationRequest(
                    email,
                    "Task completed",
                    "Task " + assignmentTask.getIdTask() + " has been completed",
                    "",
                    SUCCESS_NOTIFICATION_TYPE,
                    false,
                    true
            );
        }
        else {
            tasksMessageHandler.handleTaskAssignment(taskStatusMessageDTO);
            sendNotificationRequest(
                    assignmentTask.getIdTask(),
                    "Task update",
                    "Task " + assignmentTask.getIdTask() + " is now running and has been updated",
                    "",
                    INFO_NOTIFICATION_TYPE,
                    false,
                    true
            );
        }
    }

    //TODO: VEDERE SE FUNZIONA
    public List<Resource> getNotAssignableResources() {
        //return resourceRepository.findByIsAvailableFalse();
        List<Resource> busyResources = resourceRepository.findByStatus(Resource.Status.BUSY);
        List<Resource> unavailableResources = resourceRepository.findByStatus(Resource.Status.UNAVAILABLE);
        List<Resource> all = new ArrayList<>();

        try {
            all.addAll(busyResources);
            all.addAll(unavailableResources);
        } catch (Exception e) {
            LOGGER.info("No resources assigned");
        }

        return all;
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

    public void sendNotificationRequest(String receiver, String subject, String message, String attachment, String type, boolean isEmail, boolean isNotification) {
        notificationMessageHandler.sendNotificationMessage(NotificationMessageHandler
                .buildNotificationMessage(
                        receiver,
                        message,
                        subject,
                        attachment,
                        type,
                        isEmail,
                        isNotification
                ));
    }
}
