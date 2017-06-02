package com.example.javier.photosorter;

import android.graphics.Bitmap;

import java.util.ArrayList;

/**
 * Created by Anthony on 1/6/17.
 */

public class Compare {


    public double getAvgDistance(ArrayList<int[]> imagenes) {

        int totalImagenes = imagenes.size();
        double distanciaPromedio = 0;
        for (int i = 0; i < imagenes.size(); i++) {
            for (int j = 0; j < imagenes.size(); j++) {
                if (i != j) { //Para que no se compare con sí mismo
                    distanciaPromedio += getCosineDistance(imagenes.get(i), imagenes.get(j));
                }
            }

        }
        distanciaPromedio = distanciaPromedio / totalImagenes;
        return distanciaPromedio;
    }


    public double getCosineDistance(int[] img1, int[] img2) {
        double sumNum = 0, sumImg1Den = 0, sumImg2Den = 0;
        for (int i = 0; i < img1.length; i++) {
            sumNum += (img1[i] * img2[i]);
            sumImg1Den += Math.pow(img1[i], 2);
            sumImg2Den += Math.pow(img2[i], 2);
        }
        sumImg1Den = Math.sqrt(sumImg1Den);
        sumImg2Den = Math.sqrt(sumImg2Den);

        return 100 * (1 - sumNum / (sumImg1Den * sumImg2Den));
    }
}
