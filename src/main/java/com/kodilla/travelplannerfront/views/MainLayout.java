package com.kodilla.travelplannerfront.views;

import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.RouterLink;
import com.vaadin.flow.spring.security.AuthenticationContext;

public class MainLayout extends AppLayout {

    private final AuthenticationContext authContext; // Do obsługi wylogowania

    public MainLayout(AuthenticationContext authContext) {
        this.authContext = authContext;
        createHeader();
        createDrawer();
    }

    private void createHeader() {
        H1 logo = new H1("Travel Planner");
        logo.addClassNames("text-l", "m-m");

        Button logoutButton = new Button("Wyloguj", e -> authContext.logout());

        HorizontalLayout header = new HorizontalLayout(new DrawerToggle(), logo, logoutButton);
        header.setDefaultVerticalComponentAlignment(VerticalLayout.Alignment.CENTER);
        header.expand(logo);
        header.setWidthFull();
        header.addClassNames("py-0", "px-m");

        addToNavbar(header);
    }

    private void createDrawer() {
        RouterLink tripListLink = new RouterLink("Plany Podróży", TripListView.class);

        addToDrawer(new VerticalLayout(
                tripListLink
                // Tutaj dodasz inne linki: np. RouterLink("Profil", ProfileView.class)
        ));
    }
}
