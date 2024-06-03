package it.unisalento.pasproject.assignmentservice.service;

import it.unisalento.pasproject.assignmentservice.domain.Resource;
import it.unisalento.pasproject.assignmentservice.dto.resource.ResourceDTO;
import it.unisalento.pasproject.assignmentservice.dto.resource.ResourceMessageDTO;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ResourceService {

    public Resource getResource(ResourceMessageDTO resourceMessageDTO) {
        Resource resource = new Resource();

        Optional.ofNullable(resourceMessageDTO.getId()).ifPresent(resource::setId);
        Optional.ofNullable(resourceMessageDTO.getName()).ifPresent(resource::setName);
        Optional.ofNullable(resourceMessageDTO.getAvailability()).ifPresent(resource::setAvailability);
        Optional.of(resourceMessageDTO.getKWh()).ifPresent(resource::setKWh);
        Optional.ofNullable(resourceMessageDTO.getMemberEmail()).ifPresent(resource::setMemberEmail);
        Optional.ofNullable(resourceMessageDTO.getIsAvailable()).ifPresent(resource::setIsAvailable);
        Optional.ofNullable(resourceMessageDTO.getCurrentTaskId()).ifPresent(resource::setCurrentTaskId);
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
        Optional.ofNullable(resource.getName()).ifPresent(resourceDTO::setName);
        Optional.ofNullable(resource.getAvailability()).ifPresent(resourceDTO::setAvailability);
        Optional.of(resource.getKWh()).ifPresent(resourceDTO::setKWh);
        Optional.ofNullable(resource.getMemberEmail()).ifPresent(resourceDTO::setMemberEmail);
        Optional.ofNullable(resource.getIsAvailable()).ifPresent(resourceDTO::setIsAvailable);
        Optional.ofNullable(resource.getCurrentTaskId()).ifPresent(resourceDTO::setCurrentTaskId);
        Optional.of(resource.getSingleCoreScore()).ifPresent(resourceDTO::setSingleCoreScore);
        Optional.of(resource.getMulticoreScore()).ifPresent(resourceDTO::setMulticoreScore);
        Optional.of(resource.getOpenclScore()).ifPresent(resourceDTO::setOpenclScore);
        Optional.of(resource.getVulkanScore()).ifPresent(resourceDTO::setVulkanScore);
        Optional.of(resource.getCudaScore()).ifPresent(resourceDTO::setCudaScore);
        return resourceDTO;
    }
}