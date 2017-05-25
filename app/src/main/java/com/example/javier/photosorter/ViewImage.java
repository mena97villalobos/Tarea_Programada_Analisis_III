package com.example.javier.photosorter;

import android.net.Uri;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;

import java.io.File;

public class ViewImage extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_image);
        Bundle b = getIntent().getExtras();
        String nombreImagen = "";
        if(b != null)
            nombreImagen = b.getString("img");
        ImageView im = (ImageView)findViewById(R.id.imgView);
        File root = new File(Environment.getExternalStorageDirectory(),"Pictures");
        File archivo = new File(root,nombreImagen);
        Uri imagenUri = Uri.fromFile(archivo);
        im.setImageURI(imagenUri);
    }
}
