package com.example.mobilprogramlama;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;

import com.example.mobilprogramlama.databinding.ActivityDetailsBinding;
import com.google.android.material.snackbar.Snackbar;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.sql.SQLData;

public class DetailsActivity extends AppCompatActivity {

    private ActivityDetailsBinding binding;
    ActivityResultLauncher<Intent> ActivityResultLauncher;
    ActivityResultLauncher<String> requestPermissionLauncher;
    Bitmap selectedImage;
    SQLiteDatabase database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDetailsBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        registerLauncher();

        database = this.openOrCreateDatabase("Karikatürler" , MODE_PRIVATE , null);


        Intent intent = getIntent();
        String info = intent.getStringExtra("info");

        if(info.equals("new")){

            binding.nameText.setText("");
            binding.ContextText.setText("");
            binding.imageView.setImageResource(R.drawable.image);
            binding.button.setVisibility(View.VISIBLE);
        } else {

            int karikatürId = intent.getIntExtra("karikatür",1);
            binding.button.setVisibility(View.INVISIBLE);

            try{

                Cursor cursor = database.rawQuery("SELECT * FROM karikatürler WHERE id = ?",new String[]{String.valueOf(karikatürId)});
                int nameIx = cursor.getColumnIndex("name");
                int artistIx = cursor.getColumnIndex("artist");
                int imageIx = cursor.getColumnIndex("image");

                while (cursor.moveToNext()){
                    binding.nameText.setText(cursor.getString(nameIx));
                    binding.ContextText.setText(cursor.getString(artistIx));

                    byte[] bytes = cursor.getBlob(imageIx);
                    Bitmap bitmap = BitmapFactory.decodeByteArray(bytes,0,bytes.length);
                    binding.imageView.setImageBitmap(bitmap);
                }

                cursor.close();

            }catch (Exception e){
                e.printStackTrace();
            }
        }

    }

    public void save(View view) {
        String name = binding.nameText.getText().toString();
        String artist = binding.ContextText.getText().toString();

        Bitmap smallImage = smallmaker(selectedImage, 300);


        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        smallImage.compress(Bitmap.CompressFormat.PNG, 50, outputStream);
        byte[] byteArray = outputStream.toByteArray();

        try {
            database.execSQL("CREATE TABLE IF NOT EXISTS karikatürler (id INTEGER PRIMARY KEY , name VARCHAR , artist VARCHAR , image BLOB)");

            database.execSQL("INSERT INTO karikatürler (name , artist , image) VALUES (? , ? , ?)" , new Object[]{name , artist , byteArray});

            String sqlString = "INSERT INTO karikatürler (name , artist , image) VALUES (? , ? , ?)";
            SQLiteStatement sqLiteStatement = database.compileStatement(sqlString);
            sqLiteStatement.bindString(1 , name);
            sqLiteStatement.bindString(2 , artist);
            sqLiteStatement.bindBlob(3 , byteArray);



        } catch (Exception e) {
            e.printStackTrace();
        }

        Intent intent = new Intent(DetailsActivity.this , MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);

    }

    public Bitmap smallmaker(Bitmap image , int maximumSize){
        int width = image.getWidth();
        int height = image.getHeight();

        float Ratio = (float)width/ (float)height;

        if (Ratio > 1){
            width = maximumSize;
            height = (int)(width/Ratio);
        } else {
            height = maximumSize;
            width = (int)(height * Ratio);
        }

        return image.createScaledBitmap(image, 100,100,true);
    }

    public void selectImage (View view){

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) {
                if(ActivityCompat.shouldShowRequestPermissionRationale(this , Manifest.permission.READ_MEDIA_IMAGES)){
                    Snackbar.make(view,"permission needed" , Snackbar.LENGTH_INDEFINITE).setAction("Give Permission", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            requestPermissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES);
                        }
                    }).show();
                } else {
                    requestPermissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES);
                }
            } else {
                Intent intentToGallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                ActivityResultLauncher.launch(intentToGallery);
            }
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                if(ActivityCompat.shouldShowRequestPermissionRationale(this , Manifest.permission.READ_EXTERNAL_STORAGE)){
                    Snackbar.make(view,"permission needed" , Snackbar.LENGTH_INDEFINITE).setAction("Give Permission", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            requestPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
                        }
                    }).show();
                } else {
                    requestPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
                }
            } else {
                Intent intentToGallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                ActivityResultLauncher.launch(intentToGallery);
            }
        }


    }

    public void registerLauncher()  {

        ActivityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {
                if (result.getResultCode() == RESULT_OK){
                    Intent intentFromResult = result.getData();
                    if (intentFromResult != null) {
                        Uri imageData = intentFromResult.getData();
                        //binding.imageView.setImageURI(imageData);
                        try{
                            if (Build.VERSION.SDK_INT >= 28) {
                                ImageDecoder.Source source = ImageDecoder.createSource(DetailsActivity.this.getContentResolver(),imageData);
                                selectedImage = ImageDecoder.decodeBitmap(source);
                                binding.imageView.setImageBitmap(selectedImage);
                                //Bitmap selectedImage = BitmapFactory.decodeStream(DetailsActivity.this.getContentResolver().openInputStream(imageData));
                            } else {
                                selectedImage = MediaStore.Images.Media.getBitmap(DetailsActivity.this.getContentResolver(),imageData);
                                binding.imageView.setImageBitmap(selectedImage);
                            }

                        }
                        catch (Exception e){
                            e.printStackTrace();
                        }
                    }
                }
            }
        });



        requestPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), new ActivityResultCallback<Boolean>() {
            @Override
            public void onActivityResult(Boolean result) {
                if (result){
                   Intent intentToGallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    ActivityResultLauncher.launch(intentToGallery);
                } else {
                    Snackbar.make(binding.getRoot(),"permission needed" , Snackbar.LENGTH_INDEFINITE).setAction("Give Permission", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            requestPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
                        }
                    }).show();
                }
            }
        });
    }


}

