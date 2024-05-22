package it.unisalento.pasproject.assignmentservice.service;

import it.unisalento.pasproject.assignmentservice.domain.AssignedResource;
import it.unisalento.pasproject.assignmentservice.domain.Resource;
import it.unisalento.pasproject.assignmentservice.domain.Task;
import it.unisalento.pasproject.assignmentservice.domain.TaskAssignment;
import it.unisalento.pasproject.assignmentservice.repositories.AssignedResourceRepository;
import it.unisalento.pasproject.assignmentservice.repositories.ResourceRepository;
import it.unisalento.pasproject.assignmentservice.repositories.TaskAssignmentRepository;
import it.unisalento.pasproject.assignmentservice.repositories.TaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.*;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AllocationService {

    private final TaskRepository taskRepository;
    private final TaskAssignmentRepository taskAssignmentRepository;
    private final ResourceRepository resourceRepository;
    private final AssignedResourceRepository assignedMemberRepository;

    @Autowired
    public AllocationService(TaskRepository taskRepository, TaskAssignmentRepository taskAssignmentRepository, ResourceRepository resourceRepository, AssignedResourceRepository assignedMemberRepository) {
        this.taskRepository = taskRepository;
        this.taskAssignmentRepository = taskAssignmentRepository;
        this.resourceRepository = resourceRepository;
        this.assignedMemberRepository = assignedMemberRepository;
    }

    public List<Task> getAvailableTasks() {
        //Prendo una lista di TaskAssignment non ancora completati
        List<TaskAssignment> taskAssignments = taskAssignmentRepository.findByIsCompleteFalse();

        //Delle task ottenute (gli id delle task) prendo solo le task che hanno enabled=true e running=true
        return taskRepository.findByIdInAndEnabledTrueAndRunningTrue(taskAssignments.stream().map(TaskAssignment::getIdTask).toList());
    }

    public List<Resource> getAvailableResources() {
        //TODO: Vedere se funziona
        DayOfWeek currentDay = LocalDateTime.now().getDayOfWeek();
        LocalTime currentTime = LocalTime.now();

        List<Resource> availableResources = resourceRepository.findByIsAvailableTrue();

        availableResources = availableResources.stream()
                .filter(resource -> resource.getAvailability().stream()
                        .anyMatch(availability -> (
                                availability.getDayOfWeek().equals(currentDay) &&
                                !currentTime.isBefore(availability.getStartTime()) &&
                                !currentTime.isAfter(availability.getEndTime()))))
                .collect(Collectors.toList());

        return availableResources;
    }

    public List<Task> getRunningTasks() {
        return taskRepository.findByEnabledTrueAndRunningTrue();
    }


    public List<TaskAssignment> getTaskAssignments(String id) {
        return taskAssignmentRepository.findByIdTaskAndIsCompleteFalse(id);
    }

    public void deallocateResources(TaskAssignment taskAssignment) {
        completeTaskAssignment(taskAssignment);
        LocalDateTime now = LocalDateTime.now();

        //Prendo le risorse allocate per il task e le dealloco
        for (AssignedResource assigned : taskAssignment.getAssignedResources()) {

            Resource resource = resourceRepository.findById(assigned.getHardwareId()).orElseThrow();

            if ( now.isAfter(assigned.getCompletedTime()) ) {

                if ( !assigned.isHasCompleted() ) {
                    assigned.setHasCompleted(true);
                }


            } else {
                assigned.setCompletedTime(now);
                assigned.setHasCompleted(true);
            }

            assignedMemberRepository.save(assigned);

            if ( !resource.getIsAvailable() ){
                resource.setIsAvailable(true);
                resource.setCurrentTaskId(null);
            }

            resourceRepository.save(resource);
        }

    }

    public List<AssignedResource> getAssignedMembers() {
        //Restituisce gli assigned member che hanno completedTime > now
        return assignedMemberRepository.findByCompletedTimeAfter(LocalDateTime.now());
    }

    public void deallocateResource(AssignedResource member) {
        Resource resource = resourceRepository.findById(member.getHardwareId()).orElseThrow();
        resource.setIsAvailable(true);
        resource.setCurrentTaskId(null);
        resourceRepository.save(resource);
    }

    public void updateTask(Task task) {
        taskRepository.save(task);
    }

    public void completeTaskAssignment(Task task){
        TaskAssignment taskAssignment = taskAssignmentRepository.findByIdTask(task.getId());
        taskAssignment.setIsComplete(true);
        taskAssignment.setCompletedTime(LocalDateTime.now());
        taskAssignmentRepository.save(taskAssignment);
    }

    public void completeTaskAssignment(TaskAssignment taskAssignment){
        taskAssignment.setIsComplete(true);
        taskAssignment.setCompletedTime(LocalDateTime.now());
        taskAssignmentRepository.save(taskAssignment);
    }
}
