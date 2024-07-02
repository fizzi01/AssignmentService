package it.unisalento.pasproject.assignmentservice.business.assignment.Commands;

import it.unisalento.pasproject.assignmentservice.domain.Task;
import it.unisalento.pasproject.assignmentservice.service.AllocationService;

public class EnableTaskCommand implements Command {

    private final Task task;
    private final AllocationService allocationService;

    public EnableTaskCommand(Task task, AllocationService allocationService) {
        this.task = task;
        this.allocationService = allocationService;
    }

    @Override
    public void execute() {
        if (Boolean.FALSE.equals(task.getEnabled())) {
            task.setEnabled(true);
            allocationService.updateTask(task);
        }
    }
}
