package it.unisalento.pasproject.assignmentservice.domain;


import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@Document(collection = "settings")
public class Settings {

    @Id
    private String id;

    private double creditConstant;
}
