package it.unisalento.pasproject.assignmentservice.dto.analytics;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AnalyticsMessageDTO {
    private AssignedResourceAnalyticsDTO assignedResource;
    private AssignedAnalyticsDTO assignment;
}
