package com.example.robsfirstapp.ui;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
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
    private NoteDatabase db;
    private RecyclerView recyclerView;
    private View dragHandle;
    private EditText noteTitle, noteContent;
    private Button saveButton, deleteButton, newNoteButton;

    private Note selectedNote = null;
    private NoteAdapter adapter;
    private List<Note> notes = new ArrayList<>();

    private boolean isLoadingNote = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        loadAllNotes();
        loadSelectedNote(deleteButton);
        setupRecyclerView();
        initListeners();
        dragHandler();
    }

    /**
     * initialise all UI elements and the database.
     */
    private void initViews() {
        db = Room.databaseBuilder(getApplicationContext(), NoteDatabase.class, "notes_database")
                .allowMainThreadQueries()
                .build();

        recyclerView = findViewById(R.id.recyclerView);
        dragHandle = findViewById(R.id.dragHandle);

        noteTitle = findViewById(R.id.noteTitle);
        noteContent = findViewById(R.id.noteContent);

        saveButton = findViewById(R.id.saveButton);
        deleteButton = findViewById(R.id.deleteButton);
        newNoteButton = findViewById(R.id.newNoteButton);
    }

    /**
     * set the layout manager for the RecyclerView (list with saved notes) and
     * set the adapter for the RecyclerView.
     */
    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    /**
     * adds click listeners for buttons.
     */
    private void initListeners() {
        newNoteButton.setOnClickListener(v -> createNewNote());
        saveButton.setOnClickListener(v -> saveNote());
        deleteButton.setOnClickListener(v -> deleteNote());

        autoSaveOnTextChange(noteTitle);
        autoSaveOnTextChange(noteContent);
    }

    private void autoSaveOnTextChange(EditText textToEdit) {
        textToEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!isLoadingNote) {
                    saveNote();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void loadSelectedNote(Button deleteButton) {
        adapter = new NoteAdapter(notes, note -> {
            isLoadingNote = true; // TextWatcher deaktivieren

            selectedNote = note;
            noteTitle.setText(note.getTitle());
            noteContent.setText(note.getContent());
            deleteButton.setEnabled(true);

            isLoadingNote = false; // TextWatcher wieder aktivieren
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
        String title = noteTitle.getText().toString().trim();
        String content = noteContent.getText().toString().trim();

        if (title.isEmpty()) {
            title = generateDefaultTitle();
        }

        String finalTitle = generateUniqueTitle(title);

        new Thread(() -> {
            if (selectedNote != null) {
                selectedNote.setTitle(finalTitle);
                selectedNote.setContent(content);
                db.noteDao().update(selectedNote);
            } else {
                Note newNote = new Note(finalTitle, content);
                long newId = db.noteDao().insert(newNote);
                newNote.setId((int) newId);

                notes.add(newNote);
                selectedNote = newNote;
            }

            runOnUiThread(() -> {
                adapter.notifyDataSetChanged();
            });

        }).start();
    }

    private void createNewNote() {
        selectedNote = null;
        noteTitle.setText(generateDefaultTitle());
        noteContent.setText("");
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

    // check for Titles that already exist START
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

    private String generateUniqueTitle(String title) {
        int counter = 1;
        String newTitle = title;

        List<Note> existingNotes = db.noteDao().getAllNotes();

        while (titleExists(existingNotes, newTitle)) {
            // Wenn die existierende Notiz die gleiche ID hat, ignoriere sie
            Note existingNote = getNoteByTitle(existingNotes, newTitle);
            if (existingNote != null && existingNote.getId() == (selectedNote != null ? selectedNote.getId() : -1)) {
                break;
            }

            // Generiere neuen Titel mit Zähler
            newTitle = String.format("%s%03d", title, counter);
            counter++;
        }

        return newTitle;
    }

    // Hilfsmethode, um eine Notiz basierend auf dem Titel zu finden
    private Note getNoteByTitle(List<Note> notes, String title) {
        for (Note note : notes) {
            if (note.getTitle().equals(title)) {
                return note;
            }
        }
        return null;
    }


    // Auxiliary method: Checks whether the title already exists.
    private boolean titleExists(List<Note> notes, String title) {
        for (Note note : notes) {
            if (note.getTitle().equals(title)) {
                return true;
            }
        }
        return false;
    }
    // check for Titles that already exist END

    private void dragHandler() {
        dragHandle.setOnTouchListener(new View.OnTouchListener() {
            private int minWidth = 80;
            private int maxWidth = 800;
            private int lastX;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        lastX = (int) event.getRawX();
                        return true;

                    case MotionEvent.ACTION_MOVE:
                        int deltaX = (int) event.getRawX() - lastX;
                        lastX = (int) event.getRawX();

                        int newWidth = recyclerView.getWidth() + deltaX;
                        newWidth = Math.max(minWidth, Math.min(maxWidth, newWidth));

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
