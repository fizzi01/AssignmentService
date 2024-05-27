package it.unisalento.pasproject.assignmentservice.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class AssignedResourceDTO {
    private String id;

    private String hardwareId;

    private double assignedSingleScore;
    private double assignedMultiScore;
    private double assignedOpenclScore;
    private double assignedVulkanScore;
    private double assignedCudaScore;

    private double assignedEnergyConsumptionPerHour;

    private long assignedWorkingTimeInSeconds;

    //Viene assegnato solo quando il payload è stato avviato
    private LocalDateTime assignedTime;
    private LocalDateTime completedTime; //Calcolato a partire dall'assigned + il tempo di utilizzo max della risorsa
    private boolean hasCompleted;
}
