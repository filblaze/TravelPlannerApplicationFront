package com.kodilla.travelplannerfront.client;

import com.kodilla.travelplannerfront.dto.UserRequestDto;
import com.kodilla.travelplannerfront.dto.UserResponseDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

@Component
public class AuthenticationRestClient {

    private final RestTemplate restTemplate;
    private final String authApiUrl;

    public AuthenticationRestClient(RestTemplate restTemplate, @Value("${travel-planner.api.url}") String baseUrl) {
        this.restTemplate = restTemplate;
        this.authApiUrl = baseUrl + "/api/auth";
    }

    public UserResponseDto registerUser(UserRequestDto userRequestDto) {
        return restTemplate.postForObject(authApiUrl + "/register", userRequestDto, UserResponseDto.class);
    }

    private Long getUserId(String username, String password) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth(username, password);
        try {
            ResponseEntity<Long> response = restTemplate.exchange(
                    authApiUrl + "/user-id",
                    HttpMethod.GET,
                    new HttpEntity<>(headers),
                    Long.class
            );
            return response.getBody();
        } catch (HttpClientErrorException ex) {
            System.err.println("Błąd uwierzytelnienia (Status: " + ex.getStatusCode() + ")");
            return null;
        } catch (Exception ex) {
            System.err.println("Błąd połączenia REST: " + ex.getMessage());
            return null;
        }
    }

    public Optional<Long> verifyLoginAndGetUserId(String username, String password) {
        Long userId = getUserId(username, password);
        return Optional.ofNullable(userId);
    }
}