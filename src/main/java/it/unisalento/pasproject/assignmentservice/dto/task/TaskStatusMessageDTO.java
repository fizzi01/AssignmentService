package it.unisalento.pasproject.assignmentservice.dto.task;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class TaskStatusMessageDTO {
    private String id;

    private Boolean running;

    private Boolean enabled;

    private List<String> assignedResources;

    private LocalDateTime startTime;

    private LocalDateTime endTime;
}
