package it.unisalento.pasproject.assignmentservice.controllers;

import it.unisalento.pasproject.assignmentservice.domain.AssignedResource;
import it.unisalento.pasproject.assignmentservice.domain.Task;
import it.unisalento.pasproject.assignmentservice.dto.resource.AssignedResourceListDTO;
import it.unisalento.pasproject.assignmentservice.dto.task.TaskDTO;
import it.unisalento.pasproject.assignmentservice.dto.task.TaskListDTO;
import it.unisalento.pasproject.assignmentservice.repositories.AssignedResourceRepository;
import it.unisalento.pasproject.assignmentservice.repositories.TaskRepository;
import it.unisalento.pasproject.assignmentservice.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/tasks/assignment")
public class TestController {

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private AssignedResourceRepository assignedResourceRepository;

    @Autowired
    private TaskService taskService;

    @GetMapping(value="/find/all")
    public TaskListDTO getAllTasks() {
        TaskListDTO taskList = new TaskListDTO();
        List<TaskDTO> list = new ArrayList<>();
        taskList.setTasks(list);

        List<Task> tasks = taskRepository.findAll();

        for (Task task : tasks){
            TaskDTO taskDTO = taskService.getTaskDTO(task);

            list.add(taskDTO);
        }

        return taskList;
    }

    @GetMapping(value="/find/all/ass")
    public List<AssignedResource> getAssignedResources(){
        return assignedResourceRepository.findAll();
    }
}
