package com.kodilla.travelplannerfront.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class ReminderStatusUpdateDto {
    @JsonProperty("completed")
    private Boolean isCompleted;
}
