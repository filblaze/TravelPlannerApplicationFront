package com.kodilla.travelplannerfront.client;

import com.kodilla.travelplannerfront.dto.*;
import com.kodilla.travelplannerfront.security.AuthenticationService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Collections;
import java.util.List;

@Component
public class TripPlanRestClient {

    private final WebClient tripWebClient;
    private final AuthenticationService authService;

    private final String tripApiBaseUrl;
    private final String externalApiBaseUrl;

    public TripPlanRestClient(WebClient.Builder webClientBuilder,
                              @Value("${travel-planner.api.url}") String backendBaseUrl,
                              AuthenticationService authService) {

        this.tripApiBaseUrl = backendBaseUrl + "/api/trips";
        this.externalApiBaseUrl = backendBaseUrl + "/api";

        this.tripWebClient = webClientBuilder.baseUrl(tripApiBaseUrl).build();
        this.authService = authService;
    }

    // ZMIANA: Metoda dostosowana do interfejsu Consumer<HttpHeaders> (void, przyjmuje HttpHeaders)
    protected void applyAuthHeaders(HttpHeaders headers) {
        headers.setBasicAuth(authService.getUsername(), authService.getPassword());
    }

    private WebClient createExternalClient() {
        return WebClient.builder().baseUrl(externalApiBaseUrl).build();
    }

    // =========================================================================
    //                            TRIP PLAN METHODS
    // =========================================================================

    public List<TripPlanResponseDto> getAllTripPlansForUser(Long userId) {
        if (!authService.isAuthenticated()) {
            return Collections.emptyList();
        }

        try {
            return tripWebClient.get()
                    .uri("/user/{userId}", userId)
                    .headers(this::applyAuthHeaders) // Użycie nowej referencji
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<List<TripPlanResponseDto>>() {})
                    .block();
        } catch (Exception e) {
            System.err.println("Błąd pobierania planów podróży: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    public TripPlanResponseDto createTripPlan(Long userId, TripPlanRequestDto requestDto) {
        if (!authService.isAuthenticated()) {
            throw new IllegalStateException("Użytkownik nie jest uwierzytelniony.");
        }

        return tripWebClient.post()
                .uri("/{userId}", userId)
                .headers(this::applyAuthHeaders)
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(requestDto))
                .retrieve()
                .bodyToMono(TripPlanResponseDto.class)
                .block();
    }

    public TripPlanDetailedResponseDto getDetailedTripPlan(Long tripPlanId) {
        if (!authService.isAuthenticated()) {
            throw new IllegalStateException("Użytkownik nie jest uwierzytelniony.");
        }

        return tripWebClient.get()
                .uri("/{tripPlanId}/details", tripPlanId)
                .headers(this::applyAuthHeaders)
                .retrieve()
                .bodyToMono(TripPlanDetailedResponseDto.class)
                .block();
    }

    public void deleteTripPlan(Long tripPlanId) {
        if (!authService.isAuthenticated()) {
            throw new IllegalStateException("Użytkownik nie jest uwierzytelniony.");
        }

        tripWebClient.delete()
                .uri("/{tripPlanId}", tripPlanId)
                .headers(this::applyAuthHeaders)
                .retrieve()
                .toBodilessEntity()
                .block();
    }

    // =========================================================================
    //                                NOTE METHODS
    // =========================================================================

    public List<NoteResponseDto> getNotesForTripPlan(Long tripPlanId) {
        WebClient notesClient = createExternalClient();

        return notesClient.get()
                .uri("/trips/{tripPlanId}/notes", tripPlanId)
                .headers(this::applyAuthHeaders)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<NoteResponseDto>>() {})
                .block();
    }

    public NoteResponseDto createNote(Long tripPlanId, NoteRequestDto requestDto) {
        WebClient notesClient = createExternalClient();

        return notesClient.post()
                .uri("/trip/{tripPlanId}/notes", tripPlanId)
                .headers(this::applyAuthHeaders)
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(requestDto))
                .retrieve()
                .bodyToMono(NoteResponseDto.class)
                .block();
    }

    public NoteResponseDto updateNote(Long noteId, NoteUpdateDto requestDto) {
        WebClient notesClient = createExternalClient();

        return notesClient.put()
                .uri("/notes/{noteId}", noteId)
                .headers(this::applyAuthHeaders)
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(requestDto))
                .retrieve()
                .bodyToMono(NoteResponseDto.class)
                .block();
    }

    public void deleteNote(Long noteId) {
        WebClient notesClient = createExternalClient();

        notesClient.delete()
                .uri("/notes/{noteId}", noteId)
                .headers(this::applyAuthHeaders)
                .retrieve()
                .toBodilessEntity()
                .block();
    }

    // =========================================================================
    //                              REMINDER METHODS
    // =========================================================================

    public List<ReminderResponseDto> getRemindersForTripPlan(Long tripPlanId) {
        WebClient remindersClient = createExternalClient();

        return remindersClient.get()
                .uri("/trips/{tripPlanId}/reminders", tripPlanId)
                .headers(this::applyAuthHeaders)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<ReminderResponseDto>>() {})
                .block();
    }

    public ReminderResponseDto createReminder(Long tripPlanId, ReminderRequestDto requestDto) {
        WebClient remindersClient = createExternalClient();

        return remindersClient.post()
                .uri("/trips/{tripPlanId}/reminders", tripPlanId)
                .headers(this::applyAuthHeaders)
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(requestDto))
                .retrieve()
                .bodyToMono(ReminderResponseDto.class)
                .block();
    }

    public ReminderResponseDto updateReminder(Long reminderId, ReminderUpdateDto requestDto) {
        WebClient remindersClient = createExternalClient();

        return remindersClient.put()
                .uri("/reminders/{reminderId}", reminderId)
                .headers(this::applyAuthHeaders)
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(requestDto))
                .retrieve()
                .bodyToMono(ReminderResponseDto.class)
                .block();
    }

    public void markReminderAsComplete(Long reminderId, Boolean isCompleted) {
        WebClient remindersClient = createExternalClient();
        ReminderStatusUpdateDto statusUpdate = new ReminderStatusUpdateDto(isCompleted);

        remindersClient.patch()
                .uri("/reminders/{reminderId}", reminderId)
                .headers(this::applyAuthHeaders)
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(statusUpdate))
                .retrieve()
                .toBodilessEntity()
                .block();
    }

    public void deleteReminder(Long reminderId) {
        WebClient remindersClient = createExternalClient();

        remindersClient.delete()
                .uri("/reminders/{reminderId}", reminderId)
                .headers(this::applyAuthHeaders)
                .retrieve()
                .toBodilessEntity()
                .block();
    }
}