package it.unisalento.pasproject.assignmentservice.controllers;

import it.unisalento.pasproject.assignmentservice.domain.Resource;
import it.unisalento.pasproject.assignmentservice.dto.resource.ResourceDTO;
import it.unisalento.pasproject.assignmentservice.dto.resource.ResourceListDTO;
import it.unisalento.pasproject.assignmentservice.repositories.ResourceRepository;
import it.unisalento.pasproject.assignmentservice.service.ResourceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/resources/assignment")
public class TestResourceController {
    @Autowired
    private ResourceRepository resourceRepository;

    @Autowired
    private ResourceService resourceService;

    @GetMapping(value="/find/all")
    public ResourceListDTO getAllResources() {
        ResourceListDTO resourceListDTO = new ResourceListDTO();
        List<ResourceDTO> list = new ArrayList<>();
        resourceListDTO.setResourcesList(list);

        List<Resource> resources = resourceRepository.findAll();

        for (Resource resource : resources){
            list.add(resourceService.getResourceDTO(resource));
        }

        return resourceListDTO;
    }
}
