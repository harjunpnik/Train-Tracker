package com.example.traintracker;

import android.os.AsyncTask;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {
    AutoCompleteTextView startText;
    AutoCompleteTextView destinationText;
    Button setButton;
    Button viewMapButton;
    RecyclerView.LayoutManager layoutManager;
    private RecyclerView.Adapter mAdapter;

    private HashMap<String,String> stationNames;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        startText = findViewById(R.id.startAutoComplete);
        destinationText = findViewById(R.id.destinationAutoComplete);
        setButton = findViewById(R.id.setButton);
        viewMapButton = findViewById(R.id.viewMapButtton);

        setAutoCompleteTextViews();
    }

    //  Sets the adapters of the autoComplete Lists
    public void setAutoCompleteTextViews(){
        try {
            //  Read Json file as Array. Takes the string and converts it to JSON Array
            JSONArray jsonObj = new JSONArray(AssetReader.loadStationsFromAsset(this));
            ArrayList<String> adapterNames = new ArrayList<>();
            stationNames = new HashMap<String,String>();
            for(int i = 0; i < jsonObj.length(); i++){
                adapterNames.add(jsonObj.getJSONObject(i).getString("stationName"));
                //System.out.println(jsonObj.getJSONObject(i).getString("stationName"));
                stationNames.put(jsonObj.getJSONObject(i).getString("stationName"), jsonObj.getJSONObject(i).getString("stationShortCode"));
            }
            //  Creates adapter and sets it to the AutoComplete lists
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, adapterNames);
            startText.setAdapter(adapter);
            destinationText.setAdapter(adapter);
        } catch (JSONException e) {
            Toast.makeText(getApplicationContext(), "Failed to successfully create Auto Complete Lists", Toast.LENGTH_LONG).show();
        }
    }

    //  OnClick of Set Route
    public void onClickSetRoute(View v){
        String start = stationNames.get(startText.getText().toString());
        String destination = stationNames.get(destinationText.getText().toString());
        System.out.println(start + " " + destination);
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date date = new Date();
        System.out.println(dateFormat.format(date));
        System.out.println("https://rata.digitraffic.fi/api/v1/live-trains/station/" + start + "/" + destination + "/?departure_date=" + dateFormat.format(date));

        String url = "https://rata.digitraffic.fi/api/v1/live-trains/station/" + start + "/" + destination + "/?departure_date=" + dateFormat.format(date);

            new GetTrains(this, start, destination, this).execute(url);

    }

    public void setRecyclerView(ArrayList<Train> trains){
        String[] menuValues = new String[trains.size()];

        int i = 0;
        for(Train train : trains)
        {
            menuValues[i] = train.getNameFormated();
            i++;
        }

        ArrayAdapter<String> itemsAdapter = new ArrayAdapter(this,
                android.R.layout.simple_list_item_1, menuValues);

        final ListView lv = findViewById(R.id.listView);
        lv.setAdapter(itemsAdapter);

    }

}

