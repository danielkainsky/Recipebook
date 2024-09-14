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
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

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

public class MainActivity extends AppCompatActivity implements RecpieBookAdpter.OnItemClickListener, RecipeAdapter.onDeleteRecipeListener, RecipeAdapter.onLikeClickedListener {
    JSONObject jsonObject;
    String currentbook;
    ArrayList<RecipeBook> books;
    Button addnewrcipe;
    RecyclerView recipes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_LTR);
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
                        saveJsonToFile(input, jsonObject);
                        alert.dismiss();
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
    boolean hasshared = false;
    AlertDialog alert;
    public void onOpenBooks(View view) {
        LayoutInflater inflater = getLayoutInflater();
        View manudialogView = inflater.inflate(R.layout.booksmanu_dialog, null);

        books = loadAllJsonObjects(MainActivity.this);
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
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
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(MainActivity.this);
        bookrecycler.setLayoutManager(layoutManager);


        RecpieBookAdpter recpieBookAdpter = new RecpieBookAdpter(books, MainActivity.this);
        bookrecycler.setAdapter(recpieBookAdpter);

        getSharedRecipes(new FirebaseCallback() {
            @Override
            public void onCallback(JSONObject result) {
                if (result != null && !hasshared) {
                    for (int i = 0; i < books.size(); i++)
                        if (books.get(i).getName().equals("shared recipes"))
                            books.remove(i);

                    books.add(new RecipeBook("shared recipes", result));
                    hasshared = true;
                }
                RecpieBookAdpter recpieBookAdpter = new RecpieBookAdpter(books, MainActivity.this);
                bookrecycler.setAdapter(recpieBookAdpter);
            }
        });

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

    RecipeAdapter recipesAdapter;
    ArrayList<Recipe> recipes1;
    // Implement the onItemClick method from the interface
    @Override
    public void onItemClick(int position) {
        // Handle the item click event
        alert.dismiss();
        currentbook = books.get(position).getName();
        JSONObject book = books.get(position).getJson();
        TextView namebook = findViewById(R.id.namebook);
        namebook.setText(currentbook);

        recipes1 = new ArrayList<>();
        try {
            if (book.optJSONArray(currentbook) != null && book.has(currentbook)) {

                if (book.getJSONArray(currentbook).length() != 0) {
                    for (int i = 0; i < book.getJSONArray(currentbook).length(); i++) {

                        JSONObject jsonObject1 = book.getJSONArray(currentbook).getJSONObject(i);
                        recipes1.add(new Recipe(jsonObject1));

                    }
                    recipes.setVisibility(View.VISIBLE);
                    LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
                    recipes.setLayoutManager(layoutManager);
                    recipesAdapter = new RecipeAdapter(recipes1,MainActivity.this,MainActivity.this,MainActivity.this);
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

    public interface FirebaseCallback {
        void onCallback(JSONObject result);
    }


    DatabaseReference databaseReference;
    public void getSharedRecipes(FirebaseCallback firebaseCallback) {
        databaseReference = FirebaseDatabase.getInstance().getReference("recipes");
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                JSONObject sharedrecipes = new JSONObject();
                try {
                    JSONArray sharedRecipesArray = new JSONArray();

                    // Iterate through the recipes node in Firebase
                    for (DataSnapshot recipeSnapshot : dataSnapshot.getChildren()) {
                        // Add this recipe object to the sharedRecipes array
                        Recipe recipe = recipeSnapshot.getValue(Recipe.class);
                        Log.d("firebase", recipe.toString());
                        JSONObject recipejson = RecipetoJson(recipe);
                        sharedRecipesArray.put(recipejson);
                    }

                    // Add the sharedRecipes array to the main JSONObject
                    sharedrecipes.put("shared recipes", sharedRecipesArray);

                    hasshared = false;
                    firebaseCallback.onCallback(sharedrecipes);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle possible errors.
                System.err.println("Error: " + databaseError.getMessage());
            }
        });
    }

    public JSONObject RecipetoJson(Recipe recipe){
        JSONObject recipejson = new JSONObject();
        try {
            recipejson.put("recpie_name", recipe.getName());
            JSONArray ingredients = new JSONArray();
            for (int i = 0; i < recipe.getIngridiants().size(); i++) {
                JSONObject ing = new JSONObject();
                ing.put("ingridiant_name", recipe.getIngridiants().get(i).getIngridiant_name());
                ing.put("ingridiant_count", recipe.getIngridiants().get(i).getCount());
                ingredients.put(ing);
            }
            recipejson.put("ingredients", ingredients);
            recipejson.put("steps", recipe.getSteps());
            recipejson.put("imagedata", recipe.getEncodedimage());
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        return recipejson;
    }

    @Override
    public void onDeleteRecipe(int postion){
        if (currentbook.equals("shared recipes")){
            Toast.makeText(this, "You can't delete a shared recipe, genius.", Toast.LENGTH_LONG).show();
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        // Set the message and title
        builder.setMessage("Are you sure you want to delete this recipe?")
                .setTitle("Delete Recipe");

        // Set the positive button for "Yes" to confirm deletion
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // Perform the deletion action here
                File file = new File(MainActivity.this.getFilesDir(), currentbook);
                StringBuilder jsonStringBuilder = new StringBuilder();

                try {
                    // Read existing file content
                    if (file.exists()) {
                        FileReader fileReader = new FileReader(file);
                        BufferedReader bufferedReader = new BufferedReader(fileReader);
                        String line;
                        while ((line = bufferedReader.readLine()) != null) {
                            jsonStringBuilder.append(line);
                        }
                        bufferedReader.close();
                    }

                    // Combine existing content with new JSON object
                    String existingJsonString = jsonStringBuilder.toString();
                    JSONObject existingJsonObject;
                    existingJsonObject = new JSONObject(existingJsonString);

                    // Get or create the JSONArray associated with the given key
                    JSONArray jsonArray;
                    jsonArray = existingJsonObject.getJSONArray(currentbook);

                    // Add the new JSON object to the array
                    jsonArray.remove(postion);
                    recipes1.remove(postion);
                    // Update the JSON object with the modified array
                    existingJsonObject.put(currentbook, jsonArray);

                    // Write the updated JSON object back to the file
                    FileWriter fileWriter = new FileWriter(file);
                    BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
                    bufferedWriter.write(existingJsonObject.toString());
                    bufferedWriter.close();

                } catch (IOException | JSONException e) {
                    e.printStackTrace();
                }

            }
        });

        // Set the negative button for "No" to cancel the action
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // Dismiss the dialog
                dialog.dismiss();
            }
        });

        // Create and show the AlertDialog
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    @Override
    public void onLikeClicked(int postion){
        recipes1.get(postion).onChangeLike();
        if (recipes1.get(postion).isLiked()){
            //put into the favorite book
            saveOrUpdateJsonToFile("favorites", RecipetoJson(recipes1.get(postion)));
        }
        else {
            //remove from the favorite book
            File file = new File(MainActivity.this.getFilesDir(), "favorites");
            StringBuilder jsonStringBuilder = new StringBuilder();

            try {
                // Read existing file content
                if (file.exists()) {
                    FileReader fileReader = new FileReader(file);
                    BufferedReader bufferedReader = new BufferedReader(fileReader);
                    String line;
                    while ((line = bufferedReader.readLine()) != null) {
                        jsonStringBuilder.append(line);
                    }
                    bufferedReader.close();
                }

                // Combine existing content with new JSON object
                String existingJsonString = jsonStringBuilder.toString();
                JSONObject existingJsonObject;
                existingJsonObject = new JSONObject(existingJsonString);

                // Get or create the JSONArray associated with the given key
                JSONArray jsonArray;
                jsonArray = existingJsonObject.getJSONArray("favorites");

                // Add the new JSON object to the array
                jsonArray.remove(postion);
                // Update the JSON object with the modified array
                existingJsonObject.put("favorites", jsonArray);

                // Write the updated JSON object back to the file
                FileWriter fileWriter = new FileWriter(file);
                BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
                bufferedWriter.write(existingJsonObject.toString());
                bufferedWriter.close();

            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }

        }

    }

    ArrayList<Recipe> searchresult = new ArrayList<>();
    public void Search(View view){
        searchresult.clear();
        LayoutInflater inflater = getLayoutInflater();
        View manudialogView = inflater.inflate(R.layout.searchbox, null);

        books = loadAllJsonObjects(MainActivity.this);
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setView(manudialogView)
                .setTitle("search for...")
                .setPositiveButton("Submit", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        EditText editTextInput = manudialogView.findViewById(R.id.searchedittext);
                        Switch ingredientswitch = manudialogView.findViewById(R.id.ingredientswitch);
                        Switch recipeswitch = manudialogView.findViewById(R.id.recipeswitch);
                        String input = editTextInput.getText().toString();
                        try {
                            if (ingredientswitch.isChecked() && !recipeswitch.isChecked()) {
                                for (int i = 0; i < books.size(); i++) {
                                    if (books.get(i).getJson().has(books.get(i).getName())){
                                        JSONArray searchedbook = books.get(i).getJson().getJSONArray(books.get(i).getName());
                                        if (searchedbook != null) {
                                            for (int j = 0; j < searchedbook.length(); j++) {
                                                JSONObject recipeObject = searchedbook.optJSONObject(j); // Use optJSONObject to avoid exceptions
                                                if (recipeObject != null) {
                                                    JSONArray ingredientsArray = recipeObject.optJSONArray("ingredients");
                                                    if (ingredientsArray != null) {
                                                        for (int k = 0; k < ingredientsArray.length(); k++) {
                                                            JSONObject ingredientObject = ingredientsArray.optJSONObject(k); // Handle potential nulls
                                                            if (ingredientObject != null) {
                                                                String ingredientName = ingredientObject.optString("ingridiant_name");
                                                                if (ingredientName != null && ingredientName.equals(input)) {
                                                                    searchresult.add(new Recipe(recipeObject));
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            if (recipeswitch.isChecked()) {
                                for (int i = 0; i < books.size(); i++) {
                                    if (books.get(i).getJson().has(books.get(i).getName())) {
                                        JSONArray searchedbook = books.get(i).getJson().optJSONArray(books.get(i).getName());
                                        if (searchedbook != null) {
                                            for (int j = 0; j < searchedbook.length(); j++) {
                                                JSONObject recipeObject = searchedbook.optJSONObject(j);
                                                if (recipeObject != null) {
                                                    String recipeName = recipeObject.optString("recpie_name", null);
                                                    if (recipeName != null && recipeName.toLowerCase().contains(input.toLowerCase())) {
                                                        searchresult.add(new Recipe(recipeObject));
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        }

                        recipes.setVisibility(View.VISIBLE);
                        LinearLayoutManager layoutManager = new LinearLayoutManager(MainActivity.this, LinearLayoutManager.HORIZONTAL, false);
                        recipes.setLayoutManager(layoutManager);
                        RecipeAdapter recipesAdapter = new RecipeAdapter(searchresult,MainActivity.this,MainActivity.this,MainActivity.this);
                        recipes.setAdapter(recipesAdapter);

                        alert.dismiss();
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

        alert = builder.create();
        alert.show();

    }

    public void saveOrUpdateJsonToFile(String fileName, JSONObject newJsonObject) {
        File file = new File(this.getFilesDir(), fileName);
        StringBuilder jsonStringBuilder = new StringBuilder();

        try {
            // Read existing file content
            if (file.exists()) {
                FileReader fileReader = new FileReader(file);
                BufferedReader bufferedReader = new BufferedReader(fileReader);
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    jsonStringBuilder.append(line);
                }
                bufferedReader.close();
            }

            // Combine existing content with new JSON object
            String existingJsonString = jsonStringBuilder.toString();
            JSONObject existingJsonObject;
            if (!existingJsonString.isEmpty()) {
                existingJsonObject = new JSONObject(existingJsonString);
            } else {
                existingJsonObject = new JSONObject();
            }

            // Get or create the JSONArray associated with the given key
            JSONArray jsonArray;
            if (existingJsonObject.has(fileName)) {
                jsonArray = existingJsonObject.getJSONArray(fileName);
            } else {
                jsonArray = new JSONArray();
            }

            // Add the new JSON object to the array
            jsonArray.put(newJsonObject);

            // Update the JSON object with the modified array
            existingJsonObject.put(fileName, jsonArray);

            // Write the updated JSON object back to the file
            FileWriter fileWriter = new FileWriter(file);
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
            bufferedWriter.write(existingJsonObject.toString());
            bufferedWriter.close();

        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
    }
}