package com.example.robsfirstapp;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {Note.class}, version = 1, exportSchema = false)
public abstract class NoteDatabase extends RoomDatabase {

    private static volatile NoteDatabase INSTANCE;

    // DAO für den Zugriff auf Notizen
    public abstract NoteDao noteDao();

    // Singleton-Pattern, um sicherzustellen, dass nur eine Instanz der DB existiert
    public static NoteDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (NoteDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    NoteDatabase.class, "note_database")
                            .fallbackToDestructiveMigration() // Optional, um bei einer Migration alte DB zu löschen
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
