package it.unisalento.pasproject.assignmentservice.business.assignment;

import it.unisalento.pasproject.assignmentservice.domain.Resource;
import it.unisalento.pasproject.assignmentservice.domain.Task;
import it.unisalento.pasproject.assignmentservice.service.AllocationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AllocationManager {

    //La classe ha un metodo che recupera tutte le task attualmente in running e tutte le risorse
    //disponibili e assegna le task alle risorse in modo da minimizzare il tempo di completamento
    //delle task. Il metodo restituisce un oggetto che contiene le assegnazioni effettuate.

    private final AllocationAlgorithm allocationAlgorithm;
    private final AllocationService allocationService;

    @Autowired
    public AllocationManager(AllocationAlgorithm allocationAlgorithm, AllocationService allocationService) {
        this.allocationAlgorithm = allocationAlgorithm;
        this.allocationService = allocationService;
    }


    //TODO: @Scheduled(fixedRate = 60000) // esegue ogni minuto
    public void runAllocator() {

        //Prende tutte le Task che sono in running e che sono enabled
        List<Task> tasks = allocationService.getAvailableTasks();

        //Prende tutte le risorse disponibili
        List<Resource> resources = allocationService.getAvailableResources();

        allocationAlgorithm.assignResources(tasks, resources);
    }



}
