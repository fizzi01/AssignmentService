package it.unisalento.pasproject.assignmentservice.service;

import it.unisalento.pasproject.assignmentservice.business.io.producer.MessageProducer;
import it.unisalento.pasproject.assignmentservice.domain.Resource;
import it.unisalento.pasproject.assignmentservice.dto.resource.ResourceMessageDTO;
import it.unisalento.pasproject.assignmentservice.dto.resource.ResourceStatusMessageDTO;
import it.unisalento.pasproject.assignmentservice.repositories.ResourceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ResourceMessageHandler {
    // Si occupa della comunicazione con il servizio di gestione dei membri per ottenere
    // informazioni sulle componenti hardware per trovare membri non ancora assegnati a tasks

    @Value("${rabbitmq.routing.resourceassignment.key}")
    private String resourceAssignedTopic;

    @Value("${rabbitmq.routing.resourcedeallocation.key}")
    private String resourceDeallocationTopic;

    @Value("${rabbitmq.exchange.resource.name}")
    private String resourceExchange;

    private final ResourceService resourceService;
    private final MessageProducer messageProducer;
    private final ResourceRepository resourceRepository;
    private static final Logger LOGGER = LoggerFactory.getLogger(ResourceMessageHandler.class);

    @Autowired
    public ResourceMessageHandler(ResourceRepository resourceRepository, MessageProducer messageProducer, ResourceService resourceService) {
        this.resourceRepository = resourceRepository;
        this.messageProducer = messageProducer;
        this.resourceService = resourceService;
    }

    @RabbitListener(queues = "${rabbitmq.queue.newresource.name}")
    public void handleNewResource(ResourceMessageDTO resourceMessageDTO) {
        Optional<Resource> resource = Optional.ofNullable(resourceRepository.findByIdResource(resourceMessageDTO.getId()));
        Resource newResource;

        if(resource.isPresent()) {
            newResource = resource.get();
            newResource = resourceService.updateResource(newResource, resourceMessageDTO);
        } else {
            newResource = resourceService.getResource(resourceMessageDTO);
        }
        resourceRepository.save(newResource);
    }

    public void handleResourceAssignment(ResourceStatusMessageDTO message) {
        messageProducer.sendMessage(message, resourceAssignedTopic, resourceExchange);

    }

    public void handleResourceDeallocation(ResourceStatusMessageDTO message) {
       messageProducer.sendMessage(message, resourceDeallocationTopic, resourceExchange);
    }
}
