package it.unisalento.pasproject.assignmentservice.service;

import it.unisalento.pasproject.assignmentservice.business.io.producer.MessageProducer;
import it.unisalento.pasproject.assignmentservice.business.io.producer.MessageProducerStrategy;
import it.unisalento.pasproject.assignmentservice.dto.TransactionCreationDTO;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

// Quando la risorsa viene deallocata, scatta la richiesta di invio credito
@Service
public class CheckoutMessageHandler {

    @Value("${rabbitmq.routing.sendTransaction.name}")
    private String sendTransactionRoutingKey;

    @Value("${rabbitmq.exchange.transaction.name}")
    private String transactionExchange;

    private final MessageProducer messageProducer;

    public CheckoutMessageHandler(MessageProducer messageProducer,@Qualifier("RabbitMQProducer") MessageProducerStrategy messageProducerStrategy) {
        this.messageProducer = messageProducer;
        this.messageProducer.setStrategy(messageProducerStrategy);
    }

    public void startCheckout(String emailUtente, String memberEmail, double credits) {
        // Invio messaggio di richiesta di invio credito
        TransactionCreationDTO transaction = new TransactionCreationDTO();
        transaction.setSenderEmail(emailUtente);
        transaction.setReceiverEmail(memberEmail);
        transaction.setAmount(credits);
        transaction.setDescription("Task work reward");
        transaction.setTransactionOwner(emailUtente);
        messageProducer.sendMessage(transaction, sendTransactionRoutingKey, transactionExchange);
    }
}
