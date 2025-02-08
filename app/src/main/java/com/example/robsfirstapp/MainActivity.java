package com.example.robsfirstapp;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

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

        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        noteTitle = findViewById(R.id.noteTitle);
        noteContent = findViewById(R.id.noteContent);
        Button saveButton = findViewById(R.id.saveButton);
        Button deleteButton = findViewById(R.id.deleteButton);

        // Lade Notizen aus der DB
        new Thread(() -> {
            List<Note> notesFromDb = db.noteDao().getAllNotes();
            runOnUiThread(() -> {
                notes.addAll(notesFromDb);
                adapter.notifyDataSetChanged();
            });
        }).start();

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

        loadNotes();
    }

    private void saveNote() {
        String title = noteTitle.getText().toString();
        String content = noteContent.getText().toString();

        if (selectedNote != null) {
            selectedNote.setTitle(title);
            selectedNote.setContent(content);

            // Speichern der Änderungen in der Datenbank
            new Thread(() -> db.noteDao().update(selectedNote)).start();
        } else {
            Note newNote = new Note(title, content);
            new Thread(() -> db.noteDao().insert(newNote)).start();
        }

        // UI anpassen und Textfelder leeren
        noteTitle.setText("");
        noteContent.setText("");
        selectedNote = null;
    }

    private void loadNotes() {
        notes.clear();
        notes.addAll(db.noteDao().getAllNotes());
        adapter.notifyDataSetChanged();
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
}
