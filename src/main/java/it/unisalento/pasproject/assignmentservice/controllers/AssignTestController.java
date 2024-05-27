package it.unisalento.pasproject.assignmentservice.controllers;

import it.unisalento.pasproject.assignmentservice.domain.TaskAssignment;
import it.unisalento.pasproject.assignmentservice.repositories.TaskAssignmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/test/assignment")
public class AssignTestController {
    @Autowired
    private TaskAssignmentRepository taskAssignmentRepository;

    @GetMapping(value="/tasks")
    public List<TaskAssignment> getAllTaskAssignments() {
        return taskAssignmentRepository.findAll();
    }
}
