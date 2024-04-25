package it.unisalento.pasproject.assignmentservice.business.io.consumer;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service("RabbitMQSecurityConsumer")
public class RabbitMQSecuirityConsumer {

    @RabbitListener(queues = "${rabbitmq.queue.security.name}")
    public void receiveMessage(String message) {
        System.out.println("Received message: " + message);
    }

}
