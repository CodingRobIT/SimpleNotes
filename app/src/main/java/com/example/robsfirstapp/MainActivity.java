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
            selectedNote = new Note(selectedNote.getId(), title, content);
        } else {
            notes.add(new Note(notes.size(), title, content));
        }

        adapter.notifyDataSetChanged();
        noteTitle.setText("");
        noteContent.setText("");
        selectedNote = null;
    }
}
