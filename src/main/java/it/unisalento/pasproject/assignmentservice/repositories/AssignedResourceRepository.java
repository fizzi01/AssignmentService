package it.unisalento.pasproject.assignmentservice.repositories;

import it.unisalento.pasproject.assignmentservice.domain.AssignedResource;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface AssignedResourceRepository extends MongoRepository<AssignedResource, String> {
    List<AssignedResource> findByCompletedTimeAfter(LocalDateTime now);
    List<AssignedResource> findByCompletedTimeAfterAndHasCompletedFalse(LocalDateTime now);

    boolean existsByHardwareIdAndHasCompletedTrue(String id);

    List<AssignedResource> findByHardwareIdAndHasCompletedFalse(String id);

    Optional<AssignedResource> findByHardwareIdAndTaskAssignmentId(String resourceId, String taskAssignmentId);

    Optional<AssignedResource> findByHardwareId(String resourceId);
}
