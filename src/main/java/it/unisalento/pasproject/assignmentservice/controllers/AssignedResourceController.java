package it.unisalento.pasproject.assignmentservice.controllers;

import it.unisalento.pasproject.assignmentservice.domain.AssignedResource;
import it.unisalento.pasproject.assignmentservice.dto.resource.AssignedResourceDTO;
import it.unisalento.pasproject.assignmentservice.dto.resource.AssignedResourceListDTO;
import it.unisalento.pasproject.assignmentservice.exceptions.AssignedResourceNotFoundException;
import it.unisalento.pasproject.assignmentservice.exceptions.AssignedTaskNotFoundException;
import it.unisalento.pasproject.assignmentservice.service.AssignedResourceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static it.unisalento.pasproject.assignmentservice.security.SecurityConstants.ROLE_MEMBRO;

@RestController
@RequestMapping("/api/assignments/resources")
public class AssignedResourceController {
    private final AssignedResourceService assignedResourceService;

    @Autowired
    public AssignedResourceController(AssignedResourceService assignedResourceService) {
        this.assignedResourceService = assignedResourceService;
    }

    @GetMapping(value = "/find/all")
    @Secured(ROLE_MEMBRO)
    public AssignedResourceListDTO getAssignedResources() {
        AssignedResourceListDTO assignedResourceListDTO = assignedResourceService.getAssignedResources();

        if(assignedResourceListDTO == null) {
            throw new AssignedResourceNotFoundException("No assigned resources found");
        }

        return assignedResourceListDTO;
    }

    @GetMapping(value = "/find")
    @Secured(ROLE_MEMBRO)
    public AssignedResourceListDTO getAssignedResourcesByFilter(@RequestParam(required = false) String id,
                                                                @RequestParam(required = false) String hardwareId,
                                                                @RequestParam(required = false) Double assignedSingleScore,
                                                                @RequestParam(required = false) Double assignedMultiScore,
                                                                @RequestParam(required = false) Double assignedOpenclScore,
                                                                @RequestParam(required = false) Double assignedVulkanScore,
                                                                @RequestParam(required = false) Double assignedCudaScore,
                                                                @RequestParam(required = false) Double assignedEnergyConsumptionPerHour,
                                                                @RequestParam(required = false) Long assignedWorkingTimeInSeconds,
                                                                @RequestParam(required = false) LocalDateTime fromAssigned,
                                                                @RequestParam(required = false) LocalDateTime toAssigned,
                                                                @RequestParam(required = false) LocalDateTime fromCompleted,
                                                                @RequestParam(required = false) LocalDateTime toCompleted,
                                                                @RequestParam(required = false) Boolean hasCompleted) {
        AssignedResourceListDTO assignedResourceListDTO = new AssignedResourceListDTO();
        List<AssignedResourceDTO> assignedTaskListDTO = new ArrayList<>();
        assignedResourceListDTO.setAssignedResourceDTOList(assignedTaskListDTO);

        List<AssignedResource> assignedResourceList = assignedResourceService.findAssignedResources(id, hardwareId, assignedSingleScore,
                assignedMultiScore, assignedOpenclScore, assignedVulkanScore, assignedCudaScore,
                assignedEnergyConsumptionPerHour, assignedWorkingTimeInSeconds, fromAssigned, toAssigned,
                fromCompleted, toCompleted, hasCompleted);

        if(assignedResourceList.isEmpty()) {
            throw new AssignedTaskNotFoundException("No assigned tasks found");
        }

        for (AssignedResource assignedResource : assignedResourceList) {
            assignedTaskListDTO.add(assignedResourceService.getAssignedResourceDTO(assignedResource));
        }

        return assignedResourceListDTO;
    }
}
