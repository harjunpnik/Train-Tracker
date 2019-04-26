package com.example.traintracker;

import android.os.AsyncTask;

import org.json.JSONArray;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class TrainScheduledTime extends AsyncTask <String, Void, String> {

    private int trainNumber;
    private String trainStartName;

    public TrainScheduledTime (int trainNumber, String trainStartName){
        this.trainNumber = trainNumber;
        this.trainStartName = trainStartName;
    }

    @Override
    protected String doInBackground(String... strings) {
        //  AsyncTask for Api request
        String response = new String();
        String time = null;

        try {
            //  Search for estimated time of departure and return it in String format
            String searchUrl = "https://rata.digitraffic.fi/api/v1/trains/latest/" + trainNumber;
            URL url = new URL(searchUrl);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            BufferedReader reader = new BufferedReader(new InputStreamReader(con.getInputStream()));

            String nextLine = new String();

            while ((nextLine = reader.readLine()) != null) {
                response += nextLine;
            }

            JSONArray timeTableRows = new JSONArray(response).getJSONObject(0).getJSONArray("timeTableRows");

            //  Searches for the right station and station to be departure
            for( int i = 0; i < timeTableRows.length(); i++){
                if(trainStartName.equals(timeTableRows.getJSONObject(i).getString("stationShortCode")) && timeTableRows.getJSONObject(i).getString("type").equals("DEPARTURE") ){
                    time = timeTableRows.getJSONObject(i).getString("scheduledTime");
                    break;
                }
            }
            //  Time is by default null
            return time;
        }
        catch(Exception e) { return null; }
    }

}
