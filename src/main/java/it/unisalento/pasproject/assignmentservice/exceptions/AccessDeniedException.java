package it.unisalento.pasproject.assignmentservice.exceptions;

import it.unisalento.pasproject.assignmentservice.exceptions.global.CustomErrorException;
import org.springframework.http.HttpStatus;

public class AccessDeniedException extends CustomErrorException {
    public AccessDeniedException(String message) {
        super(message, HttpStatus.FORBIDDEN);
    }
}
