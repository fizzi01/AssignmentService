package it.unisalento.pasproject.assignmentservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.unisalento.pasproject.assignmentservice.TestSecurityConfig;
import it.unisalento.pasproject.assignmentservice.business.CheckOutUtils;
import it.unisalento.pasproject.assignmentservice.controllers.SettingsController;
import it.unisalento.pasproject.assignmentservice.domain.Settings;
import it.unisalento.pasproject.assignmentservice.dto.SettingsDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SettingsController.class)
@AutoConfigureMockMvc
@ExtendWith(MockitoExtension.class)
@Import(TestSecurityConfig.class)
public class SettingsControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CheckOutUtils checkOutUtils;

    private Logger logger = LoggerFactory.getLogger(SettingsControllerTest.class);

    private Settings settings;

    private SettingsDTO settingDTO;

    @BeforeEach
    void setUp() {
        settings = new Settings();
        settings.setId("1");
        settings.setCreditConstant(1.0);

        settingDTO = new SettingsDTO();
        settingDTO.setCreditConstant(1.0);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getSettings_whenCalled_returnsSettingsDTO() throws Exception {
        given(checkOutUtils.getSettingsDTO(checkOutUtils.getSettings())).willReturn(settingDTO);

        mockMvc.perform(get("/api/assignments/settings/get")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.creditConstant").value(1.0));
    }

    @Test
    @WithMockUser(roles = "UTENTE")
    void getSettings_whenUserNotAuthorized_returnsForbidden() throws Exception {
        mockMvc.perform(get("/api/assignments/settings/get")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void setSettings_whenCalledByAdmin_updatesSettingsSuccessfully() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();

        given(checkOutUtils.getSettingsFromDTO(any(SettingsDTO.class))).willReturn(settings);
        given(checkOutUtils.setSettings(any(Settings.class))).willReturn(settings);
        given(checkOutUtils.getSettingsDTO(any(Settings.class))).willReturn(settingDTO);

        mockMvc.perform(put("/api/assignments/settings/set")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(settingDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.creditConstant").value(1.0));
    }

    @Test
    @WithMockUser(roles = "USER")
    void setSettings_whenCalledByNonAdmin_returnsForbidden() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();

        mockMvc.perform(put("/api/assignments/settings/set")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(settingDTO)))
                .andExpect(status().isForbidden());
    }
}
