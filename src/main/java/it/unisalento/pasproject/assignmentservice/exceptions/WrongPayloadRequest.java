package it.unisalento.pasproject.assignmentservice.exceptions;

import it.unisalento.pasproject.assignmentservice.exceptions.global.CustomErrorException;
import org.springframework.http.HttpStatus;

public class WrongPayloadRequest extends CustomErrorException {

    public WrongPayloadRequest(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }
}
