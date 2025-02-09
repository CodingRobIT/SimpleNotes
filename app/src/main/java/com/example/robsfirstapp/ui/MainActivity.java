package com.example.robsfirstapp.ui;

import android.os.Bundle;
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

        db = Room.databaseBuilder(getApplicationContext(), NoteDatabase.class, "notes_database").allowMainThreadQueries().build();

        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        noteTitle = findViewById(R.id.noteTitle);
        noteContent = findViewById(R.id.noteContent);
        Button saveButton = findViewById(R.id.saveButton);
        Button deleteButton = findViewById(R.id.deleteButton);
        Button newNoteButton = findViewById(R.id.newNoteButton);
        newNoteButton.setOnClickListener(v -> createNewNote());

        new Thread(() -> {
            List<Note> notesFromDb = db.noteDao().getAllNotes();
            runOnUiThread(() -> {
                notes.clear();
                notes.addAll(notesFromDb);
                adapter.notifyDataSetChanged();
            });
        }).start();
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
        String title = noteTitle.getText().toString().trim();
        String content = noteContent.getText().toString().trim();

        if (content.isEmpty() && title.isEmpty()) {
            runOnUiThread(() -> Toast.makeText(this, "Kein Inhalt!", Toast.LENGTH_SHORT).show());
            return;
        }

        if (title.isEmpty()) {
            title = generateDefaultTitle();
            runOnUiThread(() -> Toast.makeText(this, "Kein Titel, Titel wurde Automatisch generierd", Toast.LENGTH_SHORT).show());
        }

        String finalTitle = title;
        new Thread(() -> {
            if (selectedNote != null) {
                selectedNote.setTitle(finalTitle);
                selectedNote.setContent(content);
                db.noteDao().update(selectedNote);
                runOnUiThread(() -> Toast.makeText(this, "Notiz Aktualisiert", Toast.LENGTH_SHORT).show());
            } else {
                Note newNote = new Note(finalTitle, content);
                long newId = db.noteDao().insert(newNote);
                newNote.setId((int) newId);

                notes.add(newNote);
                selectedNote = newNote;
                runOnUiThread(() -> Toast.makeText(this, "Notiz Gespeichert", Toast.LENGTH_SHORT).show());
            }

            runOnUiThread(() -> {
                adapter.notifyDataSetChanged();

                noteTitle.setText(selectedNote.getTitle());
                noteContent.setText(selectedNote.getContent());
            });
        }).start();
    }

    private void createNewNote() {
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

    private String generateDefaultTitle() {
        int counter = 1;
        String newTitle;

        List<Note> existingNotes = db.noteDao().getAllNotes();

        do {
            newTitle = String.format("Notiz%03d", counter);
            counter++;
        } while (titleExists(existingNotes, newTitle));

        return newTitle;
    }

    private boolean titleExists(List<Note> notes, String title) {
        for (Note note : notes) {
            if (note.getTitle().equals(title)) {
                return true;
            }
        }
        return false;
    }
}
