package it.unisalento.pasproject.assignmentservice.repositories;

import it.unisalento.pasproject.assignmentservice.domain.Task;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface TaskRepository extends MongoRepository<Task, String> {
    Task findByIdTask(String IdTask);
}
