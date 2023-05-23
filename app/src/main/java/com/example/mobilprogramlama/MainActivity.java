package com.example.mobilprogramlama;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.example.mobilprogramlama.databinding.ActivityMainBinding;

import java.util.ArrayList;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    ArrayList<Karikatür> karikatürArrayList;
    AppAdapter appAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        karikatürArrayList = new ArrayList<>();
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        appAdapter = new AppAdapter(karikatürArrayList);
        binding.recyclerView.setAdapter(appAdapter);

        getData();
    }

    private void getData(){
        try {
            SQLiteDatabase database = this.openOrCreateDatabase("Karikatürler",MODE_PRIVATE,null);

            Cursor cursor = database.rawQuery("SELECT * FROM karikatürler",null);
            int nameIx = cursor.getColumnIndex("name");
            int idIx = cursor.getColumnIndex("id");

            while (cursor.moveToNext()) {
                String nameFromDatabase = cursor.getString(nameIx);
                int idFromDatabase = cursor.getInt(idIx);
                Karikatür karikatür = new Karikatür(nameFromDatabase,idFromDatabase);
                karikatürArrayList.add(karikatür);
            }
            appAdapter.notifyDataSetChanged();
            cursor.close();
        }catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void addImage(View view) {
        Intent intent = new Intent(MainActivity.this, DetailsActivity.class);
        intent.putExtra("info","new");
        startActivity(intent);
    }


    public void showRandomImage(View view) {
        if (karikatürArrayList.isEmpty()) {
            Toast.makeText(this, "Liste boş.", Toast.LENGTH_SHORT).show();
            return;
        }else{
            int randomIndex = new Random().nextInt(karikatürArrayList.size());
            Karikatür randomKarikatür = karikatürArrayList.get(randomIndex);

            Intent intent = new Intent(MainActivity.this, DetailsActivity.class);
            intent.putExtra("info", "old");
            intent.putExtra("karikatür", randomKarikatür.getId());

            startActivity(intent);

        }


    }

    /*public void showRandomImage(View view) {
        if (karikatürArrayList.isEmpty()) {
            Toast.makeText(this, "Liste boş.", Toast.LENGTH_SHORT).show();
            return;
        }

        int randomIndex = new Random().nextInt(karikatürArrayList.size());
        Karikatür randomKarikatür = karikatürArrayList.get(randomIndex);

        Intent intent = new Intent(MainActivity.this, DetailsActivity.class);
        intent.putExtra("info", "existing");
        intent.putExtra("karikaturId", randomKarikatür.getId());

        startActivity(intent);
    }*/


}