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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@SpringBootTest(classes = {AllocationService.class})
class AllocationServiceTest {

    @MockBean
    private TaskRepository taskRepository;

    @MockBean
    private TaskAssignmentRepository taskAssignmentRepository;

    @MockBean
    private ResourceRepository resourceRepository;

    @MockBean
    private AssignedResourceRepository assignedResourceRepository;

    @MockBean
    private TasksMessageHandler tasksMessageHandler;

    @MockBean
    private ResourceMessageHandler resourcesMessageHandler;

    @MockBean
    private AnalyticsMessageHandler analyticsMessageHandler;

    @MockBean
    private NotificationMessageHandler notificationMessageHandler;

    @MockBean
    private CheckoutMessageHandler checkoutMessageHandler;

    @MockBean
    private CheckOutUtils checkOutUtils;

    @InjectMocks
    private AllocationService allocationService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        taskAssignmentRepository = mock(TaskAssignmentRepository.class);
        taskRepository = mock(TaskRepository.class);
        resourceRepository = mock(ResourceRepository.class);
        assignedResourceRepository = mock(AssignedResourceRepository.class);
        tasksMessageHandler = mock(TasksMessageHandler.class);
        resourcesMessageHandler = mock(ResourceMessageHandler.class);
        analyticsMessageHandler = mock(AnalyticsMessageHandler.class);
        notificationMessageHandler = mock(NotificationMessageHandler.class);
        checkoutMessageHandler = mock(CheckoutMessageHandler.class);
        checkOutUtils = mock(CheckOutUtils.class);
        allocationService = new AllocationService(taskRepository, taskAssignmentRepository, resourceRepository, assignedResourceRepository, tasksMessageHandler, resourcesMessageHandler, analyticsMessageHandler, notificationMessageHandler, checkoutMessageHandler, checkOutUtils);
    }

    @Test
    void testDeallocateAllResources() {
        Task task = new Task();
        task.setIdTask("task1");

        TaskAssignment assignment = new TaskAssignment();
        assignment.setId("assignment1");

        AssignedResource assignedResource = new AssignedResource();
        assignedResource.setHardwareId("resource1");
        assignedResource.setCompletedTime(LocalDateTime.now().minusHours(1));
        assignedResource.setAssignedTime(LocalDateTime.now().minusHours(2));
        assignment.setAssignedResources(List.of(assignedResource));

        Resource resource = new Resource();
        resource.setId("resource1");
        resource.setStatus(Resource.Status.BUSY);

        when(taskAssignmentRepository.findByIdTaskAndIsCompleteFalse(task.getIdTask())).thenReturn(assignment);
        when(resourceRepository.findById("resource1")).thenReturn(Optional.of(resource));

        allocationService.deallocateAllResources(task);

        verify(taskAssignmentRepository, times(1)).findByIdTaskAndIsCompleteFalse(task.getIdTask());
        verify(resourceRepository, times(1)).findById("resource1");
        verify(resourceRepository, times(1)).save(any(Resource.class));
        verify(assignedResourceRepository, times(1)).save(any(AssignedResource.class));
        verify(taskAssignmentRepository, times(1)).save(any(TaskAssignment.class));
    }


    @Test
    void testDeallocateResources() {
        TaskAssignment assignment = new TaskAssignment();
        assignment.setId("assignment1");

        AssignedResource assignedResource = new AssignedResource();
        assignedResource.setHardwareId("resource1");
        assignedResource.setCompletedTime(LocalDateTime.now().minusHours(1));
        assignedResource.setAssignedTime(LocalDateTime.now().minusHours(2));
        assignment.setAssignedResources(List.of(assignedResource));

        Resource resource = new Resource();
        resource.setId("resource1");
        resource.setStatus(Resource.Status.BUSY);

        when(resourceRepository.findById("resource1")).thenReturn(Optional.of(resource));

        allocationService.deallocateResources(assignment);

        verify(resourceRepository, times(1)).findById("resource1");
        verify(resourceRepository, times(1)).save(any(Resource.class));
        verify(assignedResourceRepository, times(1)).save(any(AssignedResource.class));
        verify(taskAssignmentRepository, times(1)).save(any(TaskAssignment.class));
    }

    @Test
    void testDeallocateResource() {
        AssignedResource assignedResource = new AssignedResource();
        assignedResource.setId("assignedResource1");
        assignedResource.setHardwareId("resource1");
        assignedResource.setCompletedTime(LocalDateTime.now().minusHours(1));
        assignedResource.setAssignedTime(LocalDateTime.now().minusHours(2));
        assignedResource.setTaskAssignmentId("assignment1");

        Resource resource = new Resource();
        resource.setId("resource1");
        resource.setStatus(Resource.Status.BUSY);

        TaskAssignment taskAssignment = new TaskAssignment();
        taskAssignment.setId("assignment1");
        taskAssignment.setAssignedResources(List.of(assignedResource));

        when(resourceRepository.findById("resource1")).thenReturn(Optional.of(resource));
        when(taskAssignmentRepository.findById("assignment1")).thenReturn(Optional.of(taskAssignment));

        allocationService.deallocateResource(assignedResource);

        verify(resourceRepository, times(1)).findById("resource1");
        verify(resourceRepository, times(1)).save(any(Resource.class));
        verify(assignedResourceRepository, times(1)).save(any(AssignedResource.class));
        verify(taskAssignmentRepository, times(1)).save(any(TaskAssignment.class));
    }

    @Test
    void testGetAvailableTasks() {
        Task task1 = new Task();
        Task task2 = new Task();

        when(taskRepository.findByEnabledTrueAndRunningTrue()).thenReturn(List.of(task1, task2));

        List<Task> tasks = allocationService.getAvailableTasks();

        assertEquals(2, tasks.size());
        verify(taskRepository, times(1)).findByEnabledTrueAndRunningTrue();
    }

    @Test
    void testGetAvailableResources() {
        Resource resource1 = new Resource();
        resource1.setId("resource1Id");
        resource1.setIdResource("idResource1");
        resource1.setName("Resource One");
        resource1.setStatus(Resource.Status.AVAILABLE);
        resource1.setCurrentTaskId("task1");
        resource1.setKWh(10.5);
        resource1.setMemberEmail("member1@example.com");
        resource1.setSingleCoreScore(100.0);
        resource1.setMulticoreScore(200.0);
        resource1.setOpenclScore(300.0);
        resource1.setVulkanScore(400.0);
        resource1.setCudaScore(500.0);

        Resource resource2 = new Resource();
        resource2.setId("resource2Id");
        resource2.setIdResource("idResource2");
        resource2.setName("Resource Two");
        resource2.setStatus(Resource.Status.AVAILABLE);
        resource2.setCurrentTaskId("task2");
        resource2.setKWh(20.5);
        resource2.setMemberEmail("member2@example.com");
        resource2.setSingleCoreScore(150.0);
        resource2.setMulticoreScore(250.0);
        resource2.setOpenclScore(350.0);
        resource2.setVulkanScore(450.0);
        resource2.setCudaScore(550.0);

        // Create Availability instances
        Availability availability1 = new Availability();
        availability1.setDayOfWeek(LocalDateTime.now().getDayOfWeek());
        availability1.setStartTime(LocalTime.from(LocalDateTime.now().minusHours(1)));
        availability1.setEndTime(LocalTime.from(LocalDateTime.now().plusMinutes(10)));

        Availability availability2 = new Availability();
        availability2.setDayOfWeek(LocalDateTime.now().getDayOfWeek());
        availability2.setStartTime(LocalTime.from(LocalDateTime.now().minusHours(1)));
        availability2.setEndTime(LocalTime.from(LocalDateTime.now().plusMinutes(10)));

        List<Availability> availabilitiesForResource1 = new ArrayList<>();
        availabilitiesForResource1.add(availability1);
        resource1.setAvailability(availabilitiesForResource1);

        List<Availability> availabilitiesForResource2 = new ArrayList<>();
        availabilitiesForResource2.add(availability2);
        resource2.setAvailability(availabilitiesForResource2);

        when(resourceRepository.findByStatus(any())).thenReturn(List.of(resource1, resource2));

        allocationService.getAvailableResources();

        verify(resourceRepository, times(1)).findByStatus(any());
    }

    @Test
    void testGetRunningTasks() {
        Task task1 = new Task();
        Task task2 = new Task();

        when(taskRepository.findByEnabledTrueAndRunningTrue()).thenReturn(List.of(task1, task2));

        List<Task> tasks = allocationService.getRunningTasks();

        assertEquals(2, tasks.size());
        verify(taskRepository, times(1)).findByEnabledTrueAndRunningTrue();
    }

    @Test
    void testGetActiveTaskAssignments() {
        TaskAssignment assignment1 = new TaskAssignment();
        TaskAssignment assignment2 = new TaskAssignment();

        when(taskAssignmentRepository.findAllByIdTaskAndIsCompleteFalse("task1")).thenReturn(List.of(assignment1, assignment2));

        List<TaskAssignment> assignments = allocationService.getActiveTaskAssignments("task1");

        assertEquals(2, assignments.size());
        verify(taskAssignmentRepository, times(1)).findAllByIdTaskAndIsCompleteFalse("task1");
    }

    @Test
    void testGetActiveTaskAssignmentById() {
        TaskAssignment assignment = new TaskAssignment();

        when(taskAssignmentRepository.findByIdTaskAndIsCompleteFalse("task1")).thenReturn(assignment);

        TaskAssignment result = allocationService.getActiveTaskAssignment("task1");

        assertNotNull(result);
        verify(taskAssignmentRepository, times(1)).findByIdTaskAndIsCompleteFalse("task1");
    }

    @Test
    void testGetActiveTaskAssignmentByResource() {
        Resource resource = new Resource();
        resource.setId("resource1");

        AssignedResource assignedResource = new AssignedResource();
        assignedResource.setTaskAssignmentId("assignment1");

        TaskAssignment taskAssignment = new TaskAssignment();
        taskAssignment.setId("assignment1");

        when(assignedResourceRepository.findByHardwareIdAndHasCompletedFalse("resource1")).thenReturn(List.of(assignedResource));
        when(taskAssignmentRepository.findById("assignment1")).thenReturn(Optional.of(taskAssignment));

        TaskAssignment result = allocationService.getActiveTaskAssignment(resource);

        assertNotNull(result);
        verify(assignedResourceRepository, times(1)).findByHardwareIdAndHasCompletedFalse("resource1");
        verify(taskAssignmentRepository, times(1)).findById("assignment1");
    }


    @Test
    void testUpdateTask() {
        Task task = new Task();
        task.setIdTask("task1");

        allocationService.updateTask(task);

        verify(taskRepository, times(1)).save(task);
    }

    @Test
    void testCompleteTaskAssignmentByTask() {
        Task task = new Task();
        task.setIdTask("task1");

        TaskAssignment assignment = new TaskAssignment();
        assignment.setId("assignment1");
        assignment.setIdTask("task1");
        assignment.setIsComplete(true);

        AssignedResource assignedResource = new AssignedResource();
        assignedResource.setHardwareId("resource1");
        assignedResource.setCompletedTime(LocalDateTime.now().minusHours(1));
        assignedResource.setAssignedTime(LocalDateTime.now().minusHours(2));
        assignment.setAssignedResources(List.of(assignedResource));

        when(taskAssignmentRepository.findByIdTask("task1")).thenReturn(assignment);
        when(taskRepository.findByIdTask("task1")).thenReturn(Optional.of(task));

        allocationService.completeTaskAssignment(task);

        verify(taskAssignmentRepository, times(2)).save(assignment);
    }

    @Test
    void testCompleteTaskAssignmentByAssignment() {
        TaskAssignment assignment = new TaskAssignment();
        assignment.setId("assignment1");

        allocationService.completeTaskAssignment(assignment);

        verify(taskAssignmentRepository, times(1)).save(assignment);
    }

    @Test
    void testGetTaskAssignment() {
        Task task = new Task();
        task.setIdTask("task1");

        when(taskAssignmentRepository.findByIdTask("task1")).thenReturn(null);
        when(taskAssignmentRepository.save(any(TaskAssignment.class))).thenAnswer(i -> i.getArguments()[0]);
        TaskAssignment assignment = allocationService.getTaskAssignment(task);

        assertNotNull(assignment);
        verify(taskAssignmentRepository, times(1)).save(any(TaskAssignment.class));
    }

    @Test
    void testUpdateTaskAssignment() {
        TaskAssignment assignment = new TaskAssignment();
        assignment.setId("assignment1");
        assignment.setIdTask("task1");
        assignment.setIsComplete(true);

        AssignedResource assignedResource = new AssignedResource();
        assignedResource.setHardwareId("resource1");
        assignedResource.setCompletedTime(LocalDateTime.now().minusHours(1));
        assignedResource.setAssignedTime(LocalDateTime.now().minusHours(2));
        assignment.setAssignedResources(List.of(assignedResource));

        Task task = new Task();
        task.setIdTask("task1");

        when(taskRepository.findByIdTask("task1")).thenReturn(Optional.of(task));

        allocationService.updateTaskAssignment(assignment);

        verify(taskAssignmentRepository, times(1)).save(assignment);
    }

    @Test
    void testUpdateResource() {
        Resource resource1 = new Resource();
        resource1.setId("resource1Id");
        resource1.setIdResource("idResource1");
        resource1.setName("Resource One");
        resource1.setStatus(Resource.Status.AVAILABLE);
        resource1.setCurrentTaskId("task1");
        resource1.setKWh(10.5);
        resource1.setMemberEmail("member1@example.com");
        resource1.setSingleCoreScore(100.0);
        resource1.setMulticoreScore(200.0);
        resource1.setOpenclScore(300.0);
        resource1.setVulkanScore(400.0);
        resource1.setCudaScore(500.0);

        allocationService.updateResource(resource1);

        verify(resourceRepository, times(1)).save(resource1);
    }

    @Test
    void testAssignResource() {
        Resource resource = new Resource();
        resource.setId("resource1");
        resource.setName("Test Resource");
        resource.setSingleCoreScore(1000.0);
        resource.setMulticoreScore(2000.0);
        resource.setOpenclScore(3000.0);
        resource.setVulkanScore(4000.0);
        resource.setCudaScore(5000.0);
        resource.setKWh(100.0);
        resource.setMemberEmail("member@example.com");

        Availability availability = new Availability();
        availability.setDayOfWeek(LocalDateTime.now().getDayOfWeek());
        availability.setStartTime(LocalTime.from(LocalDateTime.now()));
        availability.setEndTime(LocalTime.from(LocalDateTime.now().plusHours(1)));

        Availability availability2 = new Availability();
        availability2.setDayOfWeek(LocalDateTime.now().getDayOfWeek());
        availability2.setStartTime(LocalTime.from(LocalDateTime.now()));
        availability2.setEndTime(LocalTime.from(LocalDateTime.now().plusHours(1)));

        resource.setAvailability(List.of(availability, availability2));

        Task task = new Task();
        task.setIdTask("task1");
        task.setEmailUtente("user@example.com");
        task.setScriptLink("http://example.com/script");
        task.setRunning(true);

        TaskAssignment taskAssignment = new TaskAssignment();
        taskAssignment.setId("taskAssignment1");
        taskAssignment.setIdTask("task1");
        when(assignedResourceRepository.save(any(AssignedResource.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(taskRepository.findByIdTask("task1")).thenReturn(Optional.of(task));

        AssignedResource assignedResource = allocationService.assignResource(resource, taskAssignment);

        assertNotNull(assignedResource);
        assertEquals("resource1", assignedResource.getHardwareId());
        assertEquals("taskAssignment1", assignedResource.getTaskAssignmentId());
        assertFalse(assignedResource.isHasCompleted());

        ArgumentCaptor<AssignedResource> captor = ArgumentCaptor.forClass(AssignedResource.class);
        verify(assignedResourceRepository, times(2)).save(captor.capture());

        AssignedResource savedResource = captor.getAllValues().getFirst();
        assertEquals(resource.getSingleCoreScore(), savedResource.getAssignedSingleScore());
        assertEquals(resource.getMulticoreScore(), savedResource.getAssignedMultiScore());
        assertEquals(resource.getOpenclScore(), savedResource.getAssignedOpenclScore());
        assertEquals(resource.getVulkanScore(), savedResource.getAssignedVulkanScore());
        assertEquals(resource.getCudaScore(), savedResource.getAssignedCudaScore());
        assertEquals(resource.getKWh(), savedResource.getAssignedEnergyConsumptionPerHour());

        verify(notificationMessageHandler, times(2)).sendNotificationMessage(any());
    }

    @Test
    void testAssignResourceAvailabilityNotFound() {
        Resource resource = new Resource();
        resource.setId("resource1");
        resource.setName("Test Resource");
        resource.setSingleCoreScore(1000.0);
        resource.setMulticoreScore(2000.0);
        resource.setOpenclScore(3000.0);
        resource.setVulkanScore(4000.0);
        resource.setCudaScore(5000.0);
        resource.setKWh(100.0);
        resource.setMemberEmail("member@example.com");

        TaskAssignment taskAssignment = new TaskAssignment();
        taskAssignment.setId("taskAssignment1");
        taskAssignment.setIdTask("task1");


        Availability availability = new Availability();
        availability.setDayOfWeek(DayOfWeek.TUESDAY);
        availability.setStartTime(LocalTime.of(9, 0));
        availability.setEndTime(LocalTime.of(17, 0));

        resource.setAvailability(List.of(availability));

        assertThrows(Exception.class, () -> allocationService.assignResource(resource, taskAssignment));
    }

    @Test
    void testIsResourceAlreadyAllocated() {
        Resource resource = new Resource();
        resource.setId("resource1");

        when(assignedResourceRepository.existsByHardwareIdAndHasCompletedTrue("resource1")).thenReturn(true);

        boolean result = allocationService.isResourceAlreadyAllocated(resource);

        assertTrue(result);
        verify(assignedResourceRepository, times(1)).existsByHardwareIdAndHasCompletedTrue("resource1");
    }

    @Test
    void testGetResource() {
        Resource resource = new Resource();
        resource.setId("resource1");
        when(resourceRepository.findById("resource1")).thenReturn(Optional.of(resource));

        Optional<Resource> result = allocationService.getResource("resource1");

        assertTrue(result.isPresent());
        verify(resourceRepository, times(1)).findById("resource1");
    }

    @Test
    void testGetAssignedResourceById() {
        AssignedResource assignedResource = new AssignedResource();
        assignedResource.setId("assignedResource1");

        when(assignedResourceRepository.findById("assignedResource1")).thenReturn(Optional.of(assignedResource));

        Optional<AssignedResource> result = allocationService.getAssignedResource("assignedResource1");

        assertTrue(result.isPresent());
        verify(assignedResourceRepository, times(1)).findById("assignedResource1");
    }

    @Test
    void testGetAssignedResourceByResourceAndAssignment() {
        Resource resource = new Resource();
        resource.setId("resource1");

        TaskAssignment assignment = new TaskAssignment();
        assignment.setId("assignment1");

        AssignedResource assignedResource = new AssignedResource();
        assignedResource.setHardwareId("resource1");
        assignedResource.setTaskAssignmentId("assignment1");

        when(assignedResourceRepository.findByHardwareIdAndTaskAssignmentId("resource1", "assignment1"))
                .thenReturn(Optional.of(assignedResource));

        Optional<AssignedResource> result = allocationService.getAssignedResource(resource, assignment);

        assertTrue(result.isPresent());
        verify(assignedResourceRepository, times(1)).findByHardwareIdAndTaskAssignmentId("resource1", "assignment1");
    }

    @Test
    void testSendResourceStatusMessage() {
        Resource resource = new Resource();
        resource.setIdResource("resource1");
        resource.setStatus(Resource.Status.AVAILABLE);
        resource.setCurrentTaskId(null);

        allocationService.sendResourceStatusMessage(resource);

        verify(resourcesMessageHandler, times(1)).handleResourceDeallocation(any(ResourceStatusMessageDTO.class));
    }

    @Test
    void testSendTaskStatusMessage() {
        Task task = new Task();
        task.setIdTask("task1");
        task.setRunning(true);

        allocationService.sendTaskStatusMessage(task);

        verify(tasksMessageHandler, times(1)).handleTaskAssignment(any(TaskStatusMessageDTO.class));
    }

    @Test
    void testSendTaskStatusMessageByAssignment() {
        TaskAssignment assignment = new TaskAssignment();
        assignment.setId("assignment1");
        assignment.setIdTask("task1");
        assignment.setIsComplete(true);

        AssignedResource assignedResource = new AssignedResource();
        assignedResource.setHardwareId("resource1");
        assignedResource.setCompletedTime(LocalDateTime.now().minusHours(1));
        assignedResource.setAssignedTime(LocalDateTime.now().minusHours(2));
        assignment.setAssignedResources(List.of(assignedResource));

        when(taskRepository.findByIdTask("task1")).thenReturn(Optional.of(new Task()));

        allocationService.sendTaskStatusMessage(assignment);

        verify(tasksMessageHandler, times(1)).endTaskExecution(any(TaskStatusMessageDTO.class));
    }

    @Test
    void testUpdateAssignmentDataByAssignedResource() {
        AssignedResource assignedResource = new AssignedResource();
        Resource resource = new Resource();

        allocationService.updateAssignmentData(assignedResource, resource);

        verify(analyticsMessageHandler, times(1)).updateAssignmentData(assignedResource, resource);
    }

    @Test
    void testSendAssignmentDataByAssignedResource() {
        AssignedResource assignedResource = new AssignedResource();
        Resource resource = new Resource();

        allocationService.sendAssignmentData(assignedResource, resource);

        verify(analyticsMessageHandler, times(1)).updateAssignmentData(assignedResource, resource);
    }

    @Test
    void testUpdateAssignmentDataByTaskAssignment() {
        TaskAssignment taskAssignment = new TaskAssignment();

        allocationService.updateAssignmentData(taskAssignment);

        verify(analyticsMessageHandler, times(1)).updateAssignmentData(taskAssignment);
    }

    @Test
    void testSendAssignmentDataByTaskAssignment() {
        TaskAssignment taskAssignment = new TaskAssignment();

        allocationService.sendAssignmentData(taskAssignment);

        verify(analyticsMessageHandler, times(1)).sendAssignmentData(taskAssignment);
    }

    @Test
    void testSendNotificationRequest() {
        allocationService.sendNotificationRequest("receiver@example.com", "subject", "message", "attachment", "type", true, true);

        verify(notificationMessageHandler, times(1)).sendNotificationMessage(any());
    }
}