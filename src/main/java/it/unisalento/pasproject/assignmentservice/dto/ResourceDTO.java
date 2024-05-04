package it.unisalento.pasproject.assignmentservice.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResourceDTO {
    private String id;
    private int availableHours;
    private double kWh;
    private String memberEmail;
    private Boolean isAvailable;
    private String assignedUser;
    private double tdp;
    private double singleCoreScore;
    private double multicoreScore;
    private double openclScore;
    private double vulkanScore;
    private double cudaScore;

    public ResourceDTO() {}
}
