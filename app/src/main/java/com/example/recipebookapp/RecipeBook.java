package com.example.recipebookapp;

import org.json.JSONObject;

public class RecipeBook {
    private String name;
    private JSONObject json;

    public RecipeBook(String name, JSONObject json) {
        this.name = name;
        this.json = json;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public JSONObject getJson() {
        return json;
    }

    public void setJson(JSONObject json) {
        this.json = json;
    }
}
