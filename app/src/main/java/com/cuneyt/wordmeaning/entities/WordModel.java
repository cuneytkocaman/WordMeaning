package com.cuneyt.wordmeaning.entities;

public class WordModel {
    private String id;
    private String language;
    private String word;
    private String meaning;
    private String description;

    public WordModel() {
    }

    public WordModel(String id, String language) {
        this.id = id;
        this.language = language;
    }

    public WordModel(String id, String word, String meaning, String description) {
        this.id = id;
        this.word = word;
        this.meaning = meaning;
        this.description = description;
    }

    public WordModel(String id, String language, String word, String meaning, String description) {
        this.id = id;
        this.language = language;
        this.word = word;
        this.meaning = meaning;
        this.description = description;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public String getMeaning() {
        return meaning;
    }

    public void setMeaning(String meaning) {
        this.meaning = meaning;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
