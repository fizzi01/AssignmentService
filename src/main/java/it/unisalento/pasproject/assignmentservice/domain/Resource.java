package it.unisalento.pasproject.assignmentservice.domain;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@Document(collection = "resource")
public class Resource {
    @Id
    private String id;
    private String idResource;
    private int availableHours;
    private double kWh;
    private String memberEmail;

    // Campi da aggiornare in base all'assegnazione
    private Boolean isAvailable;
    private String assignedUser;

    private double singleCoreScore;
    private double multicoreScore;
    private double openclScore;
    private double vulkanScore;
    private double cudaScore;

}