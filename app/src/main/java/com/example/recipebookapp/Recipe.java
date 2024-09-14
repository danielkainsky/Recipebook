package com.example.recipebookapp;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class Recipe {
    private String name;
    private ArrayList<Ingridiant> ingridiants;
    private String steps;
    private  String encodedimage;
    private boolean liked;

    public Recipe(String name, ArrayList<Ingridiant> ingridiants, String steps, String encodedimage) {
        this.name = name;
        this.ingridiants = ingridiants;
        this.steps = steps;
        this.encodedimage = encodedimage;
        this.liked = false;
    }

    public Recipe() {
    }

    public Recipe(String recpieName, JSONArray ingredients, String steps, String imagedata) {
        this.name = recpieName;
        this.steps = steps;
        this.encodedimage = imagedata;
        this.ingridiants = new ArrayList<>();
        try {
            for (int i = 0; i < ingredients.length(); i++) {
                JSONObject ing = (JSONObject) ingredients.get(i);
                this.ingridiants.add(new Ingridiant(ing.getString("ingridiant_name"),ing.getString("ingridiant_count")));
            }
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        this.liked = false;
    }

    public boolean isLiked() {
        return liked;
    }

    public void setLiked(boolean liked) {
        this.liked = liked;
    }


    public Recipe(JSONObject jsonObject){
        try {
            this.name = jsonObject.getString("recpie_name");
            this.steps = jsonObject.getString("steps");
            this.encodedimage = jsonObject.getString("imagedata");
            this.ingridiants = new ArrayList<>();
            for (int i = 0; i < jsonObject.getJSONArray("ingredients").length(); i++) {
                JSONObject ing = (JSONObject) jsonObject.getJSONArray("ingredients").get(i);
                this.ingridiants.add(new Ingridiant(ing.getString("ingridiant_name"),ing.getString("ingridiant_count")));
            }
            if (jsonObject.has("isliked"))
                this.liked = jsonObject.getBoolean("isliked");
            else this.liked = false;
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ArrayList<Ingridiant> getIngridiants() {
        return ingridiants;
    }

    public void setIngridiants(ArrayList<Ingridiant> ingridiants) {
        this.ingridiants = ingridiants;
    }

    public String getSteps() {
        return steps;
    }

    public void setSteps(String steps) {
        this.steps = steps;
    }

    public String getEncodedimage() {
        return encodedimage;
    }

    public void setEncodedimage(String encodedimage) {
        this.encodedimage = encodedimage;
    }

    private StringBuilder IngToString(){
        StringBuilder output = new StringBuilder("");
        Log.d("recipe", String.valueOf(this.ingridiants.size()));
        for (int i = 0; i < this.ingridiants.size(); i++) {
            output.append(this.ingridiants.get(i).toString() + ", ");
            Log.d("recipe", output.toString());
        }
        return output;
    }
    @Override
    public String toString() {
        return
                "ingredients: " + IngToString() + "\n"+ "\n"+ "\n"+
                "steps: " + steps;
    }

    public void onChangeLike(){
        if (this.liked)
            this.liked = false;
        else this.liked = true;
    }
}
