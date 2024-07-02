package it.unisalento.pasproject.assignmentservice.business.assignment;

import it.unisalento.pasproject.assignmentservice.business.assignment.Commands.AssignResourceCommand;
import it.unisalento.pasproject.assignmentservice.business.assignment.Commands.Command;
import it.unisalento.pasproject.assignmentservice.business.assignment.Commands.EnableTaskCommand;
import it.unisalento.pasproject.assignmentservice.domain.*;
import it.unisalento.pasproject.assignmentservice.service.AllocationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class AllocationAlgorithm {

    public final AllocationService allocationService;

    @Autowired
    public AllocationAlgorithm(AllocationService allocationService) {
        this.allocationService = allocationService;
    }

    /**
     * Assegna le risorse alle task in modo da massimizzare la potenza computazionale e il numero di risorse assegnate
     * @param tasks lista di task a cui assegnare
     * @param resources lista di risorse disponibili
     */
    public void assignResources(List<Task> tasks, List<Resource> resources) {

        List<Command> commands = new ArrayList<>();

        for (Task task : tasks) {
            commands.add(new EnableTaskCommand(task, allocationService));
            commands.add(new AssignResourceCommand(task, resources, allocationService));
        }

        for (Command command : commands) {
            command.execute();
        }
    }

}
