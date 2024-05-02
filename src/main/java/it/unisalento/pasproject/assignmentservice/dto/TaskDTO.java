package it.unisalento.pasproject.assignmentservice.dto;

import com.mongodb.lang.NonNull;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class TaskDTO {

    private String idTask;

    /**
     * The email of the user who created the task.
     */

    private String emailUtente;

    /**
     * The maximum computing power that the task can use.
     */

    private Double maxComputingPower;

    /**
     * The maximum cuda power that the task can use.
     */
    private double maxCudaPower;

    /**
     * The minimum cuda power that the task requires.
     */
    private double minCudaPower;

    /**
     * The expected duration of the task, in seconds.
     */
    private Double taskDuration;

    /**
     * The maximum energy consumption of the task.
     */
    private Double maxEnergyConsumption;

    /**
     * The minimum computing power that the task requires.
     */
    private Double minComputingPower;

    /**
     * The minimum energy consumption of the task.
     */
    private Double minEnergyConsumption;

    /**
     * The minimum working time of the task, in seconds.
     */
    private Double minWorkingTime;

    /**
     * The current status of the task.
     * If true, the task is currently running. If false, the task is not currently running.
     */
    private Boolean running;

    /**
     * The enabled status of the task.
     * If true, the task is enabled. If false, the task is disabled.
     */
    private Boolean enabled;

}
