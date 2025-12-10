package com.kodilla.travelplannerfront.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReminderUpdateDto {

    private String title;
    private String content;
    private LocalDateTime reminderTime;
    @JsonProperty("set")
    private Boolean isSet;
    @JsonProperty("completed")
    private Boolean isCompleted;
}
