package com.kodilla.travelplannerfront.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AiGeneratedContentResponseDto {

    private Long id;
    private String contentType;
    private String generatedContent;
    private LocalDateTime createdAt;
}
