package com.kodilla.travelplannerfront.views;

import com.kodilla.travelplannerfront.security.AuthenticationService;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.login.LoginForm;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import org.springframework.security.authentication.BadCredentialsException;

@Route("login")
@PageTitle("Logowanie | Travel Planner")
@AnonymousAllowed
public class LoginView extends VerticalLayout implements BeforeEnterObserver {

    private final LoginForm login = new LoginForm();
    private final AuthenticationService authService;
    // Usunięto: private final AuthenticationRestClient authRestClient; // Już wstrzykiwane przez authService

    public LoginView(AuthenticationService authService) {
        this.authService = authService;

        addClassName("login-view");
        setSizeFull();
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);

        login.addLoginListener(event -> {
            login.setEnabled(false);

            try {
                // Autentykacja: wywołujemy nasz serwis, który dzwoni do REST API
                authService.authenticate(event.getUsername(), event.getPassword());

                // Sukces: Nawigacja do głównego widoku.
                // Context Security jest ustawiony, więc nawigacja powinna działać.
                UI.getCurrent().navigate(TripListView.class);

            } catch (BadCredentialsException e) {
                // Błąd uwierzytelnienia z API
                login.setError(true);
                login.setEnabled(true);
            } catch (Exception e) {
                // Ogólny błąd (np. błąd połączenia REST)
                Notification.show("Błąd połączenia z API: " + e.getMessage(),
                                5000,
                                Notification.Position.MIDDLE)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
                login.setError(true);
                login.setEnabled(true);
            }
        });

        Button registerButton = new Button("Zarejestruj się");
        registerButton.addClickListener(e -> UI.getCurrent().navigate(RegisterView.class));

        VerticalLayout formLayout = new VerticalLayout(new H1("Travel Planner"), login, registerButton);
        formLayout.setAlignItems(Alignment.CENTER);

        add(formLayout);
    }

    // Usunięto starą metodę authenticate, która używała CompletableFuture

    @Override
    public void beforeEnter(BeforeEnterEvent beforeEnterEvent) {
        if(beforeEnterEvent.getLocation()
                .getQueryParameters()
                .getParameters()
                .containsKey("error")) {
            login.setError(true);
        }
    }
}
