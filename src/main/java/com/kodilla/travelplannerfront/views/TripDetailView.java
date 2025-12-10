package com.kodilla.travelplannerfront.views;

import com.kodilla.travelplannerfront.client.TripPlanRestClient;
import com.kodilla.travelplannerfront.dto.TripPlanDetailedResponseDto;
import com.kodilla.travelplannerfront.views.components.NotesComponent;
import com.kodilla.travelplannerfront.views.components.RemindersComponent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.*;

import org.springframework.beans.factory.annotation.Autowired;

@Route(value = "trip", layout = MainLayout.class)
@PageTitle("Szczegóły Podróży | Travel Planner")
public class TripDetailView extends VerticalLayout implements HasUrlParameter<Long>, BeforeEnterObserver {

    private final TripPlanRestClient tripPlanRestClient;
    private Long tripPlanId;

    private H2 tripHeader;
    private VerticalLayout detailsLayout;

    private NotesComponent notesComponent;
    private RemindersComponent remindersComponent;

    @Autowired
    public TripDetailView(TripPlanRestClient tripPlanRestClient) {
        this.tripPlanRestClient = tripPlanRestClient;

        addClassName("trip-detail-view");
        setSizeFull();

        tripHeader = new H2("Ładowanie szczegółów podróży...");
        detailsLayout = new VerticalLayout(tripHeader);
        detailsLayout.setSpacing(true);
        detailsLayout.setPadding(true);
        detailsLayout.setWidth("80%");
        detailsLayout.setMaxWidth("1000px");
        detailsLayout.setAlignItems(Alignment.CENTER);

        add(detailsLayout);
    }

    @Override
    public void setParameter(BeforeEvent event, Long tripId) {
        this.tripPlanId = tripId;
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        if (tripPlanId == null) {
            event.rerouteTo(TripListView.class);
            return;
        }

        loadTripDetails();
    }

    private void loadTripDetails() {
        try {
            // 1. Pobranie szczegółowych danych
            TripPlanDetailedResponseDto details = tripPlanRestClient.getDetailedTripPlan(tripPlanId);

            // 2. Aktualizacja nagłówka
            tripHeader.setText(String.format("%s, %s",
                    details.getDestinationName(),
                    details.getDestinationCountry()));

            // Dodatkowe informacje
            Span dates = new Span(String.format("Od: %s do: %s",
                    details.getStartDate().toString(),
                    details.getEndDate().toString()));

            detailsLayout.removeAll();
            detailsLayout.add(tripHeader, dates);

            // 3. Inicjalizacja komponentów
            initializeComponents(details);

        } catch (IllegalStateException e) {
            // Użytkownik nie jest uwierzytelniony (mimo PermitAll, na wszelki wypadek)
            UI.getCurrent().navigate(LoginView.class);
        } catch (Exception e) {
            System.err.println("Błąd ładowania szczegółów podróży: " + e.getMessage());
            detailsLayout.add(new Span("Błąd ładowania szczegółów podróży: " + e.getMessage()));
        }
    }

    private void initializeComponents(TripPlanDetailedResponseDto details) {
        // Zapewniamy, że komponenty są inicjowane tylko raz
        if (notesComponent == null) {
            notesComponent = new NotesComponent(tripPlanId, tripPlanRestClient, details.getNotes());
            remindersComponent = new RemindersComponent(tripPlanId, tripPlanRestClient, details.getReminders());
            detailsLayout.add(notesComponent, remindersComponent);
        } else {
            // Jeśli komponenty istnieją, tylko aktualizujemy dane
            notesComponent.refreshNotes(details.getNotes());
            remindersComponent.refreshReminders(details.getReminders());
        }
    }

    // Metody wywoływane przez NotesComponent/RemindersComponent po zmianie (CRUD)
    public void refreshView() {
        // Całe odświeżanie strony (np. po dodaniu notatki)
        loadTripDetails();
    }
}
