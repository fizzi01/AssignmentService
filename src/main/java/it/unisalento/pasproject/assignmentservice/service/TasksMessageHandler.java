package it.unisalento.pasproject.assignmentservice.service;

import org.springframework.stereotype.Service;

@Service
public class TasksMessageHandler {
    // It receive new Tasks created, with all their infos, and stores them in the database
    // Deve sincronizzare le informazioni con il TaskManager, quindi si devono inviare messaggi
    // al TaskManager per aggiornare le informazioni delle task e viceversa
    //Deve anche notificare il TaskManager dell'assegnazione degli utenti alla task

}
