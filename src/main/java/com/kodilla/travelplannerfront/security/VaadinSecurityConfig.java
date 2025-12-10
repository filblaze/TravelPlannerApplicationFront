package com.kodilla.travelplannerfront.security;

import com.kodilla.travelplannerfront.views.LoginView;
import com.vaadin.flow.spring.security.VaadinWebSecurity;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@EnableWebSecurity
public class VaadinSecurityConfig extends VaadinWebSecurity {

    private final AuthenticationService authenticationService;

    // Wstrzykujemy AuthenticationService do ewentualnego wykorzystania
    public VaadinSecurityConfig(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    // --- Konfiguracja SecurityFilterChain ---

    @Override
    protected void configure(HttpSecurity http) throws Exception {

        http.authorizeHttpRequests(auth -> auth
                // 1. Ścieżki do widoków publicznych (Rejestracja)
                .requestMatchers("/register", "/images/**", "/line-awesome/**").permitAll()

                // 2. KLUCZOWA ZMIANA: Jawna autoryzacja ścieżki /trip i jej parametrów
                .requestMatchers("/trip", "/trip/*").authenticated()
        );

        // 3. Wywołanie metody nadrzędnej
        // Ta metoda dodaje wszystkie niezbędne reguły Vaadin i na końcu regułę: .anyRequest().authenticated()
        super.configure(http);

        // 4. Ustawienie niestandardowej strony logowania
        setLoginView(http, LoginView.class);
    }

    // --- Konfiguracja Beanów ---

    /**
     * Udostępnia AuthenticationManager jako Bean. Jest potrzebny do ręcznej
     * autentykacji w AuthenticationService w celu weryfikacji poświadczeń.
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    /**
     * Udostępnia PasswordEncoder jako Bean. W Twoim przypadku (Basic Auth w Back-endzie),
     * jest to potrzebne, aby Spring Security na froncie wiedział, jak działa Twój Back-end.
     * Zwykle Vaadin używa tego do sprawdzania hasła na froncie, jeśli nie używasz REST.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}