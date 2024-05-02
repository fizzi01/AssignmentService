package it.unisalento.pasproject.assignmentservice.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class TaskListDTO {

    private List<TaskDTO> tasks;

    public TaskListDTO() {
        this.tasks = new ArrayList<>();
    }
}
