package com.kodilla.travelplannerfront.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WeatherResponseDto {

    private Long id;
    private LocalDate forecastDate;
    private double avgTemperatureC;
    private double precipitationChance;
    private double windSpeedMps;
    private String generalConditions;
    private LocalDateTime lastFetchedAt;
}
