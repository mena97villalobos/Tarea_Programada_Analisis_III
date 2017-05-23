package com.example.javier.photosorter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by Bryan on 5/22/2017.
 */

public class PixelHash {

    public void principal(){

    }

    public ArrayList<Integer> vectorImagen(Bitmap imagen){
        //imagen = toGrayscale(imagen);
        imagen = resizeImage(imagen);
        imagen = toGrayscale(imagen);
        int w = imagen.getWidth();
        int h = imagen.getHeight();
        //Log.i("xD", "width: "+w+" Height: "+h);
        ArrayList<Integer> pixels = new ArrayList();
        /*for(int i = 0; i<w; i++){
            for(int j = 0; j<h; j++){
                pixels.add(imagen.getPixel(i,j));
                Log.d("Hash", "Hola");
            }
        }*/
        return pixels;
    }
    //TEMPORAL PARA DEBBUG



    public Bitmap toGrayscale(Bitmap bmpOriginal){
        int width, height;
        height = bmpOriginal.getHeight();
        width = bmpOriginal.getWidth();

        Bitmap bmpGrayscale = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(bmpGrayscale);
        Paint paint = new Paint();
        ColorMatrix cm = new ColorMatrix();
        cm.setSaturation(0);
        ColorMatrixColorFilter f = new ColorMatrixColorFilter(cm);
        paint.setColorFilter(f);
        c.drawBitmap(bmpOriginal, 0, 0, paint);
        return bmpGrayscale;
    }

    public Bitmap resizeImage(Bitmap img){
        return Bitmap.createScaledBitmap(img, 255, 255, true);
    }

}
