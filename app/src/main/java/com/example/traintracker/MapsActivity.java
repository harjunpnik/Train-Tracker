package com.example.traintracker;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Intent;
import android.graphics.Color;
import android.os.PersistableBundle;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
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
    private int trainNumber;

    private TrainPosition trainPosition;

    Button followButton;
    static boolean trainFollowing = false;

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

        followButton = findViewById(R.id.followButton);
        setFollowButtonText();

        loadValues();
    }

    //  Sets the Follow/Unfollow button correctly
    private void setFollowButtonText(){
        if(trainFollowing){
            followButton.setText("Unfollow");
        }else{
            followButton.setText("Follow");
        }
    }

    //  Load in starting values
    private void loadValues(){
        HashMap<String, Train> trains = FileIO.loadTrains(this);
        Intent intent = getIntent();
        String currentTrainName = intent.getStringExtra("full name");

        nameText.setText(trains.get(currentTrainName).getNameFormated());
        startTimeText.setText("Start Time: " + trains.get(currentTrainName).getStartTime());
        arrivalTimeText.setText("Arrival Time: " + trains.get(currentTrainName).getArrivalTime());

        startName = trains.get(currentTrainName).getStart();
        destinationName = trains.get(currentTrainName).getDestination();
        trainNumber = trains.get(currentTrainName).getNumber();

        trainPosition = new TrainPosition(this, trainNumber);

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
        loadShortCodeTranslatorArray();

    }

    //  Back button that takes you to the main page
    public void onBack(View v) {
        //  Cancels background process
        trainPosition.cancel(true);
        //  Sends intet values for MainActivity to refresh the ListView
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("start name", startName);
        intent.putExtra("destination name", destinationName);
        startActivity(intent);
        finish();
    }

    //  Executes when map is ready, updates camera position, draws markers and starts background process of trainPosition tracking
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        //mMap.moveCamera(CameraUpdateFactory.newLatLng(startLatLng));
        CameraPosition cameraMovement = CameraPosition.builder()
                .target(startLatLng)
                .zoom(10)
                .build();
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraMovement));
        drawOnMap();
        //new TrainPosition(this, trainNumber).execute();
        //  Start train tracking when map has loaded
        trainPosition.execute();
    }

    //  Draws on map the markers
    public void drawOnMap(){
        mMap.clear();
        mMap.addMarker(new MarkerOptions().position(startLatLng).title(startName));
        mMap.addMarker(new MarkerOptions().position(destinationLatLng).title(destinationName).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));

        if(trainLatLng != null)
        mMap.addMarker(new MarkerOptions().position(trainLatLng).title("Train " + trainNumber).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
    }

    //  Sets the train LatLng position, called from TrainPosition.java
    public void setNewTrainLatLng(LatLng newPosition){
        trainLatLng = newPosition;
    }

    //  Toast user the error. Called from TrainPosition.java
    public void toastError(String errorMsg){
        Toast.makeText(getApplicationContext(), errorMsg, Toast.LENGTH_LONG).show();
    }


    //TODO MAKE THIS TWO SEPPARATE BUTTONS
    public void onClickStartTrainFollowing(View v){
        trainFollowing = !trainFollowing;
        setFollowButtonText();

        if(trainFollowing){
            System.out.println("Train  being followed");
            scheduleTrainJob();
        }else{
            System.out.println("Train is not being followed");
            cancelTrainJob();
        }

    }

    //TODO MOVE TO FUNCTIONS CLASS
    private HashMap<String,String> stationNames;
    protected void loadShortCodeTranslatorArray(){
        try {
            //  Read Json file as Array. Takes the string and converts it to JSON Array
            //  This is HashMap is used for converting ShortCodes from the API to the full station names.
            JSONArray jsonObj = new JSONArray(AssetReader.loadStationsFromAsset(this));
            stationNames = new HashMap<>();
            for(int i = 0; i < jsonObj.length(); i++){
                stationNames.put(jsonObj.getJSONObject(i).getString("stationName"),jsonObj.getJSONObject(i).getString("stationShortCode"));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    //TODO CLEAN THIS TESTING MESS
    private void scheduleTrainJob(){
        ComponentName  test = new ComponentName(this, TrainJobService.class);

        PersistableBundle bundle = new PersistableBundle();
        bundle.putInt("trainNumber", trainNumber);
        bundle.putString("trainStartNameShortCode", stationNames.get(startName));

        JobInfo info = new JobInfo.Builder(7,test)
                .setExtras(bundle)
                .setRequiresBatteryNotLow(true)
                .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                .setPersisted(true)
                .setPeriodic(15 * 60 * 1000)
                .build();///    SET ON BATTERY NOT LOW AND OTHER SETTINGS

        JobScheduler scheduler = (JobScheduler) getSystemService(JOB_SCHEDULER_SERVICE);
        int resultCode = scheduler.schedule(info);
        if(resultCode == JobScheduler.RESULT_SUCCESS){
            System.out.println("JOB SCHEDULER SUCCESS");
        }else{
            System.out.println("JOB SCHEDULER FAILED"); //Use LOG
        }
    }

    //TODO CANCEL ONLY CERTAIN TRAIN
    private void cancelTrainJob(){
        JobScheduler scheduler = (JobScheduler) getSystemService(JOB_SCHEDULER_SERVICE);
        scheduler.cancel(7);
        System.out.println("JOB SCHEDULER JOB CANCELLD");
    }

}
