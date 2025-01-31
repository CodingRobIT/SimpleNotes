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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Im onCreate() SharedPreferences laden
        SharedPreferences sharedPreferences = getSharedPreferences("NotizenApp", MODE_PRIVATE);

        editTextNote = findViewById(R.id.editTextNote);
        saveButton = findViewById(R.id.saveButton);

        // Beim Starten der App gespeicherte Notiz anzeigen (falls vorhanden)
        String savedNote = sharedPreferences.getString("note", "");
        editTextNote.setText(savedNote);

        // Button Click Listener für das Speichern der Notiz
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String note = editTextNote.getText().toString();
                if (!note.isEmpty()) {
                    // Editor erst hier erstellen, um die Notiz zu speichern
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("note", note);
                    editor.apply(); // Änderungen speichern

                    // Toast-Nachricht zum Testen
                    Toast.makeText(MainActivity.this, "Notiz gespeichert: " + note, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MainActivity.this, "Bitte eine Notiz eingeben!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
