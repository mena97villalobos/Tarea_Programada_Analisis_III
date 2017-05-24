package com.example.javier.photosorter;

import android.content.Intent;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
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
    private Map<String,ArrayList<String>> mapaPixeles = new HashMap();


    public void cargarImagen(View v){
        Intent i = new Intent(this,ViewImage.class);
        startActivity(i);
    }



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_actividad__expandable);

        //simpleExpandableListView.addChildrenForAccessibility(new ArrayList<View>(1));

        // add data for displaying in expandable list view
        //loadData();
        cargarHashes();

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
                Toast.makeText(getBaseContext(), " Clicked on :: " + headerInfo.getName()
                        + "/" + detailInfo.getName(), Toast.LENGTH_LONG).show();


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
                Toast.makeText(getBaseContext(), " Header is :: " + headerInfo.getName(),
                        Toast.LENGTH_LONG).show();

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


                for(String key: mapaPixeles.keySet()){
                    for(String valor: mapaPixeles.get(key)){
                        addProduct(key,valor);
                    }
                }
            }



        } catch (Exception ex) {
            // print stack trace.

        } finally {
            // close the file.

        }
    }


}
