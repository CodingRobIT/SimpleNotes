package com.example.robsfirstapp;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private EditText editTextNote;
    private Button saveButton;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sharedPreferences = getSharedPreferences("NotizenApp", MODE_PRIVATE);

        editTextNote = findViewById(R.id.editTextNote);
        saveButton = findViewById(R.id.saveButton);

        // Gespeicherte Notiz beim Start laden
        editTextNote.setText(sharedPreferences.getString("note", ""));

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String note = editTextNote.getText().toString().trim();

                if (!note.isEmpty()) {
                    saveNote(note);
                    Toast.makeText(MainActivity.this, "Notiz gespeichert!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MainActivity.this, "Bitte eine Notiz eingeben!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void saveNote(String note) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("note", note);
        editor.apply();
    }
}
