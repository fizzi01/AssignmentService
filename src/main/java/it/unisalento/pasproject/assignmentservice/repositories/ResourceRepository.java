package it.unisalento.pasproject.assignmentservice.repositories;

import it.unisalento.pasproject.assignmentservice.domain.Resource;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface ResourceRepository extends MongoRepository<Resource, String> {
    Resource findByIdResource(String idResource);

    //List<Resource> findByIsAvailableTrue();

    //List<Resource> findByIsAvailableFalse();

    List<Resource> findByStatus(Resource.Status status);
}
