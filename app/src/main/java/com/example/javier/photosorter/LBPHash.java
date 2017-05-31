package com.example.javier.photosorter;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfInt;
import org.opencv.imgproc.Imgproc;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Random;

/**
 * Created by Anthony on 26/5/17.
 */

public class LBPHash {

    public ArrayList<ArrayList<Integer>> valoresHeadersArchivo;
    public ArrayList<ArrayList<Integer>> hiperplanos;
    public String loadResult = "";

    public LBPHash() {
        boolean yaExisteArchivo = archivoYaExiste("lbpHiperplanos.txt");
        if (!yaExisteArchivo) {
            hiperplanos = generarHiperplanos();
            loadResult = "Generar Hiperplanos";
        } else {
            setHiperplanos();
            loadResult = "Cargar Hiperplanos";
        }
    }

    public String hashImagen(ArrayList<Integer> pixeles) {
        String hashFinal = "";
        long suma = 0;//, mediaHP = 0, mediaImg = 0;
        for (int j = 0; j < hiperplanos.size(); j++) {
            for (int w = 0; w < hiperplanos.get(j).size(); w++) {
                suma += hiperplanos.get(j).get(w) * pixeles.get(w);
            }
            if (suma > 0)
                hashFinal += "1";
            else
                hashFinal += "0";
            suma = 0;
        }

        return hashFinal;
    }


    public void setHiperplanos() {
        try {
            File root = new File(Environment.getExternalStorageDirectory(), "Notes");
            File archivo = new File(root, "lbpHiperplanos.txt");
            InputStream instream = new FileInputStream(archivo);
            ArrayList<ArrayList<Integer>> enteros = new ArrayList<>();
            if (instream != null) {
                // prepare the file for reading
                InputStreamReader inputreader = new InputStreamReader(instream);
                BufferedReader buffreader = new BufferedReader(inputreader);
                String line = buffreader.readLine();
                String[] valores;
                ArrayList<Integer> valoresEnteros = null;
                while (line != null) {
                    line = line.replace("[", "").replace("]", "");
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


    public ArrayList<Integer> convertirAEnteros(String[] enteros) {
        ArrayList<Integer> valoresNuevos = new ArrayList<>();
        for (int i = 0; i < enteros.length; i++) {
            valoresNuevos.add(Integer.parseInt(enteros[i]));
        }
        return valoresNuevos;
    }

    public String comparar(Bitmap bmp, String bmpName){
        int[] arrayAux = new int[bmp.getWidth()*bmp.getHeight()];
        int [] [] intArray = new int[bmp.getWidth()][bmp.getHeight()];
        //copy pixel data from the Bitmap into the 'intArray' array
        bmp.getPixels(arrayAux, 0, bmp.getWidth(), 0, 0, bmp.getWidth(), bmp.getHeight());
        int cont = 0;
        int vuelta = 0;
        int x;
        for(int i = 0; i < arrayAux.length; i++){
            x = (vuelta*bmp.getWidth());
            if(cont == x){
                intArray[vuelta][i-x] = arrayAux[i];
                vuelta++;
                cont++;
            }
            else{
                intArray[vuelta][i-x] = arrayAux[i];
                cont++;
            }
        }
        ArrayList<Integer> histograma = generarHistogramaLBP(intArray);
        String histogramaStr = convertToString(histograma);
        escribirNuevoHash(histogramaStr, bmpName);
        return "";
    }

    public void escribirNuevoHash(String hashNuevo,String name){
        try {
            boolean yaExisteArchivo = archivoYaExiste("LBPDiccionary.txt");
            if (!yaExisteArchivo) {
                generateNoteOnSD("LBPDiccionary.txt", "");
            }
            File root = new File(Environment.getExternalStorageDirectory(), "Notes");
            if (!root.exists()) {
                root.mkdirs();
            }
            File gpxfile = new File(root, "LBPDiccionary.txt");
            FileWriter writer = new FileWriter(gpxfile,true);
            writer.append(hashNuevo+","+name+"\n");
            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void generateNoteOnSD(String sFileName, String sBody) {
        try {
            File root = new File(Environment.getExternalStorageDirectory(), "Notes");
            if (!root.exists()) {
                root.mkdirs();
            }
            File gpxfile = new File(root, sFileName);
            FileWriter writer = new FileWriter(gpxfile);
            writer.append(sBody);
            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean archivoYaExiste(String fileName) {
        File root = new File(Environment.getExternalStorageDirectory(), "Notes");
        File archivo = new File(root, fileName);
        if (!archivo.exists())
            return false;
        return true;
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
    /*
    public boolean compare(Bitmap bmpimg1, Bitmap bmpimg2) {
        bmpimg1 = Bitmap.createScaledBitmap(bmpimg1, 100, 100, true);
        bmpimg2 = Bitmap.createScaledBitmap(bmpimg2, 100, 100, true);
        Mat img1 = new Mat();
        Utils.bitmapToMat(bmpimg1, img1);
        Mat img2 = new Mat();
        Utils.bitmapToMat(bmpimg2, img2);
        Imgproc.cvtColor(img1, img1, Imgproc.COLOR_RGBA2GRAY);
        Imgproc.cvtColor(img2, img2, Imgproc.COLOR_RGBA2GRAY);
        img1.convertTo(img1, CvType.CV_32F);
        img2.convertTo(img2, CvType.CV_32F);
        //Log.d("ImageComparator", "img1:"+img1.rows()+"x"+img1.cols()+" img2:"+img2.rows()+"x"+img2.cols());
        Mat hist1 = new Mat();
        Mat hist2 = new Mat();
        MatOfInt histSize = new MatOfInt(180);
        MatOfInt channels = new MatOfInt(0);
        ArrayList<Mat> bgr_planes1 = new ArrayList<Mat>();
        ArrayList<Mat> bgr_planes2 = new ArrayList<Mat>();
        Core.split(img1, bgr_planes1);
        Core.split(img2, bgr_planes2);
        MatOfFloat histRanges = new MatOfFloat(0f, 180f);
        boolean accumulate = false;
        Imgproc.calcHist(bgr_planes1, channels, new Mat(), hist1, histSize, histRanges, accumulate);
        Core.normalize(hist1, hist1, 0, hist1.rows(), Core.NORM_MINMAX, -1, new Mat());
        Imgproc.calcHist(bgr_planes2, channels, new Mat(), hist2, histSize, histRanges, accumulate);
        Core.normalize(hist2, hist2, 0, hist2.rows(), Core.NORM_MINMAX, -1, new Mat());
        img1.convertTo(img1, CvType.CV_32F);
        img2.convertTo(img2, CvType.CV_32F);
        hist1.convertTo(hist1, CvType.CV_32F);
        hist2.convertTo(hist2, CvType.CV_32F);
        double compare = Imgproc.compareHist(hist1, hist2, Imgproc.CV_COMP_CHISQR);
        Log.d("ImageComparator", "compare: " + compare);
        String x = "";
        if (compare >= 0 && compare < 1500)
            return true;
        else
            return false;
    }
    */

    public int[][] getPixeles(Bitmap imagen) {
        int[][] matrizPixeles = new int[256][256];

        for (int i = 0; i < 256; i++) {
            for (int j = 0; j < 256; j++) {
                matrizPixeles[i][j] = Math.abs(imagen.getPixel(i, j)) % 256;
            }
        }
        return matrizPixeles;
    }


    public ArrayList<ArrayList<Integer>> generarHiperplanos() {
        ArrayList<ArrayList<Integer>> arregloFinal = new ArrayList<>();
        Random rand = new Random();
        int contador = 0;
        while (contador < 10) { //TODO MODIFICAR ESTO TAMBIEN
            ArrayList<Integer> hiperPlanoActual = new ArrayList<>();
            for (int i = 0; i < 256; i++) {
                hiperPlanoActual.add(rand.nextInt(513) - 256);
            }
            contador++;
            arregloFinal.add(hiperPlanoActual);
        }
        return arregloFinal;
    }

    public String getHiperPlanos() {
        String strFinal = "";
        for (ArrayList<Integer> hiperPlano : hiperplanos) {
            strFinal += "[" + convertToString(hiperPlano) + "]" + "\n";
        }
        return strFinal;
    }


    public ArrayList<Integer> generarHistogramaLBP(int[][] pixels){
        int [][] pixeles  = pixels;
        ArrayList<Integer> histograma = new ArrayList<>();
        for (int i = 0; i < pixeles.length;i++){
            for(int j = 0; j < pixeles[i].length;j++){
                int sumaPos = 0;
                int numCentral = pixeles[i][j];
                try{ //Sacando bit A
                    if (pixeles[i-1][j-1] >= numCentral){
                        sumaPos += 128;
                    }
                }catch (Exception error){} //Se desborda
                try{ //Sacando bit B
                    if (pixeles[i-1][j] >= numCentral){
                        sumaPos += 64;
                    }
                }catch (Exception error){} //Se desborda
                try{ //Sacando bit C
                    if (pixeles[i-1][j+1] >= numCentral){
                        sumaPos += 32;
                    }
                }catch (Exception error){} //Se desborda
                try{ //Sacando bit D
                    if (pixeles[i][j+1] >= numCentral){
                        sumaPos += 16;
                    }
                }catch (Exception error){} //Se desborda
                try{ //Sacando bit E
                    if (pixeles[i+1][j+1] >= numCentral){
                        sumaPos += 8;
                    }
                }catch (Exception error){} //Se desborda
                try{ //Sacando bit F
                    if (pixeles[i+1][j] >= numCentral){
                        sumaPos += 4;
                    }
                }catch (Exception error){} //Se desborda
                try{ //Sacando bit G
                    if (pixeles[i+1][j-1] >= numCentral){
                        sumaPos += 2;
                    }
                }catch (Exception error){} //Se desborda
                try{ //Sacando bit H
                    if (pixeles[i][j-1] >= numCentral){
                        sumaPos += 1;
                    }
                }catch (Exception error){} //Se desborda
                histograma.add(sumaPos);
            }
        }
        return histograma;
    }

}
