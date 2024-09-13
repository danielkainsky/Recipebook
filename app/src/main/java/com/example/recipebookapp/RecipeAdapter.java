package com.example.recipebookapp;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class RecipeAdapter extends  RecyclerView.Adapter<RecipeAdapter.recipeviewholder>{
    private ArrayList<Recipe> recipes;
    private onDeleteRecipeListener listener;

    public RecipeAdapter(ArrayList<Recipe> recipes,onDeleteRecipeListener listener) {
        this.recipes = recipes;
        this.listener = listener;
    }

    public interface onDeleteRecipeListener{
        void onDeleteRecipe(int postion);
    }

    @NonNull
    @Override
    public recipeviewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View recipeview = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycleritem_recipe,parent,false);
        return new recipeviewholder(recipeview);
    }

    @Override
    public void onBindViewHolder(@NonNull recipeviewholder holder, int position) {
        Recipe currentrecipe = this.recipes.get(position);
        int pos = position;
        holder.recipeview.setText(currentrecipe.toString());
        holder.dishimage.setImageBitmap(base64ToBitmap(currentrecipe.getEncodedimage()));
        holder.deletebutton.setOnClickListener(view -> {
                listener.onDeleteRecipe(pos);

        });
    }

    @Override
    public int getItemCount() {
        return this.recipes.size();
    }

    public static Bitmap base64ToBitmap(String base64String) {
        // Decode Base64 string into byte array
        byte[] decodedBytes = Base64.decode(base64String, Base64.DEFAULT);

        // Convert byte array into Bitmap
        return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
    }

    public void clearData() {
        recipes.clear();
        notifyDataSetChanged(); // Notify that the data has changed
    }
    public static class recipeviewholder extends RecyclerView.ViewHolder{
        public TextView recipeview;
        public ImageView dishimage;
        public ImageButton deletebutton;
        public recipeviewholder(@NonNull View itemView) {
            super(itemView);
            this.recipeview = itemView.findViewById(R.id.recipeview);
            this.dishimage  = itemView.findViewById(R.id.dishimage);
            this.deletebutton = itemView.findViewById(R.id.deletebutton);
        }
    }
}
