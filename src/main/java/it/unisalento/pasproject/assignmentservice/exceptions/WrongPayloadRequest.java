package it.unisalento.pasproject.assignmentservice.exceptions;

import org.springframework.http.HttpStatus;

public class WrongPayloadRequest extends CustomErrorException{

    public WrongPayloadRequest(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }
}
