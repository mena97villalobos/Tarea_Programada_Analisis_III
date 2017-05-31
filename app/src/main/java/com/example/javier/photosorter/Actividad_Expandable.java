package com.example.javier.photosorter;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Environment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class Actividad_Expandable extends AppCompatActivity {


    private LinkedHashMap<String, GroupInfo> subjects = new LinkedHashMap<String, GroupInfo>();
    private ArrayList<GroupInfo> deptList = new ArrayList<GroupInfo>();

    private CustomAdapter listAdapter;
    private ExpandableListView simpleExpandableListView;

    //TODO PIXELES
    private Map<String,ArrayList<String>> mapaPixeles = new HashMap();
    private Map<String, String> mapaTagsPixeles = new HashMap();

    //TODO LBP
    private Map<String, ArrayList<String>> mapaLBP = new HashMap();
    private Map<String, String> mapaTagsLBP = new HashMap();

    private String algoritmo = "";

    public void cargarImagen(View v){
        Intent i = new Intent(this,ViewImage.class);
        startActivity(i);
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_actividad__expandable);

        Bundle b2 = getIntent().getExtras();
        if (b2 != null) {
            algoritmo = b2.getString("Alg");
        }

        //simpleExpandableListView.addChildrenForAccessibility(new ArrayList<View>(1));

        // add data for displaying in expandable list view
        //loadData();

        //TODO Hacerle un if para cargarHashes de Pixeles o de LBP

        if (algoritmo.equals("Pixeles"))
            cargarHashes();
        else
            cargarHashesLBP();

        Toast.makeText(this, algoritmo, Toast.LENGTH_SHORT).show();

        //get reference of the ExpandableListView
        simpleExpandableListView = (ExpandableListView) findViewById(R.id.simpleExpandableListView);
        // create the adapter by passing your ArrayList data
        listAdapter = new CustomAdapter(Actividad_Expandable.this, deptList);
        // attach the adapter to the expandable list view
        simpleExpandableListView.setAdapter(listAdapter);

        //expand all the Groups
        expandAll();

        // setOnChildClickListener listener for child row click
        simpleExpandableListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                //get the group header
                GroupInfo headerInfo = deptList.get(groupPosition);
                //get the child info
                ChildInfo detailInfo =  headerInfo.getProductList().get(childPosition);
                //display it or do something with it
                //Toast.makeText(getBaseContext(), " Clicked on :: " + headerInfo.getName() + "/" + detailInfo.getName(), Toast.LENGTH_LONG).show();
                Intent i = new Intent(Actividad_Expandable.this,ViewImage.class);
                Bundle b = new Bundle();
                b.putString("img",detailInfo.getName());
                i.putExtras(b);
                startActivity(i);
                finish();

                return false;
            }
        });

        // setOnGroupClickListener listener for group heading click
        simpleExpandableListView.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
            @Override
            public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
                //get the group header
                GroupInfo headerInfo = deptList.get(groupPosition);
                //display it or do something with it
                //Toast.makeText(getBaseContext(), " Header is :: " + headerInfo.getName(),
                  //      Toast.LENGTH_LONG).show();

                return false;
            }

        });


        //este es el mejor candidato para mi
        simpleExpandableListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {

                //Toast.makeText(getBaseContext(), "Item Pos: "+simpleExpandableListView., Toast.LENGTH_SHORT).show();


                if(position>=0 && position<deptList.size()) {
                    final GroupInfo headerInfo = deptList.get(position);

                    //Toast.makeText(getBaseContext(), "Nombre:" + headerInfo.getName(), Toast.LENGTH_LONG).show();

                    // get prompts.xml view
                    LayoutInflater li = LayoutInflater.from(Actividad_Expandable.this);
                    View promptsView = li.inflate(R.layout.prompt, null);

                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(Actividad_Expandable.this);

                    // set prompts.xml to alertdialog builder
                    alertDialogBuilder.setView(promptsView);

                    final EditText userInput = (EditText) promptsView
                            .findViewById(R.id.editTextDialogUserInput);

                    TextView nombreDelBucket = (TextView) promptsView.findViewById(R.id.textView2);
                    nombreDelBucket.setText("Nombre a cambiar: " + headerInfo.getName());


                    // set dialog message
                    alertDialogBuilder
                            .setCancelable(false)
                            .setPositiveButton("OK",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            // get user input and set it to result
                                            // edit text
                                            // EditText cuadroEditar = (EditText)findViewById(R.id.editTextDialogUserInput);
                                            //cuadroEditar.setText(userInput.getText());
                                            if (algoritmo.equals("Pixeles")) {
                                                escribirNuevoTag(headerInfo.getName(), userInput.getText().toString());
                                                headerInfo.setName(userInput.getText().toString());
                                            } else {
                                                escribirNuevoTagLBP(headerInfo.getName(), userInput.getText().toString());
                                                headerInfo.setName(userInput.getText().toString());
                                            }
                                        }
                                    })
                            .setNegativeButton("Cancel",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            dialog.cancel();
                                        }
                                    });

                    // create alert dialog
                    AlertDialog alertDialog = alertDialogBuilder.create();

                    // show it
                    alertDialog.show();


                }else{
                    Toast.makeText(getBaseContext(), "Cierre los Buckets", Toast.LENGTH_LONG).show();

                }


                return false;
            }
        });



    }

    //method to expand all groups
    private void expandAll() {
        int count = listAdapter.getGroupCount();
        for (int i = 0; i < count; i++){
            simpleExpandableListView.expandGroup(i);
        }
    }

    //method to collapse all groups
    private void collapseAll() {
        int count = listAdapter.getGroupCount();
        for (int i = 0; i < count; i++){
            simpleExpandableListView.collapseGroup(i);
        }
    }

    //load some initial data into out list
    private void loadData(){

        addProduct("Android","ListView");
        addProduct("Android","ExpandableListView");
        addProduct("Android","GridView");

        addProduct("Java","PolyMorphism");
        addProduct("Java","Collections");

    }

    //here we maintain our products in various departments
    private int addProduct(String department, String product){

        int groupPosition = 0;

        //check the hash map if the group already exists
        GroupInfo headerInfo = subjects.get(department);
        //add the group if doesn't exists
        if(headerInfo == null){
            headerInfo = new GroupInfo();
            headerInfo.setName(department);
            subjects.put(department, headerInfo);
            deptList.add(headerInfo);
        }

        //get the children for the group
        ArrayList<ChildInfo> productList = headerInfo.getProductList();
        //size of the children list
        int listSize = productList.size();
        //add to the counter
        listSize++;

        //create a new child and add that to the group
        ChildInfo detailInfo = new ChildInfo();
        detailInfo.setSequence(String.valueOf(listSize));
        detailInfo.setName(product);
        productList.add(detailInfo);
        headerInfo.setProductList(productList);

        //find the group position inside the list
        groupPosition = deptList.indexOf(headerInfo);
        return groupPosition;
    }

    public void cargarHashesLBP() {
        try {

            File root = new File(Environment.getExternalStorageDirectory(), "Notes");
            File archivo = new File(root, "LBPDiccionary.txt");


            InputStream instream = new FileInputStream(archivo);

            if (instream != null) {
                // prepare the file for reading
                InputStreamReader inputreader = new InputStreamReader(instream);
                BufferedReader buffreader = new BufferedReader(inputreader);

                String line = buffreader.readLine();

                ArrayList valoresEnteros;
                while (line != null) {

                    ArrayList<String> temp;

                    if (mapaLBP.get(line.split(",")[0]) != null)
                        temp = mapaLBP.get(line.split(",")[0]);
                    else
                        temp = new ArrayList();

                    temp.add(line.split(",")[1]);
                    mapaLBP.put(line.split(",")[0], temp);


                    //addProduct(,"imagenxDxdXDxd"); // en lugar de imagen xdxdx hay que agregar el nombre de la imagen
                    line = buffreader.readLine();
                }

                cargarTagsLBP();

                for (String key : mapaLBP.keySet()) {
                    for (String valor : mapaLBP.get(key)) {
                        if (mapaTagsLBP.get(key) == null)
                            addProduct(key, valor);
                        else
                            addProduct(mapaTagsLBP.get(key), valor);
                    }
                }
            }


        } catch (Exception ex) {
            // print stack trace.
        }
    }

    public void cargarHashes(){

        try{

            File root = new File(Environment.getExternalStorageDirectory(),"Notes");
            File archivo = new File(root, "hashes.txt");


            InputStream instream = new FileInputStream(archivo);

            if (instream != null) {
                // prepare the file for reading
                InputStreamReader inputreader = new InputStreamReader(instream);
                BufferedReader buffreader = new BufferedReader(inputreader);

                String line = buffreader.readLine();

                ArrayList valoresEnteros;
                while(line != null){

                    ArrayList<String> temp;

                    if(mapaPixeles.get(line.split(",")[0]) != null)
                        temp = mapaPixeles.get(line.split(",")[0]);
                    else
                        temp = new ArrayList();

                    temp.add(line.split(",")[1]);
                    mapaPixeles.put(line.split(",")[0],temp);


                    //addProduct(,"imagenxDxdXDxd"); // en lugar de imagen xdxdx hay que agregar el nombre de la imagen
                    line = buffreader.readLine();
                }

                cargarTags(); //MUY IMPORTANTE PARA SABER CUALES TAGS EXISTEN

                for(String key: mapaPixeles.keySet()){
                    for(String valor: mapaPixeles.get(key)){
                        if (mapaTagsPixeles.get(key) == null)
                            addProduct(key,valor);
                        else
                            addProduct(mapaTagsPixeles.get(key), valor);
                    }
                }
            }


        } catch (Exception ex) {
            // print stack trace.

        }
    }

    public void cargarTagsLBP() {
        try {

            File root = new File(Environment.getExternalStorageDirectory(), "Notes");
            File archivo = new File(root, "tags_hashes_LBP.txt");

            InputStream instream = new FileInputStream(archivo);

            if (instream != null) {
                // prepare the file for reading
                InputStreamReader inputreader = new InputStreamReader(instream);
                BufferedReader buffreader = new BufferedReader(inputreader);

                String line = buffreader.readLine();

                while (line != null) {

                    mapaTagsLBP.put(line.split(",")[0], line.split(",")[1]);
                    line = buffreader.readLine();
                }
            }
        } catch (Exception ex) {
            // print stack trace.

        } finally {
            // close the file.

        }
    }

    public void cargarTags(){
        try{

            File root = new File(Environment.getExternalStorageDirectory(),"Notes");
            File archivo = new File(root, "tags_hashes.txt");

            InputStream instream = new FileInputStream(archivo);

            if (instream != null) {
                // prepare the file for reading
                InputStreamReader inputreader = new InputStreamReader(instream);
                BufferedReader buffreader = new BufferedReader(inputreader);

                String line = buffreader.readLine();

                while(line != null){

                    mapaTagsPixeles.put(line.split(",")[0], line.split(",")[1]);
                    line = buffreader.readLine();
                }
            }
        } catch (Exception ex) {
            // print stack trace.

        } finally {
            // close the file.

        }
    }

    public void escribirNuevoTag(String hash, String tag){
        try {
            File root = new File(Environment.getExternalStorageDirectory(), "Notes");
            if (!root.exists()) {
                root.mkdirs();
            }
            File gpxfile = new File(root, "tags_hashes.txt");
            FileWriter writer = new FileWriter(gpxfile,true);
            writer.append(hash+","+tag+"\n");
            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void escribirNuevoTagLBP(String hash, String tag) {
        try {
            File root = new File(Environment.getExternalStorageDirectory(), "Notes");
            if (!root.exists()) {
                root.mkdirs();
            }
            File gpxfile = new File(root, "tags_hashes_LBP.txt");
            FileWriter writer = new FileWriter(gpxfile, true);
            writer.append(hash + "," + tag + "\n");
            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
