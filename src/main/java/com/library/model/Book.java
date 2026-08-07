package com.library.model;

import java.util.List;

public class Book {
    private int id;
    private String title;
    private Integer authorId;
    private String authorName; // populated by joined queries, not persisted directly
    private List<String> genres;
    private Integer publishedYear;
    private boolean available;

    public Book() {}

    public Book(int id, String title, Integer authorId, List<String> genres,
                Integer publishedYear, boolean available) {
        this.id = id;
        this.title = title;
        this.authorId = authorId;
        this.genres = genres;
        this.publishedYear = publishedYear;
        this.available = available;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public Integer getAuthorId() { return authorId; }
    public void setAuthorId(Integer authorId) { this.authorId = authorId; }

    public String getAuthorName() { return authorName; }
    public void setAuthorName(String authorName) { this.authorName = authorName; }

    public List<String> getGenres() { return genres; }
    public void setGenres(List<String> genres) { this.genres = genres; }

    public Integer getPublishedYear() { return publishedYear; }
    public void setPublishedYear(Integer publishedYear) { this.publishedYear = publishedYear; }

    public boolean isAvailable() { return available; }
    public void setAvailable(boolean available) { this.available = available; }

    @Override
    public String toString() {
        return title + " (id " + id + ")";
    }
}
