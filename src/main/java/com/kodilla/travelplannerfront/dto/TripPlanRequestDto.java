package com.kodilla.travelplannerfront.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TripPlanRequestDto {

    private String destinationName;
    private String destinationCountry;
    private LocalDate startDate;
    private LocalDate endDate;
}
