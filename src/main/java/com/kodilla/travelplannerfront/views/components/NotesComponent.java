package com.kodilla.travelplannerfront.views.components;

import com.kodilla.travelplannerfront.client.TripPlanRestClient;
import com.kodilla.travelplannerfront.dto.NoteRequestDto;
import com.kodilla.travelplannerfront.dto.NoteResponseDto;
import com.kodilla.travelplannerfront.dto.NoteUpdateDto;
import com.kodilla.travelplannerfront.views.TripDetailView;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
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

import java.util.Collections;
import java.util.List;

public class NotesComponent extends VerticalLayout {

    private final Long tripPlanId;
    private final TripPlanRestClient tripPlanRestClient;
    private final Grid<NoteResponseDto> grid = new Grid<>(NoteResponseDto.class, false);
    private Button addNoteButton;

    public NotesComponent(Long tripPlanId, TripPlanRestClient tripPlanRestClient, List<NoteResponseDto> initialNotes) {
        this.tripPlanId = tripPlanId;
        this.tripPlanRestClient = tripPlanRestClient;

        H3 header = new H3("📋 Notatki");
        addNoteButton = new Button("Dodaj Notatkę", VaadinIcon.PLUS.create(), e -> openNoteDialog(null));
        addNoteButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        HorizontalLayout toolbar = new HorizontalLayout(header, addNoteButton);
        toolbar.setWidthFull();
        toolbar.setJustifyContentMode(JustifyContentMode.BETWEEN);

        configureGrid();
        refreshNotes(initialNotes);

        setWidthFull();
        add(toolbar, grid);
    }

    private void configureGrid() {
        grid.addColumn(NoteResponseDto::getTitle).setHeader("Tytuł");
        grid.addColumn(NoteResponseDto::getContent).setHeader("Treść");

        grid.addComponentColumn(this::createActionsLayout).setHeader("Akcje");
        grid.getColumns().forEach(col -> col.setAutoWidth(true));
        grid.setAllRowsVisible(true);
    }

    private HorizontalLayout createActionsLayout(NoteResponseDto note) {
        Button editButton = new Button(VaadinIcon.EDIT.create(), e -> openNoteDialog(note));
        Button deleteButton = new Button(VaadinIcon.TRASH.create(), e -> deleteNote(note.getId()));
        deleteButton.addThemeVariants(ButtonVariant.LUMO_ERROR);
        return new HorizontalLayout(editButton, deleteButton);
    }

    private void openNoteDialog(NoteResponseDto note) {
        Dialog dialog = new Dialog();
        boolean isNewNote = note == null;
        dialog.setHeaderTitle(isNewNote ? "Nowa Notatka" : "Edytuj Notatkę");

        Binder<NoteRequestDto> binder = new Binder<>(NoteRequestDto.class);

        NoteRequestDto noteDto = isNewNote ? new NoteRequestDto() : new NoteRequestDto(note.getTitle(), note.getContent());
        binder.setBean(noteDto);

        TextField titleField = new TextField("Tytuł");
        TextArea contentArea = new TextArea("Treść");
        contentArea.setWidthFull();

        binder.bind(titleField, NoteRequestDto::getTitle, NoteRequestDto::setTitle);
        binder.forField(contentArea)
                .withValidator(content -> !content.trim().isEmpty(), "Treść notatki nie może być pusta")
                .bind(NoteRequestDto::getContent, NoteRequestDto::setContent);

        dialog.add(new VerticalLayout(titleField, contentArea));

        Button saveButton = new Button("Zapisz", e -> {
            try {
                if (binder.isValid()) {
                    if (isNewNote) {
                        tripPlanRestClient.createNote(tripPlanId, noteDto);
                        Notification.show("Notatka dodana pomyślnie.", 3000, Notification.Position.MIDDLE).addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                    } else {
                        NoteUpdateDto updateDto = new NoteUpdateDto(noteDto.getTitle(), noteDto.getContent());
                        tripPlanRestClient.updateNote(note.getId(), updateDto);
                        Notification.show("Notatka zaktualizowana.", 3000, Notification.Position.MIDDLE).addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                    }
                    // Odświeżenie widoku głównego po zmianie
                    getUI().ifPresent(ui -> ui.navigate(TripDetailView.class, tripPlanId));
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

    private void deleteNote(Long noteId) {
        try {
            tripPlanRestClient.deleteNote(noteId);
            Notification.show("Notatka usunięta pomyślnie.", 3000, Notification.Position.MIDDLE).addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            // Odświeżenie widoku głównego po zmianie
            getUI().ifPresent(ui -> ui.navigate(TripDetailView.class, tripPlanId));
        } catch (Exception ex) {
            Notification.show("Błąd usuwania: " + ex.getMessage(), 5000, Notification.Position.MIDDLE).addThemeVariants(NotificationVariant.LUMO_ERROR);
        }
    }

    public void refreshNotes(List<NoteResponseDto> notes) {
        grid.setItems(notes != null ? notes : Collections.emptyList());
    }
}
