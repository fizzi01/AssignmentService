package it.unisalento.pasproject.assignmentservice.service;


import it.unisalento.pasproject.assignmentservice.business.io.exchanger.MessageExchangeStrategy;
import it.unisalento.pasproject.assignmentservice.business.io.exchanger.MessageExchanger;
import it.unisalento.pasproject.assignmentservice.security.UserDetailsDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;


@Service
public class UserCheckService {

    @Value("${service.role.utente.name}")
    public String ROLE;

    @Autowired
    private MessageExchanger messageExchanger;

    @Autowired
    @Qualifier("RabbitMQExchange")
    private MessageExchangeStrategy messageExchangeStrategy;

    @Value("${rabbitmq.exchange.security.name}")
    private String securityExchange;

    @Value("${rabbitmq.routing.security.key}")
    private String securityRequestRoutingKey;

    private static final Logger LOGGER = LoggerFactory.getLogger(UserCheckService.class);


    public UserDetailsDTO loadUserByUsername(String email) throws UsernameNotFoundException {

        messageExchanger.setStrategy(messageExchangeStrategy);

        //Chiamata MQTT a CQRS per ottenere i dettagli dell'utente
        UserDetailsDTO user = messageExchanger.exchangeMessage(email,securityRequestRoutingKey,securityExchange,UserDetailsDTO.class);

        if(user == null) {
            throw new UsernameNotFoundException(email);
        }

        LOGGER.info(String.format("User %s found with role: %s and enabled %s", user.getEmail(), user.getRole(), user.getEnabled()));

        return user;
    }


    public Boolean roleCheck(String role) {
        return role.equals(ROLE);
    }

    public Boolean isEnable(Boolean enable) {
        return enable;
    }

}
