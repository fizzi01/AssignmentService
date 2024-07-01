package it.unisalento.pasproject.assignmentservice.dto.resource;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResourceStatusMessageDTO {
    public enum Status {
        AVAILABLE,
        BUSY,
        UNAVAILABLE
    }

    private String id;

    //private Boolean isAvailable;
    private Status status;

    private String currentTaskId;
}
