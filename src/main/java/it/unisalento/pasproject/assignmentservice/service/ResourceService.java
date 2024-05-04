package it.unisalento.pasproject.assignmentservice.service;

import it.unisalento.pasproject.assignmentservice.domain.Resource;
import it.unisalento.pasproject.assignmentservice.dto.ResourceDTO;
import it.unisalento.pasproject.assignmentservice.dto.ResourceMessageDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ResourceService {
    //TODO: VEDERE SE TOGLIERE
    private final MongoTemplate mongoTemplate;

    //TODO: VEDERE SE TOGLIERE
    private static final Logger LOGGER = LoggerFactory.getLogger(ResourceService.class);

    @Autowired
    public ResourceService(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    public Resource getResource(ResourceMessageDTO resourceMessageDTO) {
        Resource resource = new Resource();
        Optional.ofNullable(resourceMessageDTO.getId()).ifPresent(resource::setId);
        Optional.of(resourceMessageDTO.getAvailableHours()).ifPresent(resource::setAvailableHours);
        Optional.of(resourceMessageDTO.getKWh()).ifPresent(resource::setKWh);
        Optional.ofNullable(resourceMessageDTO.getMemberEmail()).ifPresent(resource::setMemberEmail);
        Optional.ofNullable(resourceMessageDTO.getIsAvailable()).ifPresent(resource::setIsAvailable);
        Optional.ofNullable(resourceMessageDTO.getAssignedUser()).ifPresent(resource::setAssignedUser);
        Optional.of(resourceMessageDTO.getTdp()).ifPresent(resource::setTdp);
        Optional.of(resourceMessageDTO.getSingleCoreScore()).ifPresent(resource::setSingleCoreScore);
        Optional.of(resourceMessageDTO.getMulticoreScore()).ifPresent(resource::setMulticoreScore);
        Optional.of(resourceMessageDTO.getOpenclScore()).ifPresent(resource::setOpenclScore);
        Optional.of(resourceMessageDTO.getVulkanScore()).ifPresent(resource::setVulkanScore);
        Optional.of(resourceMessageDTO.getCudaScore()).ifPresent(resource::setCudaScore);
        return resource;
    }

    public ResourceDTO getResourceDTO(Resource resource) {
        ResourceDTO resourceDTO = new ResourceDTO();
        Optional.ofNullable(resource.getId()).ifPresent(resourceDTO::setId);
        Optional.of(resource.getAvailableHours()).ifPresent(resourceDTO::setAvailableHours);
        Optional.of(resource.getKWh()).ifPresent(resourceDTO::setKWh);
        Optional.ofNullable(resource.getMemberEmail()).ifPresent(resourceDTO::setMemberEmail);
        Optional.ofNullable(resource.getIsAvailable()).ifPresent(resourceDTO::setIsAvailable);
        Optional.ofNullable(resource.getAssignedUser()).ifPresent(resourceDTO::setAssignedUser);
        Optional.of(resource.getTdp()).ifPresent(resourceDTO::setTdp);
        Optional.of(resource.getSingleCoreScore()).ifPresent(resourceDTO::setSingleCoreScore);
        Optional.of(resource.getMulticoreScore()).ifPresent(resourceDTO::setMulticoreScore);
        Optional.of(resource.getOpenclScore()).ifPresent(resourceDTO::setOpenclScore);
        Optional.of(resource.getVulkanScore()).ifPresent(resourceDTO::setVulkanScore);
        Optional.of(resource.getCudaScore()).ifPresent(resourceDTO::setCudaScore);
        return resourceDTO;
    }
}