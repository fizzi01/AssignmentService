package it.unisalento.pasproject.assignmentservice.controllers;

import it.unisalento.pasproject.assignmentservice.business.CheckOutUtils;
import it.unisalento.pasproject.assignmentservice.domain.Settings;
import it.unisalento.pasproject.assignmentservice.dto.SettingsDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

import static it.unisalento.pasproject.assignmentservice.security.SecurityConstants.ROLE_ADMIN;

@RestController
@RequestMapping("/api/assignments/settings")
public class SettingsController {

    private final CheckOutUtils checkOutUtils;

    @Autowired
    public SettingsController(CheckOutUtils checkOutUtils) {
        this.checkOutUtils = checkOutUtils;
    }

    @GetMapping("/get")
    @Secured({ROLE_ADMIN})
    public SettingsDTO getSettings() {
       return checkOutUtils.getSettingsDTO(checkOutUtils.getSettings());
    }

    @PutMapping("/set")
    @Secured({ROLE_ADMIN})
    public SettingsDTO setSettings(@RequestBody SettingsDTO settingsDTO) {
        Settings settings = checkOutUtils.getSettingsFromDTO(settingsDTO);
        settings = checkOutUtils.setSettings(settings);

        return checkOutUtils.getSettingsDTO(settings);
    }
}
