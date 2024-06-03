package it.unisalento.pasproject.assignmentservice.dto.task;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class AssignedTaskListDTO {
    private List<AssignedTaskDTO> assignedTaskDTOList;

    public AssignedTaskListDTO() {
        this.assignedTaskDTOList = new ArrayList<>();
    }
}