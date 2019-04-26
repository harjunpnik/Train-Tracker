package com.example.traintracker;

import android.os.AsyncTask;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.TimeUnit;

public class TrainPosition extends AsyncTask<String, String, String> {
    MapsActivity maps;
    private int trainNumber;
    private boolean trainIsRunning = true;

    public TrainPosition(MapsActivity maps, int trainNumber){
        this.maps = maps;
        this.trainNumber = trainNumber;
    }

    //  Updates map
    protected void onProgressUpdate(String[] strings) {
        maps.drawOnMap();
    }

    @Override
    protected String doInBackground(String... strings) {
        while(trainIsRunning) {
            LatLng newPosition = getTrainLocation();

            //  If position not empty, update map
            if(newPosition != null){
                maps.setNewTrainLatLng(newPosition);
                //  calls onProgressUpdate which updates the maps markers
                publishProgress();

            }else{
                trainIsRunning = false;
                break;
            }

            //  If user exits MapsActivity this cancels background Task
            if (isCancelled())
                break;

            //  Waits 15 seconds
            try {
                TimeUnit.SECONDS.sleep(15);
            }catch (InterruptedException e) {
                e.printStackTrace();
            }

        }
        return null;
    }

    //  When train stops or train is not running, toast the user to let them know it
    @Override
    protected void onPostExecute(String s) {
        maps.toastError("Train is not currently running");
    }

    private LatLng getTrainLocation(){
        //  Api call of trains position
        String response = new String();
        String apiUrl = "https://rata.digitraffic.fi/api/v1/train-locations/latest/" + trainNumber;
        try {
            URL url = new URL(apiUrl);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            BufferedReader reader = new BufferedReader(new InputStreamReader(con.getInputStream()));

            String nextLine = new String();

            while ((nextLine = reader.readLine()) != null) {
                response += nextLine;
            }

            JSONArray responseJson = new JSONArray(response);
            LatLng newCoordinates = new LatLng(responseJson.getJSONObject(0).getJSONObject("location").getJSONArray("coordinates").getDouble(1), responseJson.getJSONObject(0).getJSONObject("location").getJSONArray("coordinates").getDouble(0));
            System.out.println("train coord : " +responseJson.getJSONObject(0).getJSONObject("location").getJSONArray("coordinates").getDouble(1));
            return newCoordinates;
        }
        catch(Exception e) {
            return null;
        }
    }

}
