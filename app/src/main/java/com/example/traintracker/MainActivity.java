package com.example.traintracker;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    AutoCompleteTextView startText;
    AutoCompleteTextView destinationText;
    Button setButton;
    Button viewMapButton;
    RecyclerView RecyclerListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        startText = findViewById(R.id.startAutoComplete);
        destinationText = findViewById(R.id.destinationAutoComplete);
        setButton = findViewById(R.id.setButton);
        viewMapButton = findViewById(R.id.viewMapButtton);
        RecyclerListView = findViewById(R.id.recyclerListView);
        setAutoCompleteTextViews();
    }

    public void setAutoCompleteTextViews(){
        try {
            JSONArray jsonObj = new JSONArray(AssetReader.loadJSONFromAsset(this));
            ArrayList<String> stationNames = new ArrayList<>();
            for(int i = 0; i < jsonObj.length(); i++){
                stationNames.add(jsonObj.getJSONObject(i).getString("stationName"));
                System.out.println(jsonObj.getJSONObject(i).getString("stationName"));
            }
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, stationNames);
            startText.setAdapter(adapter);
            destinationText.setAdapter(adapter);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


}
