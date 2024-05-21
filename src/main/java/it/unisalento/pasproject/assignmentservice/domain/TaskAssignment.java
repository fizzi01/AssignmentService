package it.unisalento.pasproject.assignmentservice.domain;

import com.mongodb.lang.NonNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Document(collection = "taskAssignment")
public class TaskAssignment {
    @Id
    private String id;

    @NonNull
    private String idTask;

    private List<AssignedMember> assignedMembers;

    //Va aggiornato quando tutti i membri hanno completato il task
    private Boolean isComplete;
    private LocalDateTime completedTime;
}
