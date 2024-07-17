package it.unisalento.pasproject.assignmentservice.controller;

import it.unisalento.pasproject.assignmentservice.TestSecurityConfig;
import it.unisalento.pasproject.assignmentservice.controllers.PayloadController;
import it.unisalento.pasproject.assignmentservice.domain.AssignedResource;
import it.unisalento.pasproject.assignmentservice.domain.Resource;
import it.unisalento.pasproject.assignmentservice.domain.TaskAssignment;
import it.unisalento.pasproject.assignmentservice.dto.payload.PayloadRequestDTO;
import it.unisalento.pasproject.assignmentservice.service.AllocationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PayloadController.class)
@AutoConfigureMockMvc
@ExtendWith(MockitoExtension.class)
@Import(TestSecurityConfig.class)
public class PayloadControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AllocationService allocationService;

    private PayloadRequestDTO payloadRequestDTO;
    private AssignedResource assignedResource;
    private TaskAssignment taskAssignment;
    private Resource resource;

    @BeforeEach
    void setUp() {
        payloadRequestDTO = new PayloadRequestDTO();
        payloadRequestDTO.setAssignedResourceId("1");
        payloadRequestDTO.setMemberEmail("test@example.com");
        payloadRequestDTO.setStart(true);

        assignedResource = new AssignedResource();
        assignedResource.setId("1");
        assignedResource.setHardwareId("1");
        assignedResource.setAssignedTime(LocalDateTime.now());

        taskAssignment = new TaskAssignment();
        taskAssignment.setId("1");
        taskAssignment.setAssignedResources(List.of(assignedResource));

        resource = new Resource();
        resource.setId("1");
        resource.setMemberEmail("test@example.com");
    }

    @Test
    @WithMockUser(roles = "MEMBRO")
    void startAssignmentWithValidRequestShouldUpdateAssignedTime() throws Exception {
        given(allocationService.getAssignedResource(any())).willReturn(Optional.of(assignedResource));
        given(allocationService.getResource(any())).willReturn(Optional.of(resource));
        given(allocationService.getTaskAssignment((String) any())).willReturn(Optional.of(taskAssignment));

        mockMvc.perform(post("/api/tasks/assignments/resource/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"assignedResourceId\":1,\"memberEmail\":\"test@example.com\",\"start\":true}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.memberEmail").value("test@example.com"))
                .andExpect(jsonPath("$.start").value(true));
    }

    @Test
    @WithMockUser(roles = "MEMBRO")
    void startAssignmentWithInvalidResourceIdShouldReturnNotFound() throws Exception {
        given(allocationService.getAssignedResource(any())).willReturn(Optional.empty());

        mockMvc.perform(post("/api/tasks/assignments/resource/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"assignedResourceId\":999,\"memberEmail\":\"test@example.com\",\"start\":true}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "MEMBRO")
    void startAssignmentWithEmptyResourceShouldReturnBadRequest() throws Exception {
        given(allocationService.getAssignedResource(any())).willReturn(Optional.of(assignedResource));
        given(allocationService.getResource(any())).willReturn(Optional.empty());

        mockMvc.perform(post("/api/tasks/assignments/resource/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"assignedResourceId\":999,\"memberEmail\":\"test2@example.com\",\"start\":true}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "MEMBRO")
    void startAssignmentWithInvalidMemberEmailShouldReturnBadRequest() throws Exception {
        given(allocationService.getAssignedResource(any())).willReturn(Optional.of(assignedResource));
        given(allocationService.getResource(any())).willReturn(Optional.of(resource));

        mockMvc.perform(post("/api/tasks/assignments/resource/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"assignedResourceId\":999,\"memberEmail\":\"test2@example.com\",\"start\":true}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "MEMBRO")
    void startAssignmentWithStartFalseShouldUpdateCompletedTime() throws Exception {
        payloadRequestDTO.setStart(false);
        payloadRequestDTO.setStop(true);

        given(allocationService.getAssignedResource(any())).willReturn(Optional.of(assignedResource));
        given(allocationService.getResource(any())).willReturn(Optional.of(resource));
        given(allocationService.getTaskAssignment((String) any())).willReturn(Optional.of(taskAssignment));

        mockMvc.perform(post("/api/tasks/assignments/resource/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"assignedResourceId\":\"1\",\"memberEmail\":\"test@example.com\",\"start\":false, \"stop\":  true}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.memberEmail").value("test@example.com"))
                .andExpect(jsonPath("$.stop").value(true));
    }

    @Test
    @WithMockUser(roles = "MEMBRO")
    void startAssignmentWithoutStartOrStopShouldThrowException() throws Exception {
        mockMvc.perform(post("/api/tasks/assignments/resource/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"assignedResourceId\":\"1\",\"memberEmail\":\"test@example.com\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "MEMBRO")
    void startAssignmentWithTaskAssignmentNotFoundShouldThrowException() throws Exception {
        given(allocationService.getAssignedResource(any())).willReturn(Optional.of(assignedResource));
        given(allocationService.getResource(any())).willReturn(Optional.of(resource));
        given(allocationService.getTaskAssignment((String) any())).willReturn(Optional.empty());

        mockMvc.perform(post("/api/tasks/assignments/resource/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"assignedResourceId\":\"1\",\"memberEmail\":\"test@example.com\",\"start\":false, \"stop\":  true}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "MEMBRO")
    void startAssignmentWithoutAssignedResourceIdAndMemberEmailShouldThrowException() throws Exception {
        mockMvc.perform(post("/api/tasks/assignments/resource/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"assignedResourceId\":\"1\",\"memberEmail\":\"test@example.com\",\"start\":false, \"stop\":  true}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "UTENTE")
    void startAssignmentWithUnauthorizedUserShouldThrowException() throws Exception {
        mockMvc.perform(post("/api/tasks/assignments/resource/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"assignedResourceId\":\"1\",\"memberEmail\":\"test@example.com\",\"start\":false, \"stop\":  true}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "MEMBRO")
    void getAssignmentWithValidRequestReturnsResourceInfo() throws Exception {
        given(allocationService.getAssignedResource(any())).willReturn(Optional.of(assignedResource));
        given(allocationService.getResource(any())).willReturn(Optional.of(resource));

        mockMvc.perform(post("/api/tasks/assignments/resource/info")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"assignedResourceId\":\"1\",\"memberEmail\":\"test@example.com\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.hardwareName").value(resource.getName()));
    }

    @Test
    @WithMockUser(roles = "MEMBRO")
    void getAssignmentWithUnassignedResourceThrowsException() throws Exception {
        given(allocationService.getAssignedResource(any())).willReturn(Optional.empty());

        mockMvc.perform(post("/api/tasks/assignments/resource/info")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"assignedResourceId\":\"1\",\"memberEmail\":\"test@example.com\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "MEMBRO")
    void getAssignmentWithNonExistentResourceThrowsException() throws Exception {
        given(allocationService.getAssignedResource(any())).willReturn(Optional.of(assignedResource));
        given(allocationService.getResource(any())).willReturn(Optional.empty());

        mockMvc.perform(post("/api/tasks/assignments/resource/info")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"assignedResourceId\":\"1\",\"memberEmail\":\"test@example.com\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "MEMBRO")
    void getAssignmentWithMismatchedMemberEmailThrowsException() throws Exception {
        given(allocationService.getAssignedResource(any())).willReturn(Optional.of(assignedResource));
        given(allocationService.getResource(any())).willReturn(Optional.of(resource));

        mockMvc.perform(post("/api/tasks/assignments/resource/info")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"assignedResourceId\":\"1\",\"memberEmail\":\"wrong@example.com\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "MEMBRO")
    void getAssignmentWithoutAssignedResourceIdOrMemberEmailThrowsException() throws Exception {
        mockMvc.perform(post("/api/tasks/assignments/resource/info")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "UTENTE")
    void getAssignmentWithUnauthorizedUserShouldThrowException() throws Exception {
        mockMvc.perform(post("/api/tasks/assignments/resource/info")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isForbidden());
    }
}