package it.unisalento.pasproject.assignmentservice.business;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Component
public class CheckOutUtils {

    @Value("${credit.value.constant}")
    private double creditConstant;

    public double getCreditAmount(LocalDateTime start,
                                         LocalDateTime end,
                                         double energyConsumptionPerHour,
                                         double computationalPower) {

        ZoneOffset zoneOffset = ZoneOffset.UTC;

        double rawCreditAmount = ((end.toEpochSecond(zoneOffset) - start.toEpochSecond(zoneOffset)) * computationalPower) / (energyConsumptionPerHour * creditConstant);
        return Math.round(rawCreditAmount * 10.0) / 10.0;

        // Formula: [(end in seconds - start in seconds) * computationalPower] / [energyConsumptionPerHour * creditConstant]
    }
}
