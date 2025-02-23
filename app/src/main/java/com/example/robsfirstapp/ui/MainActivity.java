package com.example.robsfirstapp.ui;

import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

import com.example.robsfirstapp.R;
import com.example.robsfirstapp.database.NoteDatabase;
import com.example.robsfirstapp.model.Note;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    // Klassenvariablen für UI-Elemente
    private NoteDatabase db;
    private RecyclerView recyclerView;
    private View dragHandle;
    private EditText noteTitle, noteContent;
    private Button saveButton, deleteButton, newNoteButton;

    // Aktuell ausgewählte Notiz
    private Note selectedNote = null;
    private NoteAdapter adapter;
    private List<Note> notes = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialisiert alle UI-Elemente und die Datenbank
        initViews();

        // Lädt alle gespeicherten Notizen aus der Datenbank
        loadAllNotes();

        // Setzt den Adapter für die Notizen-Liste und lädt die ausgewählte Notiz
        loadSelectedNote(deleteButton);

        // Setzt das RecyclerView auf (die Liste mit den Notizen)
        setupRecyclerView();

        // Fügt Klick-Listener für Buttons hinzu
        initListeners();

        // Aktiviert das Drag-Handle für die Breitenanpassung der Notizliste
        dragHandler();
    }

    /**
     * Initialisiert alle UI-Elemente und die Datenbank.
     */
    private void initViews() {
        // Datenbank initialisieren
        db = Room.databaseBuilder(getApplicationContext(), NoteDatabase.class, "notes_database")
                .allowMainThreadQueries() // Haupt-Thread-Zugriff erlauben (nicht ideal, aber für kleine Apps okay)
                .build();

        // Das ist die Sidebar mit allen Notizen
        recyclerView = findViewById(R.id.recyclerView);

        // Das ist der DragHandle für die Breitenanpassung der Notizliste
        dragHandle = findViewById(R.id.dragHandle);

        // Eingabefelder für Titel und Inhalt der Notiz
        noteTitle = findViewById(R.id.noteTitle);
        noteContent = findViewById(R.id.noteContent);

        // Buttons für Speichern, Löschen und neue Notiz erstellen
        saveButton = findViewById(R.id.saveButton);
        deleteButton = findViewById(R.id.deleteButton);
        newNoteButton = findViewById(R.id.newNoteButton);
    }

    /**
     * Setzt das RecyclerView auf (die Liste mit den gespeicherten Notizen).
     */
    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(this)); // Vertikale Liste
        recyclerView.setAdapter(adapter); // Adapter setzt die Notizen in die Liste
    }

    /**
     * Fügt Klick-Listener für Buttons hinzu.
     */
    private void initListeners() {
        // Erstellt eine neue Notiz, wenn der Button geklickt wird
        newNoteButton.setOnClickListener(v -> createNewNote());

        // Speichert die aktuell geöffnete Notiz
        saveButton.setOnClickListener(v -> saveNote());

        // Löscht die aktuell geöffnete Notiz
        deleteButton.setOnClickListener(v -> deleteNote());
    }

    private void loadSelectedNote(Button deleteButton) {
        adapter = new NoteAdapter(notes, note -> {
            selectedNote = note;
            noteTitle.setText(note.getTitle());
            noteContent.setText(note.getContent());
            deleteButton.setEnabled(true);
        });
    }

    private void loadAllNotes() {
        new Thread(() -> {
            List<Note> notesFromDb = db.noteDao().getAllNotes();
            runOnUiThread(() -> {
                notes.clear();
                notes.addAll(notesFromDb);
                adapter.notifyDataSetChanged();
            });
        }).start();
    }

    private void saveNote() {
        String title = noteTitle.getText().toString().trim(); // Entfernt unnötige Leerzeichen
        String content = noteContent.getText().toString().trim(); // Entfernt unnötige Leerzeichen

        if (content.isEmpty() && title.isEmpty()) {
            // Zeigt eine Toast-Meldung an, wenn der Inhalt fehlt
            runOnUiThread(() -> Toast.makeText(this, "Kein Inhalt!", Toast.LENGTH_SHORT).show());
            return;
        }

        // Falls kein Titel vorhanden ist, generiere "Notiz001", "Notiz002" usw.
        if (title.isEmpty()) {
            title = generateDefaultTitle();
            runOnUiThread(() -> Toast.makeText(this, "Kein Titel, Titel wurde Automatisch generierd", Toast.LENGTH_SHORT).show());
        }

        String finalTitle = title;
        new Thread(() -> {
            if (selectedNote != null) {
                // Bestehende Notiz aktualisieren
                selectedNote.setTitle(finalTitle);
                selectedNote.setContent(content);
                db.noteDao().update(selectedNote);
                runOnUiThread(() -> Toast.makeText(this, "Notiz Aktualisiert", Toast.LENGTH_SHORT).show());
            } else {
                // Neue Notiz speichern
                Note newNote = new Note(finalTitle, content);
                long newId = db.noteDao().insert(newNote); // Speichern in der DB
                newNote.setId((int) newId); // ID setzen, falls Room sie automatisch vergibt

                // Die Liste mit der neuen Notiz aktualisieren
                notes.add(newNote);
                selectedNote = newNote; // Setzt die neue Notiz als aktuell ausgewählte
                runOnUiThread(() -> Toast.makeText(this, "Notiz Gespeichert", Toast.LENGTH_SHORT).show());
            }

            // UI-Update auf dem Hauptthread
            runOnUiThread(() -> {
                adapter.notifyDataSetChanged();
            });
        }).start();
    }

    private void createNewNote() {
        // Leere Felder setzen, um eine neue Notiz zu erstellen
        noteTitle.setText("");
        noteContent.setText("");
        selectedNote = null;
    }

    private void deleteNote() {
        if (selectedNote != null) {
            db.noteDao().delete(selectedNote);
            notes.remove(selectedNote);
            adapter.notifyDataSetChanged();

            noteTitle.setText("");
            noteContent.setText("");
            selectedNote = null;
            findViewById(R.id.deleteButton).setEnabled(false);
        }
    }

    private String generateDefaultTitle() {
        int counter = 1;
        String newTitle;

        List<Note> existingNotes = db.noteDao().getAllNotes(); // Alle Notizen aus DB abrufen

        do {
            newTitle = String.format("Notiz%03d", counter); // Erstellt Notiz001, Notiz002, etc.
            counter++;
        } while (titleExists(existingNotes, newTitle));

        return newTitle;
    }

    // Hilfsmethode: Prüft, ob der Titel bereits existiert
    private boolean titleExists(List<Note> notes, String title) {
        for (Note note : notes) {
            if (note.getTitle().equals(title)) {
                return true;
            }
        }
        return false;
    }

    private void dragHandler() {
        dragHandle.setOnTouchListener(new View.OnTouchListener() {
            private int minWidth = 80;  // Mindestbreite für RecyclerView
            private int maxWidth = 800; // Maximale Breite für RecyclerView
            private int lastX;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        lastX = (int) event.getRawX(); // Startposition merken
                        return true;

                    case MotionEvent.ACTION_MOVE:
                        int deltaX = (int) event.getRawX() - lastX; // Bewegung berechnen
                        lastX = (int) event.getRawX(); // Position aktualisieren

                        // Neue Breite berechnen (mit Mindest- & Maximalwert)
                        int newWidth = recyclerView.getWidth() + deltaX;
                        newWidth = Math.max(minWidth, Math.min(maxWidth, newWidth));

                        // Neue Breite setzen
                        ViewGroup.LayoutParams params = recyclerView.getLayoutParams();
                        params.width = newWidth;
                        recyclerView.setLayoutParams(params);
                        return true;
                }
                return false;
            }
        });
    }
}
