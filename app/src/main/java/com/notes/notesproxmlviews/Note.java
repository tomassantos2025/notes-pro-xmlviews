package com.notes.notesproxmlviews;

import com.google.firebase.Timestamp;

public class Note {
    private String title;
    private String content;
    private Timestamp timestamp;
    private String imageUrl;
    private transient String docId;

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public Timestamp getTimestamp() { return timestamp; }
    public void setTimestamp(Timestamp timestamp) { this.timestamp = timestamp; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public String getDocId() { return docId; }
    public void setDocId(String docId) { this.docId = docId; }
}