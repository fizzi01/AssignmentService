package it.unisalento.pasproject.assignmentservice.business.commands;

import it.unisalento.pasproject.assignmentservice.business.assignment.Commands.EnableTaskCommand;
import it.unisalento.pasproject.assignmentservice.domain.Task;
import it.unisalento.pasproject.assignmentservice.service.AllocationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@SpringBootTest(classes = EnableTaskCommand.class)
public class EnableTaskCommandTest {
    @MockBean
    private AllocationService allocationService;

    @MockBean
    private Task task;

    @InjectMocks
    private EnableTaskCommand enableTaskCommand;

    @BeforeEach
    void setUp() {
        task = new Task();
        task.setEnabled(false);
        enableTaskCommand = new EnableTaskCommand(task, allocationService);
    }

    @Test
    void enableTaskWhenDisabled() {
        enableTaskCommand.execute();
        verify(allocationService).updateTask(task);
        assertTrue(task.getEnabled());
    }

    @Test
    void doNotEnableTaskWhenAlreadyEnabled() {
        task.setEnabled(true);
        enableTaskCommand.execute();
        verify(allocationService, never()).updateTask(task);
        assertTrue(task.getEnabled());
    }
}
