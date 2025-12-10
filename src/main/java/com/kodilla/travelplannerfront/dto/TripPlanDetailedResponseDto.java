package com.kodilla.travelplannerfront.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TripPlanDetailedResponseDto {

    private Long id;
    private String destinationName;
    private String destinationCountry;
    private LocalDate startDate;
    private LocalDate endDate;
    private List<NoteResponseDto> notes;
    private List<ReminderResponseDto> reminders;
    private List<WeatherResponseDto> latestWeather;
    private CurrencyResponseDto latestCurrency;
    private List<AiGeneratedContentResponseDto> latestAiContent;
}
