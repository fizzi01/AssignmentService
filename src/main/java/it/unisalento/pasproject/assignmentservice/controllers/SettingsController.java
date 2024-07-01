package it.unisalento.pasproject.assignmentservice.controllers;

import it.unisalento.pasproject.assignmentservice.business.CheckOutUtils;
import it.unisalento.pasproject.assignmentservice.dto.SettingsDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/assignments/settings")
public class SettingsController {

    private final CheckOutUtils checkOutUtils;

    @Autowired
    public SettingsController(CheckOutUtils checkOutUtils) {
        this.checkOutUtils = checkOutUtils;
    }

    @GetMapping("/get")
    public SettingsDTO getSettings() {
       return checkOutUtils.getSettingsDTO(checkOutUtils.getSettings());
    }

    @GetMapping("/set")
    public SettingsDTO setSettings(SettingsDTO settingsDTO) {
        return checkOutUtils.getSettingsDTO(checkOutUtils.setSettings(checkOutUtils.getSettingsFromDTO(settingsDTO)));
    }
}
