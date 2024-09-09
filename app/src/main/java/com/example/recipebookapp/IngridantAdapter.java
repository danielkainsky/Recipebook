package com.example.recipebookapp;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class IngridantAdapter extends RecyclerView.Adapter<IngridantAdapter.ingridantviewholder> {
    private ArrayList<Ingridiant> ingridiants;

    public IngridantAdapter(ArrayList<Ingridiant> ingridiants) {
        this.ingridiants = ingridiants;
    }

    @NonNull
    @Override
    public ingridantviewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View ingridantView = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycleritem_ingrident,parent,false);
        return new ingridantviewholder(ingridantView);
    }

    @Override
    public void onBindViewHolder(@NonNull ingridantviewholder holder, int position) {
        int pos  = position;
        Ingridiant currentingridant = ingridiants.get(position);
        holder.ingridantname.setText(currentingridant.getIngridiant_name());
        holder.ingridantcount.setText(currentingridant.getCount());

        holder.ingridantname.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                currentingridant.setIngridiant_name(charSequence.toString());
                ingridiants.set(pos,currentingridant);
                Log.d("TAG", "onTextChanged: "+currentingridant.getIngridiant_name());
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        holder.ingridantcount.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                currentingridant.setCount(charSequence.toString());
                ingridiants.set(pos,currentingridant);
                Log.d("TAG", "onTextChanged: "+currentingridant.getCount());
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }

    @Override
    public int getItemCount() {
        return ingridiants.size();
    }

    public static class ingridantviewholder extends RecyclerView.ViewHolder{
        public EditText ingridantname;
        public EditText ingridantcount;
        public ingridantviewholder(@NonNull View itemView) {
            super(itemView);
            ingridantname = itemView.findViewById(R.id.ingritantname);
            ingridantcount = itemView.findViewById(R.id.ingridiantcount);
        }
    }
}
