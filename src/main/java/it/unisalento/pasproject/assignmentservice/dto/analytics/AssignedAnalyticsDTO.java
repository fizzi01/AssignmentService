package it.unisalento.pasproject.assignmentservice.dto.analytics;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class AssignedAnalyticsDTO {

    private String taskId;

    private boolean isComplete;
    private String emailUtente;

    private LocalDateTime assignedTime;
    private LocalDateTime completedTime;

    private LocalDateTime lastUpdate;
}
