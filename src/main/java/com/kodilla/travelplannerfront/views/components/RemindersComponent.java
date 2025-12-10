package com.kodilla.travelplannerfront.views.components;

import com.kodilla.travelplannerfront.client.TripPlanRestClient;
import com.kodilla.travelplannerfront.dto.ReminderRequestDto;
import com.kodilla.travelplannerfront.dto.ReminderResponseDto;
import com.kodilla.travelplannerfront.dto.ReminderUpdateDto;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.datetimepicker.DateTimePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;

public class RemindersComponent extends VerticalLayout {

    private final Long tripPlanId;
    private final TripPlanRestClient tripPlanRestClient;
    private final Grid<ReminderResponseDto> grid = new Grid<>(ReminderResponseDto.class, false);
    private Button addReminderButton;
    private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    public RemindersComponent(Long tripPlanId, TripPlanRestClient tripPlanRestClient, List<ReminderResponseDto> initialReminders) {
        this.tripPlanId = tripPlanId;
        this.tripPlanRestClient = tripPlanRestClient;

        H3 header = new H3("⏰ Przypomnienia");
        addReminderButton = new Button("Dodaj Przypomnienie", VaadinIcon.PLUS.create(), e -> openReminderDialog(null));
        addReminderButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        HorizontalLayout toolbar = new HorizontalLayout(header, addReminderButton);
        toolbar.setWidthFull();
        toolbar.setJustifyContentMode(JustifyContentMode.BETWEEN);

        configureGrid();
        refreshReminders(initialReminders);

        setWidthFull();
        add(toolbar, grid);
    }

    private void configureGrid() {
        grid.addColumn(ReminderResponseDto::getTitle).setHeader("Tytuł");
        grid.addColumn(r -> r.getReminderTime().format(dateTimeFormatter)).setHeader("Data i Czas");

        // Kolumna Status (Completed)
        grid.addComponentColumn(this::createStatusToggle).setHeader("Ukończone");

        grid.addComponentColumn(this::createActionsLayout).setHeader("Akcje");
        grid.getColumns().forEach(col -> col.setAutoWidth(true));
        grid.setAllRowsVisible(true);
    }

    private Checkbox createStatusToggle(ReminderResponseDto reminder) {
        Checkbox checkbox = new Checkbox(reminder.getIsCompleted());
        checkbox.addValueChangeListener(event -> {
            try {
                tripPlanRestClient.markReminderAsComplete(reminder.getId(), event.getValue());
                Notification.show("Status zaktualizowany.", 1500, Notification.Position.MIDDLE);
                // W idealnym przypadku odświeżamy tylko ten jeden wiersz/całą listę przypomnień
                refreshReminders(tripPlanRestClient.getRemindersForTripPlan(tripPlanId));
            } catch (Exception e) {
                Notification.show("Błąd aktualizacji statusu: " + e.getMessage(), 5000, Notification.Position.MIDDLE).addThemeVariants(NotificationVariant.LUMO_ERROR);
                // Cofnij stan checkboxa w przypadku błędu
                checkbox.setValue(!event.getValue());
            }
        });
        return checkbox;
    }

    private HorizontalLayout createActionsLayout(ReminderResponseDto reminder) {
        Button editButton = new Button(VaadinIcon.EDIT.create(), e -> openReminderDialog(reminder));
        Button deleteButton = new Button(VaadinIcon.TRASH.create(), e -> deleteReminder(reminder.getId()));
        deleteButton.addThemeVariants(ButtonVariant.LUMO_ERROR);
        return new HorizontalLayout(editButton, deleteButton);
    }

    private void openReminderDialog(ReminderResponseDto reminder) {
        Dialog dialog = new Dialog();
        boolean isNewReminder = reminder == null;
        dialog.setHeaderTitle(isNewReminder ? "Nowe Przypomnienie" : "Edytuj Przypomnienie");

        Binder<ReminderRequestDto> binder = new Binder<>(ReminderRequestDto.class);

        // Mapowanie na DTO używane w formularzu
        ReminderRequestDto reminderDto = isNewReminder
                ? new ReminderRequestDto()
                : new ReminderRequestDto(reminder.getTitle(), reminder.getContent(), reminder.getReminderTime());
        binder.setBean(reminderDto);

        TextField titleField = new TextField("Tytuł");
        TextArea contentArea = new TextArea("Treść");
        DateTimePicker reminderTimePicker = new DateTimePicker("Data i Czas Przypomnienia");

        if (!isNewReminder) {
            reminderTimePicker.setValue(reminder.getReminderTime());
        }

        binder.bind(titleField, ReminderRequestDto::getTitle, ReminderRequestDto::setTitle);
        binder.forField(contentArea)
                .withValidator(content -> !content.trim().isEmpty(), "Treść przypomnienia nie może być pusta")
                .bind(ReminderRequestDto::getContent, ReminderRequestDto::setContent);
        binder.bind(reminderTimePicker, ReminderRequestDto::getReminderTime, ReminderRequestDto::setReminderTime);

        dialog.add(new VerticalLayout(titleField, contentArea, reminderTimePicker));

        Button saveButton = new Button("Zapisz", e -> {
            try {
                if (binder.isValid()) {
                    if (isNewReminder) {
                        tripPlanRestClient.createReminder(tripPlanId, reminderDto);
                        Notification.show("Przypomnienie dodane pomyślnie.", 3000, Notification.Position.MIDDLE).addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                    } else {
                        // Tworzenie Update DTO z formularza i danych oryginalnych (isSet/isCompleted z oryginalnego obiektu)
                        ReminderUpdateDto updateDto = ReminderUpdateDto.builder()
                                .title(reminderDto.getTitle())
                                .content(reminderDto.getContent())
                                .reminderTime(reminderDto.getReminderTime())
                                .isSet(reminder.getIsSet())
                                .isCompleted(reminder.getIsCompleted())
                                .build();

                        tripPlanRestClient.updateReminder(reminder.getId(), updateDto);
                        Notification.show("Przypomnienie zaktualizowane.", 3000, Notification.Position.MIDDLE).addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                    }
                    // Odświeżenie widoku głównego po zmianie
                    refreshReminders(tripPlanRestClient.getRemindersForTripPlan(tripPlanId));
                    dialog.close();
                }
            } catch (Exception ex) {
                Notification.show("Błąd: " + ex.getMessage(), 5000, Notification.Position.MIDDLE).addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button cancelButton = new Button("Anuluj", e -> dialog.close());

        dialog.getFooter().add(cancelButton, saveButton);
        dialog.open();
    }

    private void deleteReminder(Long reminderId) {
        try {
            tripPlanRestClient.deleteReminder(reminderId);
            Notification.show("Przypomnienie usunięte pomyślnie.", 3000, Notification.Position.MIDDLE).addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            // Odświeżenie listy
            refreshReminders(tripPlanRestClient.getRemindersForTripPlan(tripPlanId));
        } catch (Exception ex) {
            Notification.show("Błąd usuwania: " + ex.getMessage(), 5000, Notification.Position.MIDDLE).addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    public void refreshReminders(List<ReminderResponseDto> reminders) {
        grid.setItems(reminders != null ? reminders : Collections.emptyList());
    }
}
