package com.example.robsfirstapp;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private List<Note> notes = new ArrayList<>();
    private NoteAdapter adapter;
    private EditText noteTitle, noteContent;
    private Note selectedNote = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        noteTitle = findViewById(R.id.noteTitle);
        noteContent = findViewById(R.id.noteContent);
        Button saveButton = findViewById(R.id.saveButton);

        // Adapter setzen und Notiz beim Klicken auf ein Element auswählen
        adapter = new NoteAdapter(notes, note -> {
            selectedNote = note;
            noteTitle.setText(note.getTitle());
            noteContent.setText(note.getContent());
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        saveButton.setOnClickListener(v -> saveNote());
    }

    private void saveNote() {
        String title = noteTitle.getText().toString();
        String content = noteContent.getText().toString();

        if (selectedNote != null) {
            // Wenn eine Notiz ausgewählt ist, bearbeite sie
            selectedNote.setTitle(title);
            selectedNote.setContent(content);
            adapter.notifyItemChanged(notes.indexOf(selectedNote));  // RecyclerView aktualisieren
        } else {
            // Falls keine Notiz ausgewählt ist, füge eine neue hinzu
            notes.add(new Note(notes.size(), title, content));
            adapter.notifyItemInserted(notes.size() - 1);  // RecyclerView aktualisieren
        }

        // Felder zurücksetzen
        noteTitle.setText("");
        noteContent.setText("");
        selectedNote = null;  // Auswahl zurücksetzen
    }
}
