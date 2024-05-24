package it.unisalento.pasproject.assignmentservice.dto;

import lombok.Getter;
import lombok.Setter;


/**
 * Gestisce le richieste da parte dei payloads, quali:
 * Richiesta delle risorse dell'utente assegnate a una determinata task
 * Segnalare lo start della risorse
 * Segnalre lo stop della risorsa
 */
@Getter
@Setter
public class PayloadRequestDTO {

    private String assignedResourceId;
    private String memberEmail;

    private Boolean start;
    private Boolean stop;

}
