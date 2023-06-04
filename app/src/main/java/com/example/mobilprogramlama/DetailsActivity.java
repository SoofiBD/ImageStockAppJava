package com.example.mobilprogramlama;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageDecoder;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;

import com.example.mobilprogramlama.databinding.ActivityDetailsBinding;
import com.google.android.material.snackbar.Snackbar;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;

public class DetailsActivity extends AppCompatActivity {

    private ActivityDetailsBinding binding;
    private ActivityResultLauncher<Intent> activityResultLauncher;
    private ActivityResultLauncher<String> requestPermissionLauncher;
    private Bitmap selectedImage;
    private SQLiteDatabase database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDetailsBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        registerLaunchers();

        database = this.openOrCreateDatabase("Karikatürler", MODE_PRIVATE, null);

        Intent intent = getIntent();
        String info = intent.getStringExtra("info");

        if (info.equals("new")) {
            binding.nameText.setText("");
            binding.ContextText.setText("");
            binding.imageView.setImageResource(R.drawable.image);
            binding.button.setVisibility(View.VISIBLE);
            binding.share.setVisibility(View.INVISIBLE);
        } else {
            int karikatürId = intent.getIntExtra("karikatür", 1);
            binding.button.setVisibility(View.INVISIBLE);
            binding.share.setVisibility(View.VISIBLE);

            try {
                Cursor cursor = database.rawQuery("SELECT * FROM karikatürler WHERE id = ?", new String[]{String.valueOf(karikatürId)});
                int nameIx = cursor.getColumnIndex("name");
                int artistIx = cursor.getColumnIndex("artist");
                int imageIx = cursor.getColumnIndex("image");

                while (cursor.moveToNext()) {
                    binding.nameText.setText(cursor.getString(nameIx));
                    binding.ContextText.setText(cursor.getString(artistIx));

                    byte[] bytes = cursor.getBlob(imageIx);
                    Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                    binding.imageView.setImageBitmap(bitmap);
                }

                cursor.close();
            } catch (Exception e) {
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

            database.execSQL("INSERT INTO karikatürler (name , artist , image) VALUES (? , ? , ?)", new Object[]{name, artist, byteArray});

            String sqlString = "INSERT INTO karikatürler (name , artist , image) VALUES (? , ? , ?)";
            SQLiteStatement sqLiteStatement = database.compileStatement(sqlString);
            sqLiteStatement.bindString(1, name);
            sqLiteStatement.bindString(2, artist);
            sqLiteStatement.bindBlob(3, byteArray);
        } catch (Exception e) {
            e.printStackTrace();
        }

        Snackbar.make(view, "Karikatür kaydedildi", Snackbar.LENGTH_LONG).show();

        Intent intent = new Intent(DetailsActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    private Bitmap smallmaker(Bitmap image, int maximumSize) {
        int width = image.getWidth();
        int height = image.getHeight();

        float bitmapRatio = (float) width / (float) height;
        if (bitmapRatio > 1) {
            width = maximumSize;
            height = (int) (width / bitmapRatio);
        } else {
            height = maximumSize;
            width = (int) (height * bitmapRatio);
        }

        return Bitmap.createScaledBitmap(image, width, height, true);
    }

    public void selectImage(View view) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                Snackbar.make(view, "Galeriye erişim izni gerekiyor", Snackbar.LENGTH_INDEFINITE).setAction("İzin Ver", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        requestPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
                    }
                }).show();
            } else {
                requestPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
            }
        } else {
            Intent intentToGallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            activityResultLauncher.launch(intentToGallery);
        }
    }

    private void registerLaunchers() {
        activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri imageData = result.getData().getData();

                    try {
                        if (Build.VERSION.SDK_INT >= 28) {
                            ImageDecoder.Source source = ImageDecoder.createSource(DetailsActivity.this.getContentResolver(), imageData);
                            selectedImage = ImageDecoder.decodeBitmap(source);
                        } else {
                            selectedImage = MediaStore.Images.Media.getBitmap(DetailsActivity.this.getContentResolver(), imageData);
                        }

                        binding.imageView.setImageBitmap(selectedImage);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        requestPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), new ActivityResultCallback<Boolean>() {
            @Override
            public void onActivityResult(Boolean isGranted) {
                if (isGranted) {
                    Intent intentToGallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    activityResultLauncher.launch(intentToGallery);
                }
            }
        });
    }

    public void shareImage(View view) {
        BitmapDrawable bitmapDrawable = (BitmapDrawable) binding.imageView.getDrawable();
        Bitmap bitmap = bitmapDrawable.getBitmap();

        try {
            File cachePath = new File(getCacheDir(), "images");
            cachePath.mkdirs(); // Klasörü oluştur

            FileOutputStream stream = new FileOutputStream(cachePath + "/image.png"); // Resmi geçici bir dosyaya kaydet
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
            stream.close();

            File imagePath = new File(getCacheDir(), "images");
            File newFile = new File(imagePath, "image.png");
            Uri contentUri = FileProvider.getUriForFile(this, "com.example.mobilprogramlama.fileprovider", newFile);

            if (contentUri != null) {
                Intent shareIntent = new Intent();
                shareIntent.setAction(Intent.ACTION_SEND);
                shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION); // URI izinlerini ekle
                shareIntent.setDataAndType(contentUri, getContentResolver().getType(contentUri));
                shareIntent.putExtra(Intent.EXTRA_STREAM, contentUri); // Resmi paylaşmak için URI'yi ekleyin
                startActivity(Intent.createChooser(shareIntent, "Paylaş"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
