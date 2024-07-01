package it.unisalento.pasproject.assignmentservice.business;

import it.unisalento.pasproject.assignmentservice.domain.Settings;
import it.unisalento.pasproject.assignmentservice.dto.SettingsDTO;
import it.unisalento.pasproject.assignmentservice.repositories.SettingsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Optional;

@Service
public class CheckOutUtils {

    @Value("${settings.id}")
    private String SETTINGS_ID;

    @Value("${credit.value.constant.init}")
    private double initialCreditConstant;

    private final SettingsRepository settingsRepository;

    @Autowired
    public CheckOutUtils(SettingsRepository settingsRepository) {
        this.settingsRepository = settingsRepository;
    }

    public Settings createSettings(Settings settings) {
        return settingsRepository.save(settings);
    }

    public Settings setSettings(Settings newSettings) {
        Optional<Settings> settings = settingsRepository.findById(SETTINGS_ID);
        if (settings.isPresent()) {
            Settings settingsToUpdate = settings.get();
            settingsToUpdate.setCreditConstant(newSettings.getCreditConstant());
            settingsToUpdate = settingsRepository.save(settingsToUpdate);
            return settingsToUpdate;
        }else{
            newSettings.setId(SETTINGS_ID);
            newSettings.setCreditConstant(newSettings.getCreditConstant());
            return settingsRepository.save(newSettings);
        }
    }

    public Settings getSettings() {
        Optional<Settings> settings = settingsRepository.findById(SETTINGS_ID);
        if (settings.isPresent()) {
            return settings.get();
        }else{
            Settings newSettings = new Settings();
            newSettings.setId(SETTINGS_ID);
            newSettings.setCreditConstant(initialCreditConstant);
            return createSettings(newSettings);
        }
    }

    public SettingsDTO getSettingsDTO(Settings settings) {
        SettingsDTO settingsDTO = new SettingsDTO();
        settingsDTO.setCreditConstant(settings.getCreditConstant());
        return settingsDTO;
    }

    public Settings getSettingsFromDTO(SettingsDTO settingsDTO) {
        Settings settings = new Settings();
        settings.setCreditConstant(settingsDTO.getCreditConstant());
        return settings;
    }

    public double getCreditAmount(LocalDateTime start,
                                         LocalDateTime end,
                                         double energyConsumptionPerHour,
                                         double computationalPower) {

        ZoneOffset zoneOffset = ZoneOffset.UTC;

        double creditConstant;

        try{
            creditConstant = getSettings().getCreditConstant();
        } catch (Exception e) {
            return 0; // If something goes wrong, return 0 credits :D
        }

        double rawCreditAmount = ((end.toEpochSecond(zoneOffset) - start.toEpochSecond(zoneOffset)) * computationalPower) / (energyConsumptionPerHour * creditConstant);
        return Math.round(rawCreditAmount * 10.0) / 10.0;

        // Formula: [(end in seconds - start in seconds) * computationalPower] / [energyConsumptionPerHour * creditConstant]
    }
}
