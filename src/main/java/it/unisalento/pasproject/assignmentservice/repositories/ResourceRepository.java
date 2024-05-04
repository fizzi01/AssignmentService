package it.unisalento.pasproject.assignmentservice.repositories;

import it.unisalento.pasproject.assignmentservice.domain.Resource;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ResourceRepository extends MongoRepository<Resource, String> {
    Resource findByIdResource(String idResource);
}
