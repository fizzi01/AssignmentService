package it.unisalento.pasproject.assignmentservice.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.time.LocalTime;

@Getter
@Setter
public class Availability {
    public enum DayOfWeek {
        MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY, SUNDAY
    }

    @JsonProperty("dayOfWeek")
    private DayOfWeek dayOfWeek;
    @JsonProperty("startTime")
    @JsonFormat(pattern = "HH:mm:ss.SSS")
    private LocalTime startTime;
    @JsonProperty("endTime")
    @JsonFormat(pattern = "HH:mm:ss.SSS")
    private LocalTime endTime;
}
