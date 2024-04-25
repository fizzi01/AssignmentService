package it.unisalento.pasproject.assignmentservice.domain;

import com.mongodb.lang.NonNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@Document(collection = "assignedMember")
public class AssignedMember {
    @Id
    private String id;

    @NonNull
    private String memberId;
    @NonNull
    private String hardwareId;

    //TODO: DA SINCRONIZZARE CON I MEMBRI
    private int assignedComputingPower;
    private double assignedEnergyConsumptionPerHour;
    private long assignedWorkingTimeInSeconds;

    public AssignedMember() {
    }

    public AssignedMember(String id,@NonNull String memberId,@NonNull String hardwareId, int assignedComputingPower, double assignedEnergyConsumptionPerHour, long assignedWorkingTimeInSeconds) {
        this.id = id;
        this.memberId = memberId;
        this.hardwareId = hardwareId;
        this.assignedComputingPower = assignedComputingPower;
        this.assignedEnergyConsumptionPerHour = assignedEnergyConsumptionPerHour;
        this.assignedWorkingTimeInSeconds = assignedWorkingTimeInSeconds;
    }
}
