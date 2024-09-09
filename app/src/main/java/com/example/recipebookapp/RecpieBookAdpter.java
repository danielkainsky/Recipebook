package com.example.recipebookapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class RecpieBookAdpter extends RecyclerView.Adapter<RecpieBookAdpter.recpiebookviewholder>{
    private ArrayList<RecipeBook> books;
    private OnItemClickListener listener;

    public RecpieBookAdpter(ArrayList<RecipeBook> books, OnItemClickListener listener) {
        this.books = books;
        this.listener = listener;
    }

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    @NonNull
    @Override
    public recpiebookviewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View recpiebookview = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycleritem_book,parent,false);
        return new recpiebookviewholder(recpiebookview);
    }

    @Override
    public void onBindViewHolder(@NonNull recpiebookviewholder holder, int position) {
        RecipeBook currnetrecpiebook = books.get(position);
        holder.nameofbook.setText(currnetrecpiebook.getName());

        holder.itemView.setOnClickListener(v ->{
            if (listener !=null)
                listener.onItemClick(position);
        });
    }

    @Override
    public int getItemCount() {
        return books.size();
    }

    public static  class  recpiebookviewholder extends RecyclerView.ViewHolder{
        public TextView nameofbook;
        public recpiebookviewholder(@NonNull View itemView) {
            super(itemView);
            nameofbook = itemView.findViewById(R.id.nameofbook);
        }
    }
}
