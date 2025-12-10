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
public class CurrencyResponseDto {

    private Long id;
    private String currencyName;
    private LocalDate exchangeRateDate;
    private Double exchangeRateToPln;
    private LocalDateTime lastFetchTime;
}
