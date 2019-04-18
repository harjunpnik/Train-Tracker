package com.example.traintracker;

import android.content.Intent;
import android.graphics.Color;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.PolyUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    TextView nameText;
    TextView startTimeText;
    TextView arrivalTimeText;
    private LatLng startLatLng;
    private LatLng destinationLatLng;
    private String startName;
    private String destinationName;
    private LatLng trainLatLng;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        nameText = findViewById(R.id.nameText);
        startTimeText = findViewById(R.id.startTimeText);
        arrivalTimeText = findViewById(R.id.arrivalTimeText);

        loadValues();

    }

    public void loadValues(){

        HashMap<String, Train> trains = FileIO.loadAccounts(this);
        Intent intent = getIntent();
        String currentTrainName = intent.getStringExtra("full name");

        nameText.setText(trains.get(currentTrainName).getNameFormated());
        startTimeText.setText("Start Time: " + trains.get(currentTrainName).getStartTime());
        arrivalTimeText.setText("Arrival Time: " + trains.get(currentTrainName).getArrivalTime());

        startName = trains.get(currentTrainName).getStart();
        destinationName = trains.get(currentTrainName).getDestination();

        try {



            JSONArray jsonObj = new JSONArray(AssetReader.loadStationsFromAsset(this));
            for(int i = 0; i < jsonObj.length(); i++){
                if(startName.equals(jsonObj.getJSONObject(i).getString("stationName")))
                    startLatLng = new LatLng(jsonObj.getJSONObject(i).getDouble("latitude"), jsonObj.getJSONObject(i).getDouble("longitude"));

                if(destinationName.equals(jsonObj.getJSONObject(i).getString("stationName")))
                    destinationLatLng = new LatLng(jsonObj.getJSONObject(i).getDouble("latitude"), jsonObj.getJSONObject(i).getDouble("longitude"));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        try {
            JSONArray jsonArray = new JSONArray(getDataFromApi("https://rata.digitraffic.fi/api/v1/train-locations/latest/" + trains.get(currentTrainName).getNumber()));
            trainLatLng = new LatLng(jsonArray.getJSONObject(0).getJSONObject("location").getJSONArray("coordinates").getDouble(1), jsonArray.getJSONObject(0).getJSONObject("location").getJSONArray("coordinates").getDouble(0));
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    //  Back button that takes you to the main page
    public void onBack(View v) {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        mMap.moveCamera(CameraUpdateFactory.newLatLng(startLatLng));
        drawOnMap();
    }

    public void drawOnMap(){
        mMap.clear();
        mMap.addMarker(new MarkerOptions().position(startLatLng).title(startName));
        mMap.addMarker(new MarkerOptions().position(destinationLatLng).title(destinationName).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));

        //TODO IF TRAIN IS NOT NULL
        if(trainLatLng != null)
        mMap.addMarker(new MarkerOptions().position(trainLatLng).title("Train").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));

    }


    //  Executes Api query
    public String getDataFromApi(String query){
        String JSONresponse = new String();

        try {
            JSONresponse = new HTTPGet().execute(query).get();
        }
        catch (InterruptedException e) { e.printStackTrace(); }
        catch (ExecutionException e) { e.printStackTrace(); }

        if(JSONresponse != null)
            return JSONresponse;
        else
            Toast.makeText(getApplicationContext(), "Couldnt find data", Toast.LENGTH_LONG).show();

        return null;
    }

}
