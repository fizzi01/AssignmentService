package it.unisalento.pasproject.assignmentservice.repositories;

import it.unisalento.pasproject.assignmentservice.domain.Task;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface TaskRepository extends MongoRepository<Task, String> {
    Task findByIdTask(String IdTask);

    List<Task> findByIdInAndEnabledTrueAndRunningTrue(List<String> list);

    List<Task> findByEnabledTrueAndRunningTrue();

    List<Task> findByRunningTrue();
}
