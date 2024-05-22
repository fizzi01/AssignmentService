package it.unisalento.pasproject.assignmentservice.business.assignment;

import it.unisalento.pasproject.assignmentservice.domain.Resource;
import it.unisalento.pasproject.assignmentservice.domain.Task;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class AllocationAlgorithm {

    public void assignResources(List<Task> tasks, List<Resource> resources) {
        for (Task task : tasks) {
            if (!task.getRunning()) { // Processa solo le task non in esecuzione
                List<Resource> assignedResources = new ArrayList<>();
                double totalComputingPower = 0;

                for (Resource resource : resources) {
                    if (isSuitableResource(task, resource, totalComputingPower)) {
                        // Assegna la risorsa alla task
                        resource.setIsAvailable(false);
                        resource.setCurrentTaskId(task.getId());
                        assignedResources.add(resource);
                        totalComputingPower += getComputationalPower(resource);

                        // Se raggiunge o supera la potenza computazionale massima, interrompe l'assegnazione ulteriore
                        if (totalComputingPower >= task.getMaxComputingPower()) {
                            break;
                        }
                    }
                }

                if (!assignedResources.isEmpty()) {
                    task.setRunning(true); // Imposta la task come in esecuzione se almeno una risorsa è stata assegnata
                    System.out.println("Task " + task.getIdTask() + " in esecuzione con le risorse: " + assignedResources);
                }
            }
        }
    }

    public static double getComputationalPower(Resource resource) {
        return resource.getMulticoreScore() != 0.0 ? resource.getMulticoreScore() : resource.getOpenclScore();
    }

    public boolean isSuitableResource(Task task, Resource resource,double currentComputingPower) {
        double actualComputationalPower = getComputationalPower(resource);


        // Verifica se la risorsa è disponibile
        if (!resource.getIsAvailable()) {
            return false;
        }

        // Controlla se l'aggiunta della risorsa supera la potenza computazionale massima
        double projectedComputingPower = currentComputingPower + actualComputationalPower;
        if (projectedComputingPower > task.getMaxComputingPower()) {
            return false;
        }

        // Controlla la potenza computazionale minima e massima
        double minComputingPower = task.getMinComputingPower() != null ? task.getMinComputingPower() : 0;
        if (actualComputationalPower < minComputingPower || actualComputationalPower > task.getMaxComputingPower()) {
            return false;
        }

        // Controlla la potenza CUDA minima e massima, se specificate
        if ((task.getMinCudaPower() != null && task.getMinCudaPower() != 0.0) && resource.getCudaScore() < task.getMinCudaPower()) {
            return false;
        }
        if ((task.getMaxCudaPower() != null && task.getMaxCudaPower() != 0.0) && resource.getCudaScore() > task.getMaxCudaPower()) {
            return false;
        }

        // Assicurarsi che la durata della task sia supportata dalle ore disponibili della risorsa
        // TODO: MODIFICARE ALGORITMO PER CONSIDERARE AVAILABILITY
        // return resource.getAvailableHours() * 3600 >= task.getTaskDuration();
        return 3600 >= task.getTaskDuration();
    }
}
