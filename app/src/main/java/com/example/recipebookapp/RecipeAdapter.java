package com.example.recipebookapp;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
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
    private onLikeClickedListener likelistener;
    private Context context;
    private boolean isliked = false;
    public RecipeAdapter(ArrayList<Recipe> recipes,onDeleteRecipeListener listener,onLikeClickedListener likelistener, Context context) {
        this.recipes = recipes;
        this.listener = listener;
        this.likelistener = likelistener;
        this.context = context;
    }

    public interface onDeleteRecipeListener{
        void onDeleteRecipe(int postion);
    }

    public interface onLikeClickedListener{
        void onLikeClicked(int postion);
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
        isliked = currentrecipe.isLiked();
        int pos = position;
        holder.nameofrecipe.setText(currentrecipe.getName());
        holder.recipeview.setText(currentrecipe.toString());
        holder.dishimage.setImageBitmap(base64ToBitmap(currentrecipe.getEncodedimage()));
        holder.deletebutton.setOnClickListener(view -> {
                listener.onDeleteRecipe(pos);

        });
        holder.changeimage(isliked);
        holder.likebutton.setOnClickListener(view -> {

            Drawable notliked = context.getResources().getDrawable(android.R.drawable.btn_star_big_off);

            if (((BitmapDrawable)holder.likebutton.getDrawable()).getBitmap().equals(drawableToBitmap(notliked)))
                isliked = true;
            else isliked = false;
            holder.changeimage(isliked);
            likelistener.onLikeClicked(pos);
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

    public Bitmap drawableToBitmap(Drawable drawable) {
        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable) drawable).getBitmap();
        }

        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(),
                drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        return bitmap;
    }

    public static class recipeviewholder extends RecyclerView.ViewHolder{
        public TextView recipeview;
        public ImageView dishimage;
        public ImageButton deletebutton;
        public ImageButton likebutton;
        public TextView nameofrecipe;
        public recipeviewholder(@NonNull View itemView) {
            super(itemView);
            this.recipeview = itemView.findViewById(R.id.recipeview);
            this.dishimage  = itemView.findViewById(R.id.dishimage);
            this.deletebutton = itemView.findViewById(R.id.deletebutton);
            this.likebutton = itemView.findViewById(R.id.likebutton);
            this.nameofrecipe = itemView.findViewById(R.id.nameofrecipe);
        }
        public void changeimage(boolean isliked){
            if (isliked)
                this.likebutton.setImageResource(android.R.drawable.btn_star_big_on);
            else this.likebutton.setImageResource(android.R.drawable.btn_star_big_off);
        }
    }
}
