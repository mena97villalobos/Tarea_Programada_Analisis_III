package com.example.javier.photosorter;



import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;
import java.lang.String;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Created by Bryan on 5/22/2017.
 */

public class PixelHash {

    public ArrayList<ArrayList<Integer>> hiperplanos = generarHiperplanos();

    public ArrayList<Integer> vectorImagen(Bitmap imagen){
        imagen = resizeImage(imagen);
        imagen = toGrayscale(imagen);
        int w = imagen.getWidth();
        int h = imagen.getHeight();
        ArrayList<Integer> pixels = new ArrayList();
        /*for(int i = 0; i<w; i++){
            for(int j = 0; j<h; j++){
                pixels.add(imagen.getPixel(i,j));
                Log.d("Hash", "Hola");
            }
        }*/
        return pixels;
    }

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
        return Bitmap.createScaledBitmap(img, 256, 256, true);
    }

    public ArrayList<ArrayList<Integer>> generarHiperplanos(){
        ArrayList<ArrayList<Integer>> arregloFinal = new ArrayList<>();
        Random rand = new Random();
        int contador = 0;
        while(contador<10){
            ArrayList<Integer> hiperPlanoActual = new ArrayList<>();
            for(int i=0;i<65536;i++){
                hiperPlanoActual.add(rand.nextInt(513)-256);
            }
            contador++;
            arregloFinal.add(hiperPlanoActual);
        }
        return arregloFinal;
    }

    public String hashImagen(int [] pixeles){
        String hashFinal = "";
        long suma = 0;//, mediaHP = 0, mediaImg = 0;
        for(int i =0;i<pixeles.length;i++){
            pixeles[i] = Math.abs(pixeles[i])%256;
        }
        for(int j=0;j<hiperplanos.size();j++){
            for(int w=0;w<hiperplanos.get(j).size();w++){
                suma+= hiperplanos.get(j).get(w) * pixeles[w];
            }
            if(suma>0)
                hashFinal+="1";
            else
                hashFinal+="0";
            suma =0;
        }

        return hashFinal;
    }

    public String getHiperPlanos(){
        String strFinal = "";
        for(ArrayList<Integer> hiperPlano : hiperplanos){
            strFinal+="["+convertToString(hiperPlano)+"]"+"\n";
        }
        return strFinal;
    }

    public void setHiperplanos(){
        try{
            File root = new File(Environment.getExternalStorageDirectory(),"Notes");
            File archivo = new File(root, "hiperplanos.txt");
            InputStream instream = new FileInputStream(archivo);
            ArrayList<ArrayList<Integer>> enteros = new ArrayList<>();
            if (instream != null) {
                // prepare the file for reading
                InputStreamReader inputreader = new InputStreamReader(instream);
                BufferedReader buffreader = new BufferedReader(inputreader);
                String line = buffreader.readLine();
                String[] valores;
                ArrayList<Integer> valoresEnteros = null;
                while(line != null){
                    valores = line.split(":");
                    valoresEnteros = convertirAEnteros(valores);
                    enteros.add(valoresEnteros);
                    line = buffreader.readLine();
                }
            }
            hiperplanos = enteros;
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            // close the file.
        }
    }

    public void escribirNuevoHash(String hashNuevo,String name){
        try {
            File root = new File(Environment.getExternalStorageDirectory(), "Notes");
            if (!root.exists()) {
                root.mkdirs();
            }
            File gpxfile = new File(root, "hashes.txt");
            FileWriter writer = new FileWriter(gpxfile,true);
            writer.append(hashNuevo+","+name+"\n");
            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public  ArrayList<Integer> convertirAEnteros(String [] enteros){
        ArrayList<Integer> valoresNuevos = new ArrayList<>();
        for(int i =0;i<enteros.length;i++){
            valoresNuevos.add(Integer.parseInt(enteros[i]));
        }
        return valoresNuevos;
    }

    @NonNull
    static String convertToString(ArrayList<Integer> numbers) {
        StringBuilder builder = new StringBuilder();
        // Append all Integers in StringBuilder to the StringBuilder.
        for (int number : numbers) {
            builder.append(number);
            builder.append(":");
        }
        // Remove last delimiter with setLength.
        builder.setLength(builder.length() - 1);
        return builder.toString();
    }

}
