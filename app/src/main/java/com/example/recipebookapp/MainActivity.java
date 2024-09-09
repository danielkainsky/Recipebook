package com.example.recipebookapp;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements RecpieBookAdpter.OnItemClickListener {
    JSONObject jsonObject;
    String currentbook;
    ArrayList<RecipeBook> books;
    Button addnewrcipe;
    RecyclerView recipes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        addnewrcipe = findViewById(R.id.newrecpiebutton);
        addnewrcipe.setVisibility(View.GONE);
        recipes = findViewById(R.id.reciperecyclerview);
        recipes.setVisibility(View.GONE);

    }

    public void gotonewactivity(View view) {
        Intent i = new Intent(this, new_recpie.class);
        if (!currentbook.isEmpty())
            i.putExtra("name_of_json", currentbook);
        startActivity(i);
    }

    public void onNewBook(View view) {
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.newbookrecpie, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogView)
                .setTitle("Create a new book")
                .setPositiveButton("Submit", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        EditText editTextInput = dialogView.findViewById(R.id.editbookname);
                        String input = editTextInput.getText().toString();

                        jsonObject = new JSONObject();
                        try {
                            JSONArray jsonArray = new JSONArray();
                            jsonArray.put(null);
                            jsonObject.put(input, jsonArray);
                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        }
                        saveJsonToFile(input, jsonObject);
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

        AlertDialog alert2 = builder.create();
        alert2.show();

    }
    AlertDialog alert;
    public void onOpenBooks(View view) {
        LayoutInflater inflater = getLayoutInflater();
        View manudialogView = inflater.inflate(R.layout.booksmanu_dialog, null);

        books = loadAllJsonObjects(this);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(manudialogView)
                .setTitle("chose your book")
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

        alert = builder.create();
        alert.show();

        RecyclerView bookrecycler = manudialogView.findViewById(R.id.booksrecyclearview);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        bookrecycler.setLayoutManager(layoutManager);


        RecpieBookAdpter recpieBookAdpter = new RecpieBookAdpter(books, this);
        bookrecycler.setAdapter(recpieBookAdpter);

    }

    public void saveJsonToFile(String fileName, JSONObject jsonObject) {
        String jsonString = jsonObject.toString(); // Convert JSONObject to String
        try {
            // Define the File Path and its Name
            File file = new File(this.getFilesDir(), fileName);
            FileWriter fileWriter = new FileWriter(file, true);
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
            bufferedWriter.write(jsonString);
            bufferedWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Read JSON data from internal storage
    public static String readJsonFromInternalStorage(Context context, String fileName) {
        String response = "";
        try {
            File file = new File(context.getFilesDir(), fileName);
            FileReader fileReader = new FileReader(file);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            StringBuilder stringBuilder = new StringBuilder();
            String line = bufferedReader.readLine();
            while (line != null) {
                stringBuilder.append(line).append("\n");
                line = bufferedReader.readLine();
            }
            bufferedReader.close(); // The response will have JSON Format String
            response = stringBuilder.toString();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return response;
    }

    public static ArrayList<RecipeBook> loadAllJsonObjects(Context context) {
        ArrayList<RecipeBook> books = new ArrayList<>();

        // List all files in internal storage
        String[] fileNames = context.fileList();

        // Iterate over each file name
        for (String fileName : fileNames) {
            /// Check if the JSON string represents a JSONArray or JSONObject
            String jsonString = readJsonFromInternalStorage(context, fileName);
            try {
                if (jsonString.startsWith("[")) {
                    // Parse the JSON data as a JSONArray
                    JSONArray jsonArray = new JSONArray(jsonString);
                    // Iterate over the JSONArray and add each JSONObject to the books list
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                        books.add(new RecipeBook(fileName, jsonObject));
                    }
                } else {
                    // If it's a JSONObject, parse it as such
                    JSONObject jsonObject = new JSONObject(jsonString);
                    books.add(new RecipeBook(fileName, jsonObject));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return books;
    }
    // Implement the onItemClick method from the interface
    @Override
    public void onItemClick(int position) {
        // Handle the item click event
        alert.dismiss();
        currentbook = books.get(position).getName();
        JSONObject book = books.get(position).getJson();

        ArrayList<Recipe> recipes1 = new ArrayList<>();
        try {
            if (book.has(currentbook)) {
                Log.d("recipe", String.valueOf(book.getJSONArray(currentbook).get(0)));
                if (book.getJSONArray(currentbook).length() != 0) {
                    for (int i = 0; i < book.getJSONArray(currentbook).length(); i++) {

                        JSONObject jsonObject1 = book.getJSONArray(currentbook).getJSONObject(i);
                        recipes1.add(new Recipe(jsonObject1.getString("recpie_name"), jsonObject1.getJSONArray("ingredients"), jsonObject1.getString("steps"), jsonObject1.getString("imagedata")));
                    }
                    recipes.setVisibility(View.VISIBLE);
                    LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
                    recipes.setLayoutManager(layoutManager);
                    RecipeAdapter recipesAdapter = new RecipeAdapter(recipes1);
                    recipes.setAdapter(recipesAdapter);

                }
            }
            else {
                Log.d("recipe", "no recipe :( ");
                recipes.setVisibility(View.GONE);

            }
        } catch (JSONException e) {throw new RuntimeException(e);
        }

        addnewrcipe.setVisibility(View.VISIBLE);

    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {
        super.onPointerCaptureChanged(hasCapture);
    }
}