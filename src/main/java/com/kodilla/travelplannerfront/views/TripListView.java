package com.kodilla.travelplannerfront.views;

import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;

@Route(value = "", layout = MainLayout.class)
@PageTitle("Plan Podróży | Travel Planner")
@PermitAll
public class TripListView extends VerticalLayout {

    public TripListView() {
        add(new H2("Moje Plany Podróży"));
        add("Tutaj wkrótce będzie tabela z Twoimi planami podróży.");

        // Dalsza implementacja: Grid, pobieranie danych z REST
    }
}
