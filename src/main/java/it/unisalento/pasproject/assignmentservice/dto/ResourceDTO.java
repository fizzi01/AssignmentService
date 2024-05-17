package it.unisalento.pasproject.assignmentservice.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import it.unisalento.pasproject.assignmentservice.domain.Availability;
import it.unisalento.pasproject.assignmentservice.service.AvailabilityDeserializer;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ResourceDTO {
    private String id;
    @JsonDeserialize(using = AvailabilityDeserializer.class)
    private List<Availability> availability;
    private double kWh;
    private String memberEmail;
    private Boolean isAvailable;
    private String assignedUser;
    private double singleCoreScore;
    private double multicoreScore;
    private double openclScore;
    private double vulkanScore;
    private double cudaScore;

    public ResourceDTO() {}
}
