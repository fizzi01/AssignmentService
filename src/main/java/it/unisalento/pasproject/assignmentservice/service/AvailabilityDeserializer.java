package it.unisalento.pasproject.assignmentservice.service;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import it.unisalento.pasproject.assignmentservice.domain.Availability;

import java.io.IOException;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class AvailabilityDeserializer extends JsonDeserializer<List<Availability>> {
    @Override
    public List<Availability> deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        var treeNode = p.getCodec().readTree(p);
        var availabilityList = new ArrayList<Availability>();

        if (treeNode instanceof ArrayNode arrayNode) {
            for (JsonNode node : arrayNode) {
                var availability = new Availability();

                var dayOfWeekString = node.get("dayOfWeek").asText();
                if (!dayOfWeekString.isEmpty()) {
                    availability.setDayOfWeek(DayOfWeek.valueOf(dayOfWeekString.toUpperCase()));
                }

                var startTimeString = node.get("startTime").asText();
                if (!startTimeString.isEmpty()) {
                    availability.setStartTime(LocalTime.parse(startTimeString));
                }

                var endTimeString = node.get("endTime").asText();
                if (!endTimeString.isEmpty()) {
                    availability.setEndTime(LocalTime.parse(endTimeString));
                }

                availabilityList.add(availability);
            }
        }

        return availabilityList;
    }
}
