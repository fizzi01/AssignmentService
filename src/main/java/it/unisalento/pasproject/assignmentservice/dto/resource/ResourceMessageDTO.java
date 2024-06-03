package it.unisalento.pasproject.assignmentservice.dto.resource;

import it.unisalento.pasproject.assignmentservice.domain.Availability;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ResourceMessageDTO {
    private String id;
    private String name;
    private List<Availability> availability;
    private double kWh;
    private String memberEmail;
    private Boolean isAvailable;
    private String currentTaskId;
    private double singleCoreScore;
    private double multicoreScore;
    private double openclScore;
    private double vulkanScore;
    private double cudaScore;

    public ResourceMessageDTO() {}
}
