package it.unisalento.pasproject.assignmentservice.exceptions;

import it.unisalento.pasproject.assignmentservice.exceptions.global.CustomErrorException;
import org.springframework.http.HttpStatus;

public class AssignedResourceNotFoundException extends CustomErrorException {
    public AssignedResourceNotFoundException(String message) {
        super(message, HttpStatus.NOT_FOUND);
    }
}
