package it.unisalento.pasproject.assignmentservice.service;

import it.unisalento.pasproject.assignmentservice.domain.AssignedResource;
import it.unisalento.pasproject.assignmentservice.dto.resource.AssignedResourceDTO;
import it.unisalento.pasproject.assignmentservice.dto.resource.AssignedResourceListDTO;
import it.unisalento.pasproject.assignmentservice.repositories.AssignedResourceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class AssignedResourceService {
    private final MongoTemplate mongoTemplate;
    private final AssignedResourceRepository assignedResourceRepository;

    private final Logger LOGGER = LoggerFactory.getLogger(AssignedResourceService.class);

    @Autowired
    public AssignedResourceService(AssignedResourceRepository assignedResourceRepository, MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
        this.assignedResourceRepository = assignedResourceRepository;
    }

    public AssignedResourceDTO getAssignedResourceDTO(AssignedResource assignedResource) {
        AssignedResourceDTO assignedResourceDTO = new AssignedResourceDTO();

        Optional.ofNullable(assignedResource.getId()).ifPresent(assignedResourceDTO::setId);
        Optional.ofNullable(assignedResource.getHardwareId()).ifPresent(assignedResourceDTO::setHardwareId);
        Optional.of(assignedResource.getAssignedSingleScore()).ifPresent(assignedResourceDTO::setAssignedSingleScore);
        Optional.of(assignedResource.getAssignedMultiScore()).ifPresent(assignedResourceDTO::setAssignedMultiScore);
        Optional.of(assignedResource.getAssignedOpenclScore()).ifPresent(assignedResourceDTO::setAssignedOpenclScore);
        Optional.of(assignedResource.getAssignedVulkanScore()).ifPresent(assignedResourceDTO::setAssignedVulkanScore);
        Optional.of(assignedResource.getAssignedCudaScore()).ifPresent(assignedResourceDTO::setAssignedCudaScore);
        Optional.of(assignedResource.getAssignedEnergyConsumptionPerHour()).ifPresent(assignedResourceDTO::setAssignedEnergyConsumptionPerHour);
        Optional.of(assignedResource.getAssignedWorkingTimeInSeconds()).ifPresent(assignedResourceDTO::setAssignedWorkingTimeInSeconds);
        Optional.ofNullable(assignedResource.getAssignedTime()).ifPresent(assignedResourceDTO::setAssignedTime);
        Optional.ofNullable(assignedResource.getCompletedTime()).ifPresent(assignedResourceDTO::setCompletedTime);
        Optional.of(assignedResource.isHasCompleted()).ifPresent(assignedResourceDTO::setHasCompleted);

        return assignedResourceDTO;
    }

    public AssignedResourceListDTO getAssignedResources() {
        List<AssignedResource> assignedResourceList = assignedResourceRepository.findAll();

        if(assignedResourceList.isEmpty()) {
            return null;
        }

        AssignedResourceListDTO assignedResourceListDTO = new AssignedResourceListDTO();
        List<AssignedResourceDTO> assignedResourceDTOList = new ArrayList<>();
        assignedResourceListDTO.setAssignedResourceDTOList(assignedResourceDTOList);

        for (AssignedResource assignedResource : assignedResourceList) {
            assignedResourceDTOList.add(getAssignedResourceDTO(assignedResource));
        }

        return assignedResourceListDTO;
    }

    public List<AssignedResource> findAssignedResources(String id, String hardwareId, Double assignedSingleScore,
                                                        Double assignedMultiScore, Double assignedOpenclScore,
                                                        Double assignedVulkanScore, Double assignedCudaScore,
                                                        Double assignedEnergyConsumptionPerHour, Long assignedWorkingTimeInSeconds,
                                                        LocalDateTime fromAssignment, LocalDateTime toAssignment,
                                                        LocalDateTime fromCompleted, LocalDateTime toCompleted, Boolean hasCompleted) {
        Query query = new Query();

        if(id != null) {
            query.addCriteria(Criteria.where("id").is(id));
        }

        if(hardwareId != null) {
            query.addCriteria(Criteria.where("hardwareId").is(hardwareId));
        }

        if(assignedSingleScore != null) {
            query.addCriteria(Criteria.where("assignedSingleScore").is(assignedSingleScore));
        }

        if(assignedMultiScore != null) {
            query.addCriteria(Criteria.where("assignedMultiScore").is(assignedMultiScore));
        }

        if(assignedOpenclScore != null) {
            query.addCriteria(Criteria.where("assignedOpenclScore").is(assignedOpenclScore));
        }

        if(assignedVulkanScore != null) {
            query.addCriteria(Criteria.where("assignedVulkanScore").is(assignedVulkanScore));
        }

        if(assignedCudaScore != null) {
            query.addCriteria(Criteria.where("assignedCudaScore").is(assignedCudaScore));
        }

        if(assignedEnergyConsumptionPerHour != null) {
            query.addCriteria(Criteria.where("assignedEnergyConsumptionPerHour").is(assignedEnergyConsumptionPerHour));
        }

        if(assignedWorkingTimeInSeconds != null) {
            query.addCriteria(Criteria.where("assignedWorkingTimeInSeconds").is(assignedWorkingTimeInSeconds));
        }

        if(fromAssignment != null) {
            query.addCriteria(Criteria.where("assignedTime").gte(fromAssignment));
        }

        if(toAssignment != null) {
            query.addCriteria(Criteria.where("assignedTime").lte(toAssignment));
        }

        if(fromCompleted != null) {
            query.addCriteria(Criteria.where("completedTime").gte(fromCompleted));
        }

        if(toCompleted != null) {
            query.addCriteria(Criteria.where("completedTime").lte(toCompleted));
        }

        if(hasCompleted != null) {
            query.addCriteria(Criteria.where("hasCompleted").is(hasCompleted));
        }

        LOGGER.info("\n{}\n", query);

        List<AssignedResource> assignedResources = mongoTemplate.find(query, AssignedResource.class, mongoTemplate.getCollectionName(AssignedResource.class));

        LOGGER.info("\nTask assignments: {}\n", assignedResources);

        return assignedResources;
    }
}
