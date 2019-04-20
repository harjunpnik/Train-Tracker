package com.example.traintracker;

import android.content.Context;
import android.os.AsyncTask;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.TimeUnit;

public class TrainPosition extends AsyncTask<String, String, String>
{
    MapsActivity maps;
    int trainNumber;
    boolean trainIsRunning;

    public TrainPosition(MapsActivity maps, int trainNumber){
        this.maps = maps;
        this.trainNumber = trainNumber;
        trainIsRunning = true;
    }

    protected void onProgressUpdate(String[] strings)
    {
        //TODO FIX THIS??
        maps.drawOnMap();
    }

    @Override
    protected String doInBackground(String... strings)
    {
        while(trainIsRunning) {
            LatLng newPosition = getTrainLocation();


            if(newPosition != null){
                maps.setNewTrainLatLng(newPosition);
                //  UPDATE MAP
                publishProgress("TEST");
            }else{
                trainIsRunning = false;
            }

            try {
                TimeUnit.SECONDS.sleep(15);
            }catch (InterruptedException e) {
                e.printStackTrace();
            }

        }
        return null;

    }



    private LatLng getTrainLocation(){
        String response = new String();
        String apiUrl = "https://rata.digitraffic.fi/api/v1/train-locations/latest/" + trainNumber;
        try
        {
            URL url = new URL(apiUrl);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            BufferedReader reader = new BufferedReader(new InputStreamReader(con.getInputStream()));

            String nextLine = new String();

            while ((nextLine = reader.readLine()) != null)
            {
                response += nextLine;
            }

            JSONArray responseJson = new JSONArray(response);
            LatLng newCoordinates = new LatLng(responseJson.getJSONObject(0).getJSONObject("location").getJSONArray("coordinates").getDouble(1), responseJson.getJSONObject(0).getJSONObject("location").getJSONArray("coordinates").getDouble(0));
            System.out.println(responseJson.getJSONObject(0).getJSONObject("location").getJSONArray("coordinates").getDouble(1));
            return newCoordinates;
        }
        catch(Exception e)
        {
            return null;
        }
    }

}
