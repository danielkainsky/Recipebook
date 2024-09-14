package com.example.recipebookapp;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Switch;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class new_recpie extends AppCompatActivity {
    private static final int PICK_IMAGE_REQUEST = 1;
    int SELECT_PICTURE = 200;
    ImageButton imageselector;
    int numofingridants = 0 ;
    String currentbook;
    Switch localsave;
    FirebaseDatabase database;
    ArrayList<Ingridiant> ingridants = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_LTR);
        super.onCreate(savedInstanceState);
        //EdgeToEdge.enable(this);
        setContentView(R.layout.activity_new_recpie);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        imageselector = findViewById(R.id.imageselctor);

        localsave = findViewById(R.id.localsave);

        RecyclerView ingridantview = findViewById(R.id.ingridiants);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        ingridantview.setLayoutManager(layoutManager);

        EditText numberinput = findViewById(R.id.enteringridiantsnum);
        numberinput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Update the TextView when the text changes
                ingridants.clear();
                if (!s.toString().isEmpty()) {
                    numofingridants = Integer.parseInt(s.toString());
                } else {
                    numofingridants =0 ;
                }

                IngridantAdapter ingridantAdapter = new IngridantAdapter(ingridants);
                ingridantview.setAdapter(ingridantAdapter);
                for (int i = 0; i < numofingridants; i++) {
                    ingridants.add(new Ingridiant("",""));
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        currentbook = getIntent().getStringExtra("name_of_json");
        if (currentbook.equals("shared recipes"))
            localsave.setVisibility(View.GONE);

    }
    public void onSubmit(View view){
        EditText recpiename = findViewById(R.id.enterrecpiename);
        EditText howtomake = findViewById(R.id.enterhowtomake);
        ImageButton imageButton = findViewById(R.id.imageselctor);
        Switch serversave = findViewById(R.id.serversave);
        boolean localy = localsave.isChecked();
        boolean server = serversave.isChecked();
        Drawable   drawable = imageButton.getDrawable();
        Bitmap bit = ((BitmapDrawable) drawable).getBitmap();
        String encodedimage = bitmapToBase64(bit);
        JSONObject recipe = new JSONObject();
        try {
            recipe.put("recpie_name",recpiename.getText().toString());
            JSONArray ingredients = new JSONArray();
            for (int i = 0; i < ingridants.size(); i++) {
                JSONObject ing = new JSONObject();
                ing.put("ingridiant_name",ingridants.get(i).getIngridiant_name());
                ing.put("ingridiant_count",ingridants.get(i).getCount());
                ingredients.put(ing);
            }
            recipe.put("ingredients",ingredients);
            recipe.put("steps",howtomake.getText().toString());
            recipe.put("imagedata",encodedimage);
            recipe.put("isliked",false);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        if (localy) {
            saveOrUpdateJsonToFile(currentbook, recipe);
            Log.d("firebase", "local: ");
        }
        if (server){
            database = FirebaseDatabase.getInstance();
            Recipe recipe1 = new Recipe(recpiename.getText().toString(),ingridants,howtomake.getText().toString(),encodedimage);
            Log.d("firebase", "server: ");
            DatabaseReference myRef1 = database.getReference("recipes").push();
            myRef1.setValue(recipe1);
            Intent i = new Intent(this,MainActivity.class);
            startActivity(i);
        }
        else  {Intent i = new Intent(this,MainActivity.class);
        startActivity(i);}
    }
    public void openImageChooser(View view) {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), SELECT_PICTURE);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {

            // compare the resultCode with the
            // SELECT_PICTURE constant
            if (requestCode == SELECT_PICTURE) {
                // Get the url of the image from data
                Uri selectedImageUri = data.getData();
                if (null != selectedImageUri) {
                    // update the preview image in the layout
                    imageselector.setImageURI(selectedImageUri);
                }
            }
        }
    }
    // Function to convert Bitmap to Base64 string
    public String bitmapToBase64(Bitmap bitmap) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);  // Compress to PNG or JPG
        byte[] byteArray = outputStream.toByteArray();
        return Base64.encodeToString(byteArray, Base64.DEFAULT);
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
