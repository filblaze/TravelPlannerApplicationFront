package com.kodilla.travelplannerfront.views;

import com.kodilla.travelplannerfront.security.AuthenticationService;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.login.LoginForm;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;

@Route("login")
@PageTitle("Logowanie | Travel Planner")
@AnonymousAllowed
public class LoginView extends VerticalLayout implements BeforeEnterObserver {

    private final LoginForm login = new LoginForm();
    private final AuthenticationService authService;

    public LoginView(AuthenticationService authService) {
        this.authService = authService;

        addClassName("login-view");
        setSizeFull();
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);

        login.setAction("login");
        login.addLoginListener(event -> {
            boolean authenticated = authenticate(event.getUsername(), event.getPassword());
            if (authenticated) {
                UI.getCurrent().navigate(TripListView.class);
            } else {
                login.setError(true);
            }
        });

        add(new H1("Travel Planner"), login);
    }

    private boolean authenticate(String username, String password) {
        if (username.length() > 0 && password.length() > 0) {
            authService.authenticate(username, password);
            return true;
        }
        return false;
    }

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
