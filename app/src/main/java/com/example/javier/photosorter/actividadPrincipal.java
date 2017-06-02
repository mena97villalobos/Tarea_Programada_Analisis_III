package com.example.javier.photosorter;

//Imports sin usar
import android.app.Fragment;
import android.support.design.widget.Snackbar;
import android.provider.Settings;
import android.support.v4.app.FragmentManager;
import android.widget.EditText;
import java.util.Map;
import java.util.Arrays;
import android.widget.Button;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;
import android.content.Context;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.Toast;
import java.io.File;
import android.widget.TextView;

//Imports OpenCV
import org.opencv.core.Scalar;
import org.opencv.features2d.Features2d;
import org.opencv.engine.OpenCVEngineInterface;
import org.opencv.core.MatOfByte;
import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfInt;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDMatch;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.core.DMatch;
import org.opencv.features2d.DescriptorExtractor;
import org.opencv.features2d.DescriptorMatcher;
import org.opencv.features2d.FeatureDetector;
import org.opencv.imgproc.Imgproc;

public class actividadPrincipal extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private static final String TAG = "MainActivity";

    static {
        System.loadLibrary("opencv_java3");
    }

    static {
        if (!OpenCVLoader.initDebug())
            Log.d(TAG, "OpenCV not Loaded");
        else
            Log.d(TAG, "OpenCV Loaded");
    }

    public static final String UNABLE_TO_SAVE_PHOTO_FILE = "Unable to save photo file";
    private static String logtag = "CameraApp8";
    private Uri imageUri;
    private static final int TAKE_PICTURE = 0;
    private File imageFile;
    public PixelHash pixelHash = new PixelHash();
    public LBPHash lbpHash = new LBPHash();
    public int RESULT_LOAD_IMG = 1;

    public int[][] pixelesLBP = new int[256][256];

    //Variables OpenCV
    private static Bitmap bmp, yourSelectedImage;//, bmpimg1, bmpimg2;
    private static Mat descriptors, dupDescriptor;//, img1, img2,;
    private static FeatureDetector detector;
    private static DescriptorExtractor DescExtractor;
    private static DescriptorMatcher matcher;
    private static MatOfKeyPoint keypoints, dupKeypoints;
    private static MatOfDMatch matches, matches_final_mat;
    private static int descriptor = DescriptorExtractor.BRISK;
    public double distance;
    public double min_dist = 0.775;

    public void abrirPixeles(View v) {
        Bundle b = new Bundle();
        b.putString("Alg", "Pixeles");

        Intent i = new Intent(this, Actividad_Expandable.class);
        i.putExtras(b);
        startActivity(i);
        finish();
    }

    public void abrirLBP(View v) {
        Bundle b = new Bundle();
        b.putString("Alg", "LBP");

        Intent i = new Intent(this, Actividad_Expandable.class);
        i.putExtras(b);
        startActivity(i);
        finish();
    }

    public void cargarHiperPlanos(View v) {
        //Toast.makeText(this, "Cargar LBP", Toast.LENGTH_SHORT).show();
    }

    public void iniciarCamara(View v) {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);//"android.media.action.ACTION_IMAGE_CAPTURE");
        File photo = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), ("IMG_" + System.currentTimeMillis() + "_photo.jpg"));
        imageFile = new File(photo, "passpoints_image");
        imageUri = Uri.fromFile(photo);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        startActivityForResult(intent, TAKE_PICTURE);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 100);
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 100);
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 100);
        }
        setContentView(R.layout.activity_actividad_principal);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        FloatingActionButton camara = (FloatingActionButton) findViewById(R.id.camara);
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        camara.setOnClickListener(camaraListener);
        generateNoteOnSD(actividadPrincipal.this, "Debug.txt", pixelHash.loadResult);
        generateNoteOnSD(actividadPrincipal.this, "DebugLBP.txt", lbpHash.loadResult);

    }

    public View.OnClickListener camaraListener = new View.OnClickListener() {
        public void onClick(View v) {
            iniciarCamara(v);
        }
    };

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if (requestCode == RESULT_LOAD_IMG && resultCode == RESULT_OK) {
            try {
                ImageView image_view = (ImageView) findViewById(R.id.viewPhoto);
                Uri imageUri = intent.getData();
                InputStream imageStream = getContentResolver().openInputStream(imageUri);
                Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
                image_view.setImageBitmap(selectedImage);
                //TODO De aqui pa'lla se puede joder PD le quite el final a las variables de arriba
                String pathCargado = getRealPathFromURI(imageUri);
                //Copiar la imagen al directorio Pictures
                String pathDest = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString();
                copyFileOrDirectory(pathCargado, pathDest);
                //
                //LBP Prueba
                String nombreImagen = new File(pathCargado).getName();
                //String hashLBP = lbpHash.comparar(selectedImage, nombreImagen);


                //Pixel Hash
                selectedImage = pixelHash.resizeImage(selectedImage);
                selectedImage = pixelHash.toGrayscale(selectedImage);
                String informacion = "";
                int[] pixeles = new int[selectedImage.getHeight() * selectedImage.getWidth()];
                selectedImage.getPixels(pixeles, 0, selectedImage.getWidth(), 0, 0, selectedImage.getWidth(), selectedImage.getHeight());
                informacion = "Total de pixeles: " + pixeles.length + "\n";
                String hashImagen = pixelHash.hashImagen(pixeles);
                boolean yaExisteArchivoHash = archivoYaExiste("hashes.txt");
                if (!yaExisteArchivoHash) {
                    generateNoteOnSD(actividadPrincipal.this, "hashes.txt", "");
                }
                pixelHash.escribirNuevoHash(hashImagen, nombreImagen);
                informacion += "Imagen tomada y su hash: " + hashImagen;
                generateNoteOnSD(actividadPrincipal.this, "info.txt", informacion);

                boolean pixelExists = archivoYaExiste("hiperplanos.txt");
                if (!pixelExists) {
                    String hp = pixelHash.getHiperPlanos();
                    generateNoteOnSD(actividadPrincipal.this, "hiperplanos.txt", hp);
                }

                //Logica para LBP
                pixelesLBP = lbpHash.getPixeles(selectedImage);
                ArrayList<Integer> vectorLBP = lbpHash.generarHistogramaLBP(pixelesLBP);
                String hashLBP = lbpHash.hashImagen(vectorLBP);
                lbpHash.escribirNuevoHash(hashLBP, nombreImagen);

                boolean lbpExists = archivoYaExiste("lbpHiperplanos.txt");
                if (!lbpExists) {
                    String hp2 = lbpHash.getHiperPlanos();
                    generateNoteOnSD(actividadPrincipal.this, "lbpHiperplanos.txt", hp2);
                }



            } catch (FileNotFoundException e) {
                e.printStackTrace();
                Toast.makeText(actividadPrincipal.this, "Error", Toast.LENGTH_LONG).show();
            }
        } else if (requestCode == TAKE_PICTURE && resultCode == Activity.RESULT_OK) {
            Uri selectedImage = imageUri;
            getContentResolver().notifyChange(selectedImage, null);
            ImageView imageView = null;
            imageView = (ImageView) findViewById(R.id.viewPhoto);
            ContentResolver cr = getContentResolver();
            Bitmap bitmap;
            String nombreImagen = new File(selectedImage.getPath()).getName();
            try {
                bitmap = MediaStore.Images.Media.getBitmap(cr, selectedImage);
                imageView.setImageBitmap(bitmap);

                //String hashLBP = lbpHash.comparar(bitmap, nombreImagen);

                bitmap = pixelHash.resizeImage(bitmap);
                bitmap = pixelHash.toGrayscale(bitmap);
                String info = "";
                int[] pixeles = new int[bitmap.getHeight() * bitmap.getWidth()];
                bitmap.getPixels(pixeles, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());
                info = "Total de pixeles: " + pixeles.length + "\n";
                String hashImagen = pixelHash.hashImagen(pixeles);
                boolean yaExisteArchivoHash = archivoYaExiste("hashes.txt");
                if (!yaExisteArchivoHash) {
                    //Toast.makeText(actividadPrincipal.this, "Creando Archivo Hashes", Toast.LENGTH_LONG).show();
                    generateNoteOnSD(actividadPrincipal.this, "hashes.txt", "");
                }
                pixelHash.escribirNuevoHash(hashImagen, nombreImagen);
                info += "Imagen tomada y su hash: " + hashImagen;
                generateNoteOnSD(actividadPrincipal.this, "info.txt", info);

                boolean pixelExists = archivoYaExiste("hiperplanos.txt");
                if (!pixelExists) {
                    String hp = pixelHash.getHiperPlanos();
                    generateNoteOnSD(actividadPrincipal.this, "hiperplanos.txt", hp);
                }

                //Logica para LBP
                pixelesLBP = lbpHash.getPixeles(bitmap); //Si falla, revisar esto...
                ArrayList<Integer> vectorLBP = lbpHash.generarHistogramaLBP(pixelesLBP);
                String hashLBP = lbpHash.hashImagen(vectorLBP);
                lbpHash.escribirNuevoHash(hashLBP, nombreImagen);

                boolean lbpExists = archivoYaExiste("lbpHiperplanos.txt");
                if (!lbpExists) {
                    String hp2 = lbpHash.getHiperPlanos();
                    generateNoteOnSD(actividadPrincipal.this, "lbpHiperplanos.txt", hp2);
                }



            } catch (Exception e) {
                Log.e(logtag, e.toString());
            }
        }
    }

    public void generateNoteOnSD(Context context, String sFileName, String sBody) {
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
            //Toast.makeText(actividadPrincipal.this, "Archivo guardado", Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            Toast.makeText(actividadPrincipal.this, "Archivo no guardado", Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.actividad_principal, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        android.app.FragmentManager fragmentManager = getFragmentManager();
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public boolean archivoYaExiste(String fileName) {
        File root = new File(Environment.getExternalStorageDirectory(), "Notes");
        File archivo = new File(root, fileName);
        if (!archivo.exists())
            return false;
        return true;
    }

    // Carga de Imagen desde la galeria
    public void onButtonCargarImagenClick(View view) {
        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
        photoPickerIntent.setType("image/*");
        startActivityForResult(photoPickerIntent, RESULT_LOAD_IMG);
    }

    public String getRealPathFromURI(Uri uri) {
        String[] projection = { MediaStore.Images.Media.DATA };
        @SuppressWarnings("deprecation")
        Cursor cursor = managedQuery(uri, projection, null, null, null);
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }

    public static void copyFileOrDirectory(String srcDir, String dstDir) {
        try {
            File src = new File(srcDir);
            File dst = new File(dstDir, src.getName());
            if (src.isDirectory()) {
                String files[] = src.list();
                int filesLength = files.length;
                for (int i = 0; i < filesLength; i++) {
                    String src1 = (new File(src, files[i]).getPath());
                    String dst1 = dst.getPath();
                    copyFileOrDirectory(src1, dst1);
                }
            } else {
                copyFile(src, dst);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void copyFile(File sourceFile, File destFile) throws IOException {
        if (!destFile.getParentFile().exists())
            destFile.getParentFile().mkdirs();

        if (!destFile.exists()) {
            destFile.createNewFile();
        }

        FileChannel source = null;
        FileChannel destination = null;

        try {
            source = new FileInputStream(sourceFile).getChannel();
            destination = new FileOutputStream(destFile).getChannel();
            destination.transferFrom(source, 0, source.size());
        } finally {
            if (source != null) {
                source.close();
            }
            if (destination != null) {
                destination.close();
            }
        }
    }

}
