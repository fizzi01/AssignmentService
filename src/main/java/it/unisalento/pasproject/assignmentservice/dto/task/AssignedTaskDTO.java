package it.unisalento.pasproject.assignmentservice.dto.task;

import it.unisalento.pasproject.assignmentservice.domain.AssignedResource;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class AssignedTaskDTO {
    private String id;

    private String idTask;

    private List<AssignedResource> assignedResources;

    //Va aggiornato quando tutti i membri hanno completato il task
    private Boolean isComplete;
    private LocalDateTime completedTime;
}
