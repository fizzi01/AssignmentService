package it.unisalento.pasproject.assignmentservice.controllers;

import it.unisalento.pasproject.assignmentservice.domain.AssignedResource;
import it.unisalento.pasproject.assignmentservice.domain.Resource;
import it.unisalento.pasproject.assignmentservice.dto.PayloadRequestDTO;
import it.unisalento.pasproject.assignmentservice.dto.PayloadResponseDTO;
import it.unisalento.pasproject.assignmentservice.exceptions.WrongPayloadRequest;
import it.unisalento.pasproject.assignmentservice.service.AllocationService;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Gestisce le richieste dai payloads in arrivo, quali:
 * - Payload avviato (POST)
 * - Payload completato (POST)
 */
@RestController
@RequestMapping("/api/tasks/assignments")
public class PayloadController {

    private final AllocationService allocationService;

    public PayloadController(AllocationService allocationService) {
        this.allocationService = allocationService;
    }

    //Richiesta da chiamare quando il payload si avvia, la richiesta contiene l'id del membro
    @PostMapping(value="/resource/update")
    public void startAssignment(@RequestBody PayloadRequestDTO payloadRequestDTO) {

        if (payloadRequestDTO.getAssignedResourceId() == null || payloadRequestDTO.getMemberEmail() == null) {
            throw new WrongPayloadRequest("Assignment id and member email must be provided");
        }

        //Cerco la risorsa assegnata
        Optional<AssignedResource> assignedResource = allocationService.getAssignedResource(payloadRequestDTO.getAssignedResourceId());

        if(assignedResource.isEmpty()) {
            throw new WrongPayloadRequest("Assigned resource not found");
        }

        //In base a start o stop
        if(payloadRequestDTO.getStart() != null && payloadRequestDTO.getStart()) {
            //Aggiorno il tempo di inizio
            assignedResource.get().setAssignedTime(LocalDateTime.now());
        } else if(payloadRequestDTO.getStop() != null && payloadRequestDTO.getStop()) {
            //Aggiorno il tempo di fine
            assignedResource.get().setCompletedTime(LocalDateTime.now());
        } else {
            throw new WrongPayloadRequest("Start or stop must be provided");
        }

    }

    /**
     * Quando invocato vengono passati come informazioni l'id della task e l'email del membro
     * @return Ritorna una lista con il nome di tutte le risorse (non deallocate) assegnate alla task
     */
    @PostMapping(value="/resource/info")
    public PayloadResponseDTO getAssignment(@RequestBody PayloadRequestDTO payloadRequestDTO) {

        if ( payloadRequestDTO.getAssignedResourceId() == null || payloadRequestDTO.getMemberEmail() == null ) {
            throw new WrongPayloadRequest("Assignment id and member email must be provided");
        }

        //Cerco la risorsa assegnata
        Optional<AssignedResource> assignedResource = allocationService.getAssignedResource(payloadRequestDTO.getAssignedResourceId());

        if(assignedResource.isEmpty()) {
            throw new WrongPayloadRequest("Assigned resource not found");
        }

        Optional<Resource> resource = allocationService.getResource(assignedResource.get().getHardwareId());
        if(resource.isEmpty()) {
            throw new WrongPayloadRequest("Resource not found");
        }

        PayloadResponseDTO payloadResponseDTO = new PayloadResponseDTO();
        payloadResponseDTO.setHardwareName(resource.get().getName());

        return payloadResponseDTO;

    }

}
