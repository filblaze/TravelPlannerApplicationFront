package com.kodilla.travelplannerfront.security;

import com.kodilla.travelplannerfront.client.AuthenticationRestClient;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Optional;

@Service
public class AuthenticationService {

    private String username;
    private String password;
    private Long userId;

    private final AuthenticationRestClient authRestClient; // Używamy do weryfikacji i pobrania ID

    public AuthenticationService(AuthenticationRestClient authRestClient) {
        this.authRestClient = authRestClient;
    }

    public void authenticate(String username, String password) throws BadCredentialsException {

        // 1. Weryfikacja poświadczeń i pobranie ID z Back-endu (synchronicznie!)
        Optional<Long> userIdOptional = authRestClient.verifyLoginAndGetUserId(username, password);

        if (userIdOptional.isEmpty()) {
            // Jeśli REST Client zwróci błąd (np. 401 Unauthorized), to zgłaszamy błąd uwierzytelnienia
            throw new BadCredentialsException("Nieprawidłowa nazwa użytkownika lub hasło.");
        }

        // 2. Ręczne ustawienie kontekstu bezpieczeństwa LOKALNIE
        this.username = username;
        this.password = password;
        this.userId = userIdOptional.get();

        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(
                // Tworzymy użytkownika, który będzie widoczny dla Spring Security
                new org.springframework.security.core.userdetails.User(username, password, Collections.emptyList()),
                password,
                Collections.emptyList()
        );

        SecurityContextHolder.getContext().setAuthentication(token);
    }

    // ... (pozostałe metody bez zmian, ponieważ są używane przez Resztę Vaadin) ...
    public boolean isAuthenticated() {
        return username != null && password != null;
    }

    public String getUsername() {
        return username;
    }

    // ... reszta getterów (getPassword, getUserId, logout) ...
    public String getPassword() {
        return password;
    }

    public Long getUserId() {
        return userId;
    }

    public void logout() {
        this.username = null;
        this.password = null;
        this.userId = null;
        SecurityContextHolder.clearContext();
    }
}