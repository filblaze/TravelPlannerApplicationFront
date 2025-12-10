package com.kodilla.travelplannerfront.views;

import com.kodilla.travelplannerfront.client.AuthenticationRestClient;
import com.kodilla.travelplannerfront.dto.UserRequestDto;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@Route("register")
@PageTitle("Rejestracja | Travel Planner")
@AnonymousAllowed
public class RegisterView extends VerticalLayout {

    private final AuthenticationRestClient authRestClient;

    private final TextField userName = new TextField("Nazwa użytkownika");
    private final EmailField email = new EmailField("Email");
    private final TextField country = new TextField("Kraj zamieszkania");
    private final PasswordField password = new PasswordField("Hasło");
    private final PasswordField confirmPassword = new PasswordField("Powtórz hasło");
    private final Button registerButton = new Button("Zarejestruj się");
    private final Button backToLogin = new Button("Wróć do logowania");

    public RegisterView(AuthenticationRestClient authRestClient) {
        this.authRestClient = authRestClient;

        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);
        setSizeFull();

        registerButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        registerButton.addClickListener(e -> registerNewUser());
        backToLogin.addClickListener(e -> UI.getCurrent().navigate(LoginView.class));

        add(new H2("Utwórz nowe konto"),
                userName, email, country, password, confirmPassword,
                registerButton, backToLogin);
    }

    private void registerNewUser() {
        if (!password.getValue().equals(confirmPassword.getValue())) {
            Notification.show("Hasła nie są identyczne!", 3000, Notification.Position.MIDDLE).addThemeVariants(NotificationVariant.LUMO_ERROR);
            return;
        }

        UserRequestDto requestDto = UserRequestDto.builder()
                .userName(userName.getValue())
                .email(email.getValue())
                .countryOfResidence(country.getValue())
                .password(password.getValue())
                .build();

        try {
            authRestClient.registerUser(requestDto);

            Notification.show("Rejestracja zakończona sukcesem! Możesz się zalogować.",
                    3000,
                    Notification.Position.MIDDLE).addThemeVariants(NotificationVariant.LUMO_SUCCESS);

            // Przekierowanie na stronę logowania
            UI.getCurrent().navigate(LoginView.class);

        } catch (WebClientResponseException.Conflict ex) {
            // 409 Conflict - np. użytkownik już istnieje (obsługa EntityExistsException z back-endu)
            Notification.show("Użytkownik o tej nazwie lub emailu już istnieje.",
                    3000,
                    Notification.Position.MIDDLE).addThemeVariants(NotificationVariant.LUMO_ERROR);
        } catch (Exception ex) {
            // Ogólny błąd
            Notification.show("Błąd rejestracji. Spróbuj ponownie. (" + ex.getMessage() + ")",
                    3000,
                    Notification.Position.MIDDLE).addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }
}
