package it.unisalento.pasproject.assignmentservice.dto.resource;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class AssignedResourceListDTO {
    private List<AssignedResourceDTO> assignedResourceDTOList;

    public AssignedResourceListDTO() {
        this.assignedResourceDTOList = new ArrayList<>();
    }
}
