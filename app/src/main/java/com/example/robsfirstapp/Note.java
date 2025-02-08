package com.example.robsfirstapp;

public class Note {
    private int id;
    private String title;
    private String content;

    public Note(int id, String title, String content) {
        this.id = id;
        this.title = title;
        this.content = content;
    }

    public int getId() { return id; }

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

    // Methode to delete (optional for now)
    public void clear() {
        this.id = 0; // unsure what to do with this or if i need this
        this.title = "";
        this.content = "";
    }
}
