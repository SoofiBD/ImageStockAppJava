package com.example.mobilprogramlama;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mobilprogramlama.databinding.RecyclerRowBinding;

import java.util.ArrayList;

public class AppAdapter extends RecyclerView.Adapter<AppAdapter.AppHolder> {

    ArrayList<Karikatür> karikatürArrayList;

    public AppAdapter(ArrayList<Karikatür> karikatürArrayList) {
        this.karikatürArrayList = karikatürArrayList;
    }

    @NonNull
    @Override
    public AppHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        RecyclerRowBinding recyclerRowBinding = RecyclerRowBinding.inflate(LayoutInflater.from(parent.getContext()),parent,false);
        return new AppHolder(recyclerRowBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull AppHolder holder, @SuppressLint("RecyclerView") int position) {
        holder.binding.rowText.setText(karikatürArrayList.get(position).name);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(holder.itemView.getContext(), DetailsActivity.class);
                intent.putExtra("karikatür",karikatürArrayList.get(position).id);
                intent.putExtra("info","old");
                holder.itemView.getContext().startActivity(intent);

                //startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return karikatürArrayList.size();
    }

    public class AppHolder extends RecyclerView.ViewHolder {

        private RecyclerRowBinding binding;

        public AppHolder(RecyclerRowBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

}
