package it.unisalento.pasproject.assignmentservice.exceptions;

import it.unisalento.pasproject.assignmentservice.exceptions.global.CustomErrorException;
import org.springframework.http.HttpStatus;

public class AssignedTaskNotFoundException extends CustomErrorException {
    public AssignedTaskNotFoundException(String message) {
        super(message, HttpStatus.NOT_FOUND);
    }
}
