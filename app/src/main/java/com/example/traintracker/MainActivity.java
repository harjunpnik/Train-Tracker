package com.example.traintracker;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ListView;
import android.widget.TextView;
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
    ListView listView;
    TextView loadingText;

    private HashMap<String,String> stationNames;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        startText = findViewById(R.id.startAutoComplete);
        destinationText = findViewById(R.id.destinationAutoComplete);
        listView = findViewById(R.id.listView);
        loadingText = findViewById(R.id.loadingText);

        setAutoCompleteTextViews();

        //  If user returns from map page, this reloads the ListView
        Intent intent = getIntent();
        String startName = intent.getStringExtra("start name");
        if(startName != null){
            startText.setText(intent.getStringExtra("start name"));
            destinationText.setText(intent.getStringExtra("destination name"));
            setRoute();
        }
    }

    //  Toasts Error msg to user
    public void toastError(String errorMsg){
        Toast.makeText(getApplicationContext(), errorMsg, Toast.LENGTH_LONG).show();
    }

    //  Toggles visibility between loading and ListView
    public void setLoadingVisibility(boolean visibility){
        if(visibility){
            listView.setVisibility(View.GONE);
            loadingText.setVisibility(View.VISIBLE);
        }else{
            listView.setVisibility(View.VISIBLE);
            loadingText.setVisibility(View.GONE);
        }
    }

    //  Sets the adapters of the autoComplete Lists
    public void setAutoCompleteTextViews(){
        try {
            //  Read Json file as Array. Takes the string and converts it to JSON Array
            JSONArray jsonObj = new JSONArray(AssetReader.loadStationsFromAsset(this));
            ArrayList<String> adapterNames = new ArrayList<>();
            stationNames = new HashMap<>();
            for(int i = 0; i < jsonObj.length(); i++){
                adapterNames.add(jsonObj.getJSONObject(i).getString("stationName"));
                stationNames.put(jsonObj.getJSONObject(i).getString("stationName"), jsonObj.getJSONObject(i).getString("stationShortCode"));
            }
            //  Creates adapter and sets it to the AutoComplete lists
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, adapterNames);
            startText.setAdapter(adapter);
            destinationText.setAdapter(adapter);
        } catch (JSONException e) {
            toastError("Failed to successfully create Auto Complete Lists");
        }
    }

    //  OnClick of Set Route, called by button
    public void onClickSetRoute(View v){
        setRoute();
    }


    //  On set route makes AsyncTask api call and updates the ListView based on on the result
    public void setRoute(){
        String start = stationNames.get(startText.getText().toString());
        String destination = stationNames.get(destinationText.getText().toString());
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date date = new Date();

        System.out.println(start + " " + destination);
        System.out.println(dateFormat.format(date));
        System.out.println("https://rata.digitraffic.fi/api/v1/live-trains/station/" + start + "/" + destination + "/?departure_date=" + dateFormat.format(date));

        String url = "https://rata.digitraffic.fi/api/v1/live-trains/station/" + start + "/" + destination + "/?departure_date=" + dateFormat.format(date);

        new GetTrains(this, start, destination, this).execute(url);
    }


    //  Sets ListView to current array and makes each item clickable
    public void setListView(ArrayList<Train> trains){
        String[] menuValues = new String[trains.size()];

        int i = 0;
        for(Train train : trains) {
            menuValues[i] = train.getNameFormated();
            i++;
        }

        ArrayAdapter<String> itemsAdapter = new ArrayAdapter(this,
                android.R.layout.simple_list_item_1, menuValues);

        listView.setAdapter(itemsAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l)
            {
                Intent intent = new Intent(getApplicationContext(), MapsActivity.class);
                String itemFullName = (String) listView.getItemAtPosition(i);
                intent.putExtra("full name", itemFullName);

                startActivity(intent);
                finish();
            }
        });
    }

}

