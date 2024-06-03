package it.unisalento.pasproject.assignmentservice.service;

import it.unisalento.pasproject.assignmentservice.domain.TaskAssignment;
import it.unisalento.pasproject.assignmentservice.dto.task.AssignedTaskDTO;
import it.unisalento.pasproject.assignmentservice.dto.task.AssignedTaskListDTO;
import it.unisalento.pasproject.assignmentservice.repositories.TaskAssignmentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class AssignedTaskService {
    private final MongoTemplate mongoTemplate;

    private final TaskAssignmentRepository taskAssignmentRepository;

    private final Logger LOGGER = LoggerFactory.getLogger(AssignedTaskService.class);

    @Autowired
    public AssignedTaskService(TaskAssignmentRepository taskAssignmentRepository, MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
        this.taskAssignmentRepository = taskAssignmentRepository;
    }

    public AssignedTaskDTO getAssignedTaskDTO(TaskAssignment taskAssignment) {
        AssignedTaskDTO assignedTaskDTO = new AssignedTaskDTO();

        Optional.ofNullable(taskAssignment.getId()).ifPresent(assignedTaskDTO::setId);
        Optional.of(taskAssignment.getIdTask()).ifPresent(assignedTaskDTO::setIdTask);
        Optional.ofNullable(taskAssignment.getAssignedResources()).ifPresent(assignedTaskDTO::setAssignedResources);
        Optional.ofNullable(taskAssignment.getIsComplete()).ifPresent(assignedTaskDTO::setIsComplete);
        Optional.ofNullable(taskAssignment.getCompletedTime()).ifPresent(assignedTaskDTO::setCompletedTime);

        return assignedTaskDTO;
    }

    public AssignedTaskListDTO getAssignedTasks() {
        List<TaskAssignment> taskAssignmentList = taskAssignmentRepository.findAll();

        if(taskAssignmentList.isEmpty()) {
            return null;
        }

        AssignedTaskListDTO assignedTaskListDTO = new AssignedTaskListDTO();
        List<AssignedTaskDTO> assignedTaskList = new ArrayList<>();
        assignedTaskListDTO.setAssignedTaskDTOList(assignedTaskList);

        for (TaskAssignment taskAssignment : taskAssignmentList) {
            assignedTaskList.add(getAssignedTaskDTO(taskAssignment));
        }

        return assignedTaskListDTO;
    }

    public List<TaskAssignment> findAssignedTasks(String id, String idTask, Boolean isComplete, LocalDateTime from , LocalDateTime to) {
        Query query = new Query();

        // Add conditions based on parameters provided
        if (id != null) {
            query.addCriteria(Criteria.where("id").is(id));
        }

        if (idTask != null) {
            query.addCriteria(Criteria.where("idTask").is(idTask));
        }
        if (isComplete != null) {
            query.addCriteria(Criteria.where("isComplete").is(isComplete));
        }
        if (from != null) {
            query.addCriteria(Criteria.where("completedTime").gte(from));
        }
        if (to != null) {
            query.addCriteria(Criteria.where("completedTime").lte(to));
        }

        LOGGER.info("\n{}\n", query);

        List<TaskAssignment> taskAssignments = mongoTemplate.find(query, TaskAssignment.class, mongoTemplate.getCollectionName(TaskAssignment.class));

        LOGGER.info("\nTask assignments: {}\n", taskAssignments);

        return taskAssignments;
    }
}
