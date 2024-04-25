package it.unisalento.pasproject.assignmentservice.repositories;

import it.unisalento.pasproject.assignmentservice.domain.AssignedMember;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AssignedMemberRepository extends MongoRepository<AssignedMember, String> {
    // Qui puoi aggiungere metodi per query personalizzate se necessario
    // Esempio: trovare tutti i membri assegnati a una specifica task
    List<AssignedMember> findByMemberId(String idTask);
}
