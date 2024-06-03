package it.unisalento.pasproject.assignmentservice.controllers;

import it.unisalento.pasproject.assignmentservice.domain.TaskAssignment;
import it.unisalento.pasproject.assignmentservice.dto.task.AssignedTaskDTO;
import it.unisalento.pasproject.assignmentservice.dto.task.AssignedTaskListDTO;
import it.unisalento.pasproject.assignmentservice.exceptions.AssignedTaskNotFoundException;
import it.unisalento.pasproject.assignmentservice.service.AssignedTaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static it.unisalento.pasproject.assignmentservice.security.SecurityConstants.ROLE_UTENTE;

@RestController
@RequestMapping("/api/assignments/tasks")
public class AssignedTaskController {
    private final AssignedTaskService assignedTaskService;

    @Autowired
    public AssignedTaskController(AssignedTaskService assignedTaskService) {
        this.assignedTaskService = assignedTaskService;
    }

    @GetMapping(value = "/find/all")
    @Secured(ROLE_UTENTE)
    public AssignedTaskListDTO getAssignedTasks() {
        AssignedTaskListDTO assignedTaskListDTO = assignedTaskService.getAssignedTasks();

        if(assignedTaskListDTO == null) {
            throw new AssignedTaskNotFoundException("No assigned tasks found");
        }

        return assignedTaskListDTO;
    }

    @GetMapping(value = "/find")
    @Secured(ROLE_UTENTE)
    public AssignedTaskListDTO getAssignedTasksByFilter(@RequestParam(required = false) String id,
                                                        @RequestParam(required = false) String idTask,
                                                        @RequestParam(required = false) Boolean isComplete,
                                                        @RequestParam(required = false) LocalDateTime from,
                                                        @RequestParam(required = false) LocalDateTime to) {
        AssignedTaskListDTO assignedTaskListDTO = new AssignedTaskListDTO();
        List<AssignedTaskDTO> assignedTaskList = new ArrayList<>();
        assignedTaskListDTO.setAssignedTaskDTOList(assignedTaskList);

        List<TaskAssignment> taskAssignmentList = assignedTaskService.findAssignedTasks(id, idTask, isComplete, from, to);

        if(taskAssignmentList.isEmpty()) {
            throw new AssignedTaskNotFoundException("No assigned tasks found");
        }

        for (TaskAssignment taskAssignment : taskAssignmentList) {
            assignedTaskList.add(assignedTaskService.getAssignedTaskDTO(taskAssignment));
        }

        return assignedTaskListDTO;
    }
}
