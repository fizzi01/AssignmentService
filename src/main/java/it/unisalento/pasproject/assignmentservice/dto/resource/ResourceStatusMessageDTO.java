package it.unisalento.pasproject.assignmentservice.dto.resource;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResourceStatusMessageDTO {
    private String id;

    private Boolean isAvailable;

    private String currentTaskId;
}
