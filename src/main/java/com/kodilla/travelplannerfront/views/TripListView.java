package com.kodilla.travelplannerfront.views;


import com.kodilla.travelplannerfront.client.TripPlanRestClient;
import com.kodilla.travelplannerfront.dto.TripPlanResponseDto;
import com.kodilla.travelplannerfront.security.AuthenticationService;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;
import com.vaadin.flow.router.RouteParameters;
import com.vaadin.flow.router.RouteConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;

@Route(value = "", layout = MainLayout.class)
@PageTitle("Moje Plany Podróży | Travel Planner")
@PermitAll
public class TripListView extends VerticalLayout {

    private static final Logger logger = LoggerFactory.getLogger(TripListView.class);

    private final TripPlanRestClient tripPlanRestClient;
    private final AuthenticationService authService;
    private final Grid<TripPlanResponseDto> grid = new Grid<>(TripPlanResponseDto.class);

    public TripListView(TripPlanRestClient tripPlanRestClient, AuthenticationService authService) {
        this.tripPlanRestClient = tripPlanRestClient;
        this.authService = authService;

        addClassName("trip-list-view");
        setSizeFull();
        configureGrid();

        add(createToolbar(), grid);
        updateList();
    }

    private HorizontalLayout createToolbar() {
        Button profileButton = new Button("Profil", VaadinIcon.USER.create());
        profileButton.addClickListener(e -> Notification.show("Nawigacja do Profilu (TODO)"));

        Button newTripButton = new Button("Stwórz nowy plan", VaadinIcon.PLUS.create());
        newTripButton.addClickListener(e -> openCreateTripDialog());

        HorizontalLayout toolbar = new HorizontalLayout(profileButton, newTripButton);
        toolbar.setWidthFull();
        toolbar.setJustifyContentMode(JustifyContentMode.END);

        return toolbar;
    }

    private void openCreateTripDialog() {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Stwórz Nowy Plan Podróży");

        // Użyjemy tymczasowego DTO do stworzenia nowego TripPlan
        // Należy upewnić się, że TripPlanRequestDto jest dostępny i ma buildera/konstruktor
        // (Zakładam, że DTO jest zdefiniowane i działa)
        class TempTripPlanRequestDto {
            public String destinationName;
            public String destinationCountry;
            public java.time.LocalDate startDate;
            public java.time.LocalDate endDate;
        }

        Binder<TempTripPlanRequestDto> binder = new Binder<>(TempTripPlanRequestDto.class);
        TempTripPlanRequestDto newTrip = new TempTripPlanRequestDto();
        binder.setBean(newTrip);

        TextField destinationName = new TextField("Nazwa Destynacji");
        TextField destinationCountry = new TextField("Kraj");
        DatePicker startDate = new DatePicker("Data rozpoczęcia");
        DatePicker endDate = new DatePicker("Data zakończenia");

        binder.bind(destinationName, "destinationName");
        binder.bind(destinationCountry, "destinationCountry");
        binder.bind(startDate, "startDate");
        binder.bind(endDate, "endDate");

        // Walidacja nazwy
        binder.forField(destinationName)
                .withValidator(name -> !name.trim().isEmpty(), "Nazwa destynacji nie może być pusta")
                .bind(d -> d.destinationName, (d, name) -> d.destinationName = name);

        // Walidacja dat
        binder.forField(startDate)
                .withValidator(date -> date != null, "Proszę wybrać datę rozpoczęcia")
                .bind(d -> d.startDate, (d, date) -> d.startDate = date);

        Button saveButton = new Button("Zapisz", e -> {
            try {
                if (binder.validate().isOk()) {
                    Long userId = authService.getUserId();

                    // Konwersja do właściwego DTO (zakładając, że masz je zdefiniowane i zaimportowane)
                    com.kodilla.travelplannerfront.dto.TripPlanRequestDto requestDto = com.kodilla.travelplannerfront.dto.TripPlanRequestDto.builder()
                            .destinationName(newTrip.destinationName)
                            .destinationCountry(newTrip.destinationCountry)
                            .startDate(newTrip.startDate)
                            .endDate(newTrip.endDate)
                            .build();

                    tripPlanRestClient.createTripPlan(userId, requestDto);
                    Notification.show("Plan utworzony pomyślnie.", 3000, Notification.Position.MIDDLE).addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                    updateList();
                    dialog.close();
                } else {
                    Notification.show("Proszę wypełnić wszystkie wymagane pola.", 3000, Notification.Position.MIDDLE).addThemeVariants(NotificationVariant.LUMO_ERROR);
                }
            } catch (Exception ex) {
                Notification.show("Błąd: " + ex.getMessage(), 5000, Notification.Position.MIDDLE).addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button cancelButton = new Button("Anuluj", e -> dialog.close());

        dialog.add(new VerticalLayout(destinationName, destinationCountry, startDate, endDate));
        dialog.getFooter().add(cancelButton, saveButton);
        dialog.open();
    }

    private void configureGrid() {
        grid.addClassName("trip-plan-grid");
        grid.setSizeFull();

        grid.setColumns("destinationName", "destinationCountry");

        DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        grid.addColumn(trip -> trip.getStartDate().format(dateFormat)).setHeader("Data rozpoczęcia");
        grid.addColumn(trip -> trip.getEndDate().format(dateFormat)).setHeader("Data zakończenia");

        // Kolumna z przyciskami
        grid.addComponentColumn(trip -> {
                    Button detailsButton = new Button("Szczegóły", click -> navigateToDetails(trip.getId()));
                    Button deleteButton = new Button("Usuń", VaadinIcon.TRASH.create());
                    deleteButton.addThemeVariants(ButtonVariant.LUMO_ERROR);

                    deleteButton.addClickListener(e -> confirmAndDeleteTrip(trip));

                    return new HorizontalLayout(detailsButton, deleteButton);
                })
                .setHeader("Akcje");

        grid.getColumns().forEach(col -> col.setAutoWidth(true));
    }

    private void confirmAndDeleteTrip(TripPlanResponseDto trip) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Potwierdzenie usunięcia");

        dialog.add(new Text("Czy na pewno chcesz usunąć plan podróży do: " + trip.getDestinationName() + "? Tej operacji nie można cofnąć."));

        Button deleteButton = new Button("Usuń", e -> {
            try {
                tripPlanRestClient.deleteTripPlan(trip.getId());
                Notification.show("Plan podróży usunięty pomyślnie.", 3000, Notification.Position.MIDDLE)
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                updateList();
                dialog.close();
            } catch (Exception ex) {
                Notification.show("Błąd podczas usuwania: " + ex.getMessage(), 5000, Notification.Position.MIDDLE)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });
        deleteButton.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_PRIMARY);

        Button cancelButton = new Button("Anuluj", e -> dialog.close());

        dialog.getFooter().add(cancelButton, deleteButton);
        dialog.open();
    }

    private void updateList() {
        Long userId = authService.getUserId();
        if (userId != null) {
            List<TripPlanResponseDto> tripPlans = tripPlanRestClient.getAllTripPlansForUser(userId);
            grid.setItems(tripPlans);
        } else {
            grid.setItems(Collections.emptyList());
        }
    }

    private void navigateToDetails(Long tripPlanId) {
        if (tripPlanId == null || tripPlanId <= 0) {
            // ... Logika błędu
            return;
        }

        logger.info("Przygotowanie nawigacji do TripDetailView. ID: {}, (Kanoniczny HasUrlParameter)", tripPlanId);

        try {
            // Użycie kanonicznej formy: Klasa Widoku + Obiekt Parametru (Long)
            UI.getCurrent().navigate(TripDetailView.class, tripPlanId);

        } catch (Exception e) {
            // Użycie Notification.show() aby zobaczyć błąd na ekranie, jeśli Vaadin go wyłapie
            logger.error("Błąd podczas kanonicznej nawigacji do TripDetailView z ID: {} : {}", tripPlanId, e.getMessage());
            Notification.show("Błąd nawigacji Vaadin: " + e.getMessage(), 5000, Notification.Position.MIDDLE).addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }
}
