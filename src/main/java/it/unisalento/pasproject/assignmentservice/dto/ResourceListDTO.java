package it.unisalento.pasproject.assignmentservice.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class ResourceListDTO {
    private List<ResourceDTO> resourcesList;

    public ResourceListDTO() {
        this.resourcesList = new ArrayList<>();
    }
}