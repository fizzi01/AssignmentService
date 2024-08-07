package it.unisalento.pasproject.assignmentservice.domain;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Getter
@Setter
@Document(collection = "resource")
public class Resource {
    public enum Status {
        AVAILABLE,
        BUSY,
        UNAVAILABLE
    }

    @Id
    private String id;
    private String idResource;
    private String name;

    private List<Availability> availability;
    private double kWh;
    private String memberEmail;

    // Campi da aggiornare in base all'assegnazione
    private Status status;
    private String currentTaskId;

    private double singleCoreScore;
    private double multicoreScore;
    private double openclScore;
    private double vulkanScore;
    private double cudaScore;

}