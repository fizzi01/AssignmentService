package it.unisalento.pasproject.assignmentservice.domain;

import com.mongodb.lang.NonNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

// DEVE CONTENERE SOLO LE informazioni necessarie per runnare la business logic dell'assegnazione
// tutte le altre informazioni sono contenute altrove.
// QUINDI deve contenere informazioni sui filtri della task e sullo stato della task
// Un altro documento terrà traccia delle assegnazione e delle performance dei membri assegnati alle task
@Getter
@Setter
@Document(collection = "task")
public class Task {

    /**
     * The unique identifier of the task.
     */
    @Id
    private String id;

    @NonNull
    private String idTask;

    /**
     * The email of the user who created the task.
     */
    @NonNull
    private String emailUtente;

    /**
     * The maximum computing power that the task can use.
     */
    @NonNull
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
    @NonNull
    private Double taskDuration;

    /**
     * The maximum energy consumption of the task.
     */
    @NonNull
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


    /**
     * Default constructor for the Task class.
     */
    public Task() {}

    /**
     * Constructor for the Task class.
     * @param id The unique identifier of the task.
     * @param idTask The unique identifier of the task (data consistency).
     * @param emailUtente The email of the user who created the task.
     * @param maxComputingPower The maximum computing power that the task can use.
     * @param taskDuration The expected duration of the task, in seconds.
     * @param maxEnergyConsumption The maximum energy consumption of the task.
     * @param minComputingPower The minimum computing power that the task requires.
     * @param minEnergyConsumption The minimum energy consumption of the task.
     * @param minWorkingTime The minimum working time of the task, in seconds.
     * @param running The current status of the task.
     * @param enabled The enabled status of the task.
     */
    public Task(String id, @NonNull String idTask, @NonNull String emailUtente, @NonNull Double maxComputingPower, @NonNull Double taskDuration,@NonNull Double maxEnergyConsumption, Double minComputingPower, Double minEnergyConsumption, Double minWorkingTime, Boolean running, Boolean enabled) {
        this.id = id;
        this.idTask = idTask;
        this.emailUtente = emailUtente;
        this.maxComputingPower = maxComputingPower;
        this.taskDuration = taskDuration;
        this.maxEnergyConsumption = maxEnergyConsumption;
        this.minComputingPower = minComputingPower;
        this.minEnergyConsumption = minEnergyConsumption;
        this.minWorkingTime = minWorkingTime;
        this.running = running;
        this.enabled = enabled;
    }
}