package com.library.model;

public class Author {
    private int id;
    private String name;
    private String nationality;
    private Integer birthYear;
    private Integer deathYear;

    public Author() {}

    public Author(int id, String name, String nationality, Integer birthYear, Integer deathYear) {
        this.id = id;
        this.name = name;
        this.nationality = nationality;
        this.birthYear = birthYear;
        this.deathYear = deathYear;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getNationality() { return nationality; }
    public void setNationality(String nationality) { this.nationality = nationality; }

    public Integer getBirthYear() { return birthYear; }
    public void setBirthYear(Integer birthYear) { this.birthYear = birthYear; }

    public Integer getDeathYear() { return deathYear; }
    public void setDeathYear(Integer deathYear) { this.deathYear = deathYear; }

    @Override
    public String toString() {
        return name + " (id " + id + ")";
    }
}
