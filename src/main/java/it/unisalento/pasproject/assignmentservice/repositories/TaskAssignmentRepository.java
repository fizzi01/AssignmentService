package it.unisalento.pasproject.assignmentservice.repositories;

import it.unisalento.pasproject.assignmentservice.domain.AssignedResource;
import it.unisalento.pasproject.assignmentservice.domain.TaskAssignment;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface TaskAssignmentRepository extends MongoRepository<TaskAssignment, String> {
    // Qui puoi aggiungere metodi per query personalizzate se necessario
    // Esempio: trovare tutte le assegnazioni per una data task
    TaskAssignment findByIdTask(String taskId);

    // Esempio: trovare tutte le assegnazioni che non sono ancora completate
    List<TaskAssignment> findByIsCompleteFalse();

    List<TaskAssignment> findAllByIdTaskAndIsCompleteFalse(String id);
    TaskAssignment findByIdTaskAndIsCompleteFalse(String id);

    TaskAssignment findByAssignedResourcesContainsAndIsCompleteFalse(AssignedResource resource);
}