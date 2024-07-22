package it.unisalento.pasproject.assignmentservice.business.commands;

import it.unisalento.pasproject.assignmentservice.business.assignment.Commands.AssignResourceCommand;
import it.unisalento.pasproject.assignmentservice.domain.*;
import it.unisalento.pasproject.assignmentservice.service.AllocationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@SpringBootTest(classes = AssignResourceCommand.class)
class AssignResourceCommandTest {
    @MockBean
    private Task task;

    @MockBean
    private AllocationService allocationService;

    @InjectMocks
    private AssignResourceCommand assignResourceCommand;

    private Resource resource1, resource2;

    private AssignedResource assignedResource1, assignedResource2;

    private TaskAssignment taskAssignment;

    private TaskAssignment updatedTaskAssignment;

    @BeforeEach
    void setUp() {
        // Configurazione mock Task
        task = new Task();
        task.setId("1");
        task.setIdTask("1");
        task.setEmailUtente("test@example.com");
        task.setMaxComputingPower(1100.0);
        task.setMinComputingPower(1.0);
        task.setMaxCudaPower(1100.0);
        task.setMinCudaPower(1.0);
        task.setTaskDuration(50.0);
        task.setMaxEnergyConsumption(50.0);
        task.setMinEnergyConsumption(20.0);
        task.setMinWorkingTime(45.0);
        task.setRunning(false);
        task.setEnabled(false);

        Availability availability1 = new Availability();
        availability1.setDayOfWeek(DayOfWeek.MONDAY);
        availability1.setStartTime(LocalTime.parse("00:00"));
        availability1.setEndTime(LocalTime.parse("23:30"));

        Availability availability2 = new Availability();
        availability2.setDayOfWeek(DayOfWeek.TUESDAY);
        availability2.setStartTime(LocalTime.parse("00:00"));
        availability2.setEndTime(LocalTime.parse("23:30"));

        Availability availability3 = new Availability();
        availability3.setDayOfWeek(DayOfWeek.WEDNESDAY);
        availability3.setStartTime(LocalTime.parse("00:00"));
        availability3.setEndTime(LocalTime.parse("23:30"));

        Availability availability4 = new Availability();
        availability4.setDayOfWeek(DayOfWeek.THURSDAY);
        availability4.setStartTime(LocalTime.parse("00:00"));
        availability4.setEndTime(LocalTime.parse("23:30"));

        Availability availability5 = new Availability();
        availability5.setDayOfWeek(DayOfWeek.FRIDAY);
        availability5.setStartTime(LocalTime.parse("00:00"));
        availability5.setEndTime(LocalTime.parse("23:30"));

        Availability availability6 = new Availability();
        availability6.setDayOfWeek(DayOfWeek.SATURDAY);
        availability6.setStartTime(LocalTime.parse("00:00"));
        availability6.setEndTime(LocalTime.parse("23:30"));

        Availability availability7 = new Availability();
        availability7.setDayOfWeek(DayOfWeek.SUNDAY);
        availability7.setStartTime(LocalTime.parse("00:00"));
        availability7.setEndTime(LocalTime.parse("23:30"));

        // Configurazione mock Resource
        resource1 = new Resource();
        resource1.setId("1");
        resource1.setIdResource("1");
        resource1.setName("Test Resource");
        resource1.setAvailability(List.of(availability1, availability2, availability3, availability4, availability5, availability6, availability7));
        resource1.setKWh(1.0);
        resource1.setMemberEmail("test@resoure.com");
        resource1.setStatus(Resource.Status.AVAILABLE);
        resource1.setCurrentTaskId(null);
        resource1.setSingleCoreScore(1100.0);
        resource1.setMulticoreScore(1100.0);
        resource1.setOpenclScore(40.0);
        resource1.setVulkanScore(45.0);
        resource1.setCudaScore(1100.0);

        resource2 = new Resource();
        resource2.setId("2");
        resource2.setIdResource("2");
        resource2.setName("Test Resource");
        resource2.setAvailability(List.of(availability1, availability2, availability3, availability4, availability5, availability6, availability7));
        resource2.setKWh(100.0);
        resource2.setMemberEmail("test@resoure.com");
        resource2.setStatus(Resource.Status.AVAILABLE);
        resource2.setCurrentTaskId(null);
        resource2.setSingleCoreScore(1100.0);
        resource2.setMulticoreScore(1100.0);
        resource2.setOpenclScore(400.0);
        resource2.setVulkanScore(450.0);
        resource2.setCudaScore(1100.0);

        // Configurazione mock AssignedResource
        assignedResource1 = new AssignedResource();
        assignedResource1.setId("1");
        assignedResource1.setTaskAssignmentId(null);
        assignedResource1.setHardwareId(null);
        assignedResource1.setAssignedSingleScore(1100.0);
        assignedResource1.setAssignedMultiScore(1100.0);
        assignedResource1.setAssignedOpenclScore(40.0);
        assignedResource1.setAssignedVulkanScore(45.0);
        assignedResource1.setAssignedCudaScore(1100.0);
        assignedResource1.setAssignedEnergyConsumptionPerHour(1.0);
        assignedResource1.setAssignedWorkingTimeInSeconds(0);
        assignedResource1.setAssignedTime(null);
        assignedResource1.setCompletedTime(null);
        assignedResource1.setHasCompleted(false);

        assignedResource2 = new AssignedResource();
        assignedResource2.setId("2");
        assignedResource2.setTaskAssignmentId(null);
        assignedResource2.setHardwareId(null);
        assignedResource2.setAssignedSingleScore(1100.0);
        assignedResource2.setAssignedMultiScore(1100.0);
        assignedResource2.setAssignedOpenclScore(40.0);
        assignedResource2.setAssignedVulkanScore(45.0);
        assignedResource2.setAssignedCudaScore(1100.0);
        assignedResource2.setAssignedEnergyConsumptionPerHour(1.0);
        assignedResource2.setAssignedWorkingTimeInSeconds(0);
        assignedResource2.setAssignedTime(null);
        assignedResource2.setCompletedTime(null);
        assignedResource2.setHasCompleted(false);

        // Configurazione mock TaskAssignment
        taskAssignment = new TaskAssignment();
        taskAssignment.setId("1");
        taskAssignment.setIdTask("1");
        taskAssignment.setAssignedResources(Collections.emptyList());
        taskAssignment.setIsComplete(false);
        taskAssignment.setCompletedTime(null);

        updatedTaskAssignment = new TaskAssignment();
        updatedTaskAssignment.setId("1");
        updatedTaskAssignment.setIdTask("1");
        updatedTaskAssignment.setAssignedResources(List.of(assignedResource1));
        updatedTaskAssignment.setIsComplete(false);
        updatedTaskAssignment.setCompletedTime(null);
    }

    @Test
    void assignSingleResourceToTaskSuccessfully() {
        resource1.setKWh(22.0);
        List<Resource> resources = Collections.singletonList(resource1);
        assignResourceCommand = new AssignResourceCommand(task, resources, allocationService);

        when(allocationService.getTaskAssignment(task)).thenReturn(taskAssignment);
        when(allocationService.assignResource(resource1, taskAssignment)).thenReturn(assignedResource1);
        when(allocationService.updateTaskAssignment(taskAssignment)).thenReturn(updatedTaskAssignment);

        assignResourceCommand.execute();

        assertEquals(Resource.Status.BUSY, resource1.getStatus());
        assertEquals(task.getId(), resource1.getCurrentTaskId());
    }

    @Test
    void notAssignSingleResourceToTaskSuccessfully() {
        List<Resource> resources = Collections.singletonList(resource2);
        assignResourceCommand = new AssignResourceCommand(task, resources, allocationService);

        when(allocationService.getTaskAssignment(task)).thenReturn(taskAssignment);
        when(allocationService.assignResource(resource2, taskAssignment)).thenReturn(assignedResource1);
        when(allocationService.updateTaskAssignment(taskAssignment)).thenReturn(updatedTaskAssignment);

        assignResourceCommand.execute();

        assertEquals(Resource.Status.AVAILABLE, resource2.getStatus());
        assertNull(resource2.getCurrentTaskId());
    }

    @Test
    void shouldSkipResourceWhenAlreadyAssigned() {
        assignedResource1.setHardwareId(resource1.getId());
        taskAssignment.setAssignedResources(Collections.singletonList(assignedResource1));

        List<Resource> resources = Collections.singletonList(resource1);
        assignResourceCommand = new AssignResourceCommand(task, resources, allocationService);

        when(allocationService.getTaskAssignment(task)).thenReturn(taskAssignment);
        when(allocationService.assignResource(resource1, taskAssignment)).thenReturn(assignedResource1);
        when(allocationService.updateTaskAssignment(taskAssignment)).thenReturn(updatedTaskAssignment);

        assignResourceCommand.execute();

        assertEquals(Resource.Status.AVAILABLE, resource1.getStatus());
        assertNull(resource1.getCurrentTaskId());
    }

    @Test
    void shouldSkipResourceWhenMaxComputingPowerReached() {
        assignedResource1.setHardwareId(resource1.getId());
        taskAssignment.setAssignedResources(Collections.singletonList(assignedResource1));

        List<Resource> resources = Collections.singletonList(resource2);
        assignResourceCommand = new AssignResourceCommand(task, resources, allocationService);

        when(allocationService.getTaskAssignment(task)).thenReturn(taskAssignment);
        when(allocationService.assignResource(resource2, taskAssignment)).thenReturn(assignedResource2);
        when(allocationService.updateTaskAssignment(taskAssignment)).thenReturn(updatedTaskAssignment);

        assignResourceCommand.execute();

        assertEquals(Resource.Status.AVAILABLE, resource2.getStatus());
        assertNull(resource2.getCurrentTaskId());
    }

    @Test
    void shouldSkipResourceWhenMaxCudaPowerReached() {
        assignedResource1.setAssignedSingleScore(500.0);
        assignedResource1.setAssignedMultiScore(500.0);
        assignedResource1.setHardwareId(resource1.getId());
        taskAssignment.setAssignedResources(Collections.singletonList(assignedResource1));

        List<Resource> resources = Collections.singletonList(resource2);
        assignResourceCommand = new AssignResourceCommand(task, resources, allocationService);

        when(allocationService.getTaskAssignment(task)).thenReturn(taskAssignment);
        when(allocationService.assignResource(resource2, taskAssignment)).thenReturn(assignedResource2);
        when(allocationService.updateTaskAssignment(taskAssignment)).thenReturn(updatedTaskAssignment);

        assignResourceCommand.execute();

        assertEquals(Resource.Status.AVAILABLE, resource2.getStatus());
        assertNull(resource2.getCurrentTaskId());
    }

    @Test
    void resourceIsNotSuitableWhenBusy() {
        resource1.setStatus(Resource.Status.BUSY);
        List<Resource> resources = Collections.singletonList(resource1);
        assignResourceCommand = new AssignResourceCommand(task, resources, allocationService);

        when(allocationService.getTaskAssignment(task)).thenReturn(taskAssignment);
        when(allocationService.assignResource(resource1, taskAssignment)).thenReturn(assignedResource1);
        when(allocationService.updateTaskAssignment(taskAssignment)).thenReturn(updatedTaskAssignment);

        assignResourceCommand.execute();

        assertEquals(Resource.Status.BUSY, resource1.getStatus());
        assertNull(resource1.getCurrentTaskId());
    }

    @Test
    void resourceIsNotSuitableWhenUnavailable() {
        resource1.setStatus(Resource.Status.UNAVAILABLE);
        List<Resource> resources = Collections.singletonList(resource1);
        assignResourceCommand = new AssignResourceCommand(task, resources, allocationService);

        when(allocationService.getTaskAssignment(task)).thenReturn(taskAssignment);
        when(allocationService.assignResource(resource1, taskAssignment)).thenReturn(assignedResource1);
        when(allocationService.updateTaskAssignment(taskAssignment)).thenReturn(updatedTaskAssignment);

        assignResourceCommand.execute();

        assertEquals(Resource.Status.UNAVAILABLE, resource1.getStatus());
        assertNull(resource1.getCurrentTaskId());
    }

    @Test
    void resourceIsNotSuitableWhenMinWorkingTimeExceeded() {
        resource1.setAvailability(Collections.emptyList());
        List<Resource> resources = Collections.singletonList(resource1);
        assignResourceCommand = new AssignResourceCommand(task, resources, allocationService);

        when(allocationService.getTaskAssignment(task)).thenReturn(taskAssignment);
        when(allocationService.assignResource(resource1, taskAssignment)).thenReturn(assignedResource1);
        when(allocationService.updateTaskAssignment(taskAssignment)).thenReturn(updatedTaskAssignment);

        assignResourceCommand.execute();

        assertEquals(Resource.Status.AVAILABLE, resource1.getStatus());
        assertNull(resource1.getCurrentTaskId());
    }

    @Test
    void resourceIsNotSuitableWhenMaxEnergyConsumptionNotMet() {
        List<Resource> resources = Collections.singletonList(resource2);
        assignResourceCommand = new AssignResourceCommand(task, resources, allocationService);

        when(allocationService.getTaskAssignment(task)).thenReturn(taskAssignment);
        when(allocationService.assignResource(resource2, taskAssignment)).thenReturn(assignedResource2);
        when(allocationService.updateTaskAssignment(taskAssignment)).thenReturn(updatedTaskAssignment);

        assignResourceCommand.execute();

        assertEquals(Resource.Status.AVAILABLE, resource2.getStatus());
        assertNull(resource2.getCurrentTaskId());
    }

    @Test
    void resourceIsNotSuitableWhenProjectedComputingPowerExceeded() {
        assignedResource1.setAssignedEnergyConsumptionPerHour(1.0);
        assignedResource1.setAssignedSingleScore(500.0);
        assignedResource1.setAssignedMultiScore(500.0);
        assignedResource1.setHardwareId(resource1.getId());
        taskAssignment.setAssignedResources(Collections.singletonList(assignedResource1));

        List<Resource> resources = Collections.singletonList(resource2);
        assignResourceCommand = new AssignResourceCommand(task, resources, allocationService);

        when(allocationService.getTaskAssignment(task)).thenReturn(taskAssignment);
        when(allocationService.assignResource(resource2, taskAssignment)).thenReturn(assignedResource2);
        when(allocationService.updateTaskAssignment(taskAssignment)).thenReturn(updatedTaskAssignment);

        assignResourceCommand.execute();

        assertEquals(Resource.Status.AVAILABLE, resource2.getStatus());
        assertNull(resource2.getCurrentTaskId());
    }

    @Test
    void resourceIsNotSuitableWhenMinComputingPowerExceeded() {
        resource2.setSingleCoreScore(0.0);
        resource2.setMulticoreScore(0.0);

        List<Resource> resources = Collections.singletonList(resource2);
        assignResourceCommand = new AssignResourceCommand(task, resources, allocationService);

        when(allocationService.getTaskAssignment(task)).thenReturn(taskAssignment);
        when(allocationService.assignResource(resource2, taskAssignment)).thenReturn(assignedResource2);
        when(allocationService.updateTaskAssignment(taskAssignment)).thenReturn(updatedTaskAssignment);

        assignResourceCommand.execute();

        assertEquals(Resource.Status.AVAILABLE, resource2.getStatus());
        assertNull(resource2.getCurrentTaskId());
    }

    @Test
    void resourceIsNotSuitableWhenMaxComputingPowerExceeded() {
        resource2.setSingleCoreScore(1200.0);
        resource2.setMulticoreScore(1200.0);

        List<Resource> resources = Collections.singletonList(resource2);
        assignResourceCommand = new AssignResourceCommand(task, resources, allocationService);

        when(allocationService.getTaskAssignment(task)).thenReturn(taskAssignment);
        when(allocationService.assignResource(resource2, taskAssignment)).thenReturn(assignedResource2);
        when(allocationService.updateTaskAssignment(taskAssignment)).thenReturn(updatedTaskAssignment);

        assignResourceCommand.execute();

        assertEquals(Resource.Status.AVAILABLE, resource2.getStatus());
        assertNull(resource2.getCurrentTaskId());
    }

    @Test
    void resourceIsNotSuitableWhenMinCudaPowerExceeded() {
        resource2.setCudaScore(0.0);

        List<Resource> resources = Collections.singletonList(resource2);
        assignResourceCommand = new AssignResourceCommand(task, resources, allocationService);

        when(allocationService.getTaskAssignment(task)).thenReturn(taskAssignment);
        when(allocationService.assignResource(resource2, taskAssignment)).thenReturn(assignedResource2);
        when(allocationService.updateTaskAssignment(taskAssignment)).thenReturn(updatedTaskAssignment);

        assignResourceCommand.execute();

        assertEquals(Resource.Status.AVAILABLE, resource2.getStatus());
        assertNull(resource2.getCurrentTaskId());
    }

    @Test
    void resourceIsNotSuitableWhenMaxCudaPowerExceeded() {
        resource2.setCudaScore(1200.0);

        List<Resource> resources = Collections.singletonList(resource2);
        assignResourceCommand = new AssignResourceCommand(task, resources, allocationService);

        when(allocationService.getTaskAssignment(task)).thenReturn(taskAssignment);
        when(allocationService.assignResource(resource2, taskAssignment)).thenReturn(assignedResource2);
        when(allocationService.updateTaskAssignment(taskAssignment)).thenReturn(updatedTaskAssignment);

        assignResourceCommand.execute();

        assertEquals(Resource.Status.AVAILABLE, resource2.getStatus());
        assertNull(resource2.getCurrentTaskId());
    }

    @Test
    void resourceIsNotSuitableWhenTaskDurationExceeded() {
        task.setTaskDuration(3700.0);

        List<Resource> resources = Collections.singletonList(resource1);
        assignResourceCommand = new AssignResourceCommand(task, resources, allocationService);

        when(allocationService.getTaskAssignment(task)).thenReturn(taskAssignment);
        when(allocationService.assignResource(resource1, taskAssignment)).thenReturn(assignedResource1);
        when(allocationService.updateTaskAssignment(taskAssignment)).thenReturn(updatedTaskAssignment);

        assignResourceCommand.execute();

        assertEquals(Resource.Status.AVAILABLE, resource1.getStatus());
        assertNull(resource1.getCurrentTaskId());
    }
}
