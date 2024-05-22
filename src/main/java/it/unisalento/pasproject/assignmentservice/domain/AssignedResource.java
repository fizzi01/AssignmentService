package it.unisalento.pasproject.assignmentservice.domain;

import com.mongodb.lang.NonNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Getter
@Setter
@Document(collection = "assignedMember")
public class AssignedResource {
    @Id
    private String id;

    private String hardwareId;

    //TODO: DA SINCRONIZZARE CON I MEMBRI
    private int assignedComputingPower;
    private double assignedEnergyConsumptionPerHour;
    private long assignedWorkingTimeInSeconds;

    private LocalDateTime assignedTime;
    private LocalDateTime completedTime; //Calcolato a partire dall'assigned + il tempo di utilizzo max della risorsa
    private boolean hasCompleted;

}
