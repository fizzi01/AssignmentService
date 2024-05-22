package it.unisalento.pasproject.assignmentservice.repositories;

import it.unisalento.pasproject.assignmentservice.domain.AssignedResource;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface AssignedResourceRepository extends MongoRepository<AssignedResource, String> {
    // Qui puoi aggiungere metodi per query personalizzate se necessario
    // Esempio: trovare tutti i membri assegnati a una specifica task
    List<AssignedResource> findByMemberId(String idTask);

    List<AssignedResource> findByCompletedTimeAfter(LocalDateTime now);

}
