package com.example.robsfirstapp.model;

// Fürt die DB anbindung
import androidx.room.Entity;
import androidx.room.PrimaryKey;
// -------------------------------------

// Annotation für DB anbindung
@Entity(tableName = "notes")
public class Note {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private String title;
    private String content;

    public Note(String title, String content) {
        this.title = title;
        this.content = content;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

//    public String getTitle() { // Old getTitel() method who works but i try also the new one
//        return title != null && !title.isEmpty() ? title : content.substring(0, Math.min(10, content.length()));
//    }

    // Getter
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void clear() {
        this.title = "";
        this.content = "";
    }
}
