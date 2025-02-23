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
    private List<Note> notes = new ArrayList<>();
    private NoteAdapter adapter;
    private EditText noteTitle, noteContent;
    private Note selectedNote = null;

    private NoteDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Datenbank initialisieren
        db = Room.databaseBuilder(getApplicationContext(), NoteDatabase.class, "notes_database").allowMainThreadQueries().build();

        // Das ist die Sidebar mit allen Notizen
        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        // Das ist der DragHandle für die Breitenanpassung
        View dragHandle = findViewById(R.id.dragHandle);

        noteTitle = findViewById(R.id.noteTitle);
        noteContent = findViewById(R.id.noteContent);
        Button saveButton = findViewById(R.id.saveButton);
        Button deleteButton = findViewById(R.id.deleteButton);
        Button newNoteButton = findViewById(R.id.newNoteButton);
        newNoteButton.setOnClickListener(v -> createNewNote());

        // Lade Notizen aus der DB
        loadAllNotes();
        // Adapter setzen und Notiz beim Klicken auf ein Element auswählen
        adapter = new NoteAdapter(notes, note -> {
            selectedNote = note;
            noteTitle.setText(note.getTitle());
            noteContent.setText(note.getContent());
            deleteButton.setEnabled(true);
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        saveButton.setOnClickListener(v -> saveNote());
        deleteButton.setOnClickListener(v -> deleteNote());

        // Drag-Logik hinzugefügt um größe des RecyclerViews anzupassen
        dragHandler(dragHandle, recyclerView);
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

    private static void dragHandler(View dragHandle, RecyclerView recyclerView) {
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
