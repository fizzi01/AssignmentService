package it.unisalento.pasproject.assignmentservice.business.assignment;

import it.unisalento.pasproject.assignmentservice.domain.AssignedResource;
import it.unisalento.pasproject.assignmentservice.domain.Resource;
import it.unisalento.pasproject.assignmentservice.domain.Task;
import it.unisalento.pasproject.assignmentservice.domain.TaskAssignment;
import it.unisalento.pasproject.assignmentservice.service.AllocationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@SpringBootTest(classes = AssignmentWatcher.class)
public class AssignmentWatcherTest {
    @MockBean
    private AllocationService allocationService;

    @InjectMocks
    private AssignmentWatcher assignmentWatcher;

    @BeforeEach
    void setUp() {
        assignmentWatcher = new AssignmentWatcher(allocationService);
    }

    @Test
    void testWatch_TaskExpired() {
        Task task = new Task();
        task.setId("1");
        task.setRunning(true);
        task.setTaskDuration(0.30);
        task.setStartTime(LocalDateTime.now().minusMinutes(1));
        task.setEndTime(LocalDateTime.now());

        when(allocationService.getRunningTasks()).thenReturn(Collections.singletonList(task));
        doNothing().when(allocationService).updateTask(any(Task.class));
        doNothing().when(allocationService).deallocateResource(any(AssignedResource.class));

        assignmentWatcher.watch();

        verify(allocationService, times(1)).getRunningTasks();
        verify(allocationService, times(1)).updateTask(any(Task.class));
    }

    @Test
    void testWatch_AllResourcesFinished() {
        Task task = new Task();
        task.setId("1");
        task.setRunning(true);
        task.setTaskDuration(6000.0); // Durata fittizia per assicurarsi che la task non sia scaduta
        task.setStartTime(LocalDateTime.now().minusMinutes(10)); // Inizio nel passato per evitare che sia scaduta

        AssignedResource finishedResource1 = new AssignedResource();
        finishedResource1.setHasCompleted(true);
        AssignedResource finishedResource2 = new AssignedResource();
        finishedResource2.setCompletedTime(LocalDateTime.now().minusMinutes(5)); // Completato nel passato

        TaskAssignment taskAssignment = new TaskAssignment();
        taskAssignment.setAssignedResources(List.of(finishedResource1, finishedResource2));

        when(allocationService.getRunningTasks()).thenReturn(Collections.singletonList(task));
        when(allocationService.getActiveTaskAssignment(task.getId())).thenReturn(taskAssignment);

        doNothing().when(allocationService).completeTaskAssignment(any(Task.class));
        doNothing().when(allocationService).updateTask(any(Task.class));

        assignmentWatcher.watch();

        verify(allocationService, times(1)).getRunningTasks();
        verify(allocationService, times(1)).completeTaskAssignment(task);
        verify(allocationService, times(1)).updateTask(task);
    }

    @Test
    void testWatch_DeallocateForcedResources() {
        AssignedResource assignedResource = new AssignedResource();
        assignedResource.setId("1");

        when(allocationService.getAssignedMembers()).thenReturn(Collections.singletonList(assignedResource));
        doNothing().when(allocationService).deallocateResource(any(AssignedResource.class));

        assignmentWatcher.watch();

        verify(allocationService, times(1)).getAssignedMembers();
        verify(allocationService, times(1)).deallocateResource(any(AssignedResource.class));
    }

    @Test
    void testWatch_DeallocateNotAssignableResources() {
        Resource resource = new Resource();
        resource.setId("1");

        TaskAssignment taskAssignment = new TaskAssignment();
        taskAssignment.setIsComplete(true);

        when(allocationService.getNotAssignableResources()).thenReturn(Collections.singletonList(resource));
        when(allocationService.getActiveTaskAssignment(any(Resource.class))).thenReturn(taskAssignment);
        when(allocationService.getAssignedResource(any(Resource.class), any(TaskAssignment.class)))
                .thenReturn(Optional.empty());
        doNothing().when(allocationService).deallocateResource(any(Resource.class));

        assignmentWatcher.watch();

        verify(allocationService, times(1)).getNotAssignableResources();
        verify(allocationService, times(1)).getActiveTaskAssignment(any(Resource.class));
        verify(allocationService, times(1)).getAssignedResource(any(Resource.class), any(TaskAssignment.class));
        verify(allocationService, times(1)).deallocateResource(any(Resource.class));
    }
}
