package com.example.traintracker;

import android.content.Context;
import android.os.AsyncTask;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

//  AsyncTask for Api requests.
public class GetTrains extends AsyncTask<String, Void, JSONArray>
{
    private String startShortCode;
    private String destShortCode;
    private HashMap<String,String> stationNames;
    private Context thisContext;

    public GetTrains(String startShortCode, String destShortCode, Context thisContext){
        this.startShortCode =  startShortCode;
        this.destShortCode = destShortCode;
        this.thisContext = thisContext;
        loadShortCodeTranslatorArray();
    }

    protected void loadShortCodeTranslatorArray(){
        try {
            //  Read Json file as Array. Takes the string and converts it to JSON Array
            JSONArray jsonObj = new JSONArray(AssetReader.loadStationsFromAsset(thisContext));
            stationNames = new HashMap<String,String>();
            for(int i = 0; i < jsonObj.length(); i++){
                //System.out.println(jsonObj.getJSONObject(i).getString("stationName"));
                stationNames.put(jsonObj.getJSONObject(i).getString("stationShortCode"),jsonObj.getJSONObject(i).getString("stationName"));
            }
            //  Creates adapter and sets it to the AutoComplete lists
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected JSONArray doInBackground(String... strings)
    {
        String response = new String();

        try
        {
            URL url = new URL(strings[0]);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            BufferedReader reader = new BufferedReader(new InputStreamReader(con.getInputStream()));

            String nextLine = new String();

            while ((nextLine = reader.readLine()) != null)
            {
                response += nextLine;
            }

            JSONArray responseJson = new JSONArray(response);
            return responseJson;
        }
        catch(Exception e)
        {
            return null;
        }

    }


    @Override
    protected void onPostExecute(JSONArray result)
    {
        if(result != null) {
            sortData(result);
        }
    }

    protected void sortData(JSONArray result){

        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ISO_ZONED_DATE_TIME;
        DateTimeFormatter houtMinuteFormatter = DateTimeFormatter.ofPattern("HH:mm");

        try {
            for (int i = 0; i < result.length(); i++) {
                //TODO MEBE CHECK IF CANCELLED???
                String trainType = result.getJSONObject(i).getString("trainType");
                int trainNumber = result.getJSONObject(i).getInt("trainNumber");
                String startName = "";
                String destinationName = "";
                String startTime = "";
                String arrivalTime = "";
                JSONArray timeTableRows = result.getJSONObject(i).getJSONArray("timeTableRows");
                for(int j = 0; j < timeTableRows.length(); j++ ){

                    if(startShortCode.equals(timeTableRows.getJSONObject(j).getString("stationShortCode")) && timeTableRows.getJSONObject(j).getString("type").equals("DEPARTURE") && timeTableRows.getJSONObject(j).getBoolean("trainStopping")){
                        startName = stationNames.get(timeTableRows.getJSONObject(j).getString("stationShortCode"));
                        String tempTime = result.getJSONObject(i).getJSONArray("timeTableRows").getJSONObject(0).getString("scheduledTime");

                        ZonedDateTime zoneStartTime =  ZonedDateTime.parse(tempTime, dateTimeFormatter);

                        startTime = zoneStartTime.format(houtMinuteFormatter);
                    }

                    if( destShortCode.equals(timeTableRows.getJSONObject(j).getString("stationShortCode")) && timeTableRows.getJSONObject(j).getString("type").equals("ARRIVAL") && timeTableRows.getJSONObject(j).getBoolean("trainStopping")){
                        destinationName = stationNames.get(timeTableRows.getJSONObject(j).getString("stationShortCode"));
                        String tempTime = result.getJSONObject(i).getJSONArray("timeTableRows").getJSONObject(0).getString("scheduledTime");

                        ZonedDateTime zoneStartTime =  ZonedDateTime.parse(tempTime, dateTimeFormatter);

                        arrivalTime = zoneStartTime.format(houtMinuteFormatter);
                    }

                }
                //startName = result.getJSONObject(i).getJSONArray("timeTableRows").getJSONObject(0).getString("stationShortCode");
                //SimpleDateFormat dateFormatter = new SimpleDateFormat("HH:mm");
                //Date startDate = dateFormatter.parse(startTime);
                //LocalDateTime startTime = LocalDateTime.parse(tempStartTime, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.S"));
                //ZonedDateTime zoneTime = temp.atZone(ZoneId.of("Europe/Helsinki"));
                //TODO MEBE CHECK IF commercial stop???
                //String testForStopping = result.getJSONObject(i).getJSONArray("timeTableRows").getJSONObject(0).getString("trainStopping");
                //String type = result.getJSONObject(i).getJSONArray("timeTableRows").getJSONObject(0).getString("type");
                //TODO ADD TO HASHMAP OR ARRAYLIST
                if(!startName.isEmpty() && !destinationName.isEmpty() )
                    System.out.println(trainType + " " + trainNumber + ", " + startName + " " + startTime  + " " + destinationName + " " + arrivalTime);


                //System.out.println(startDate);
                //System.out.println(trainType + " " + trainNumber + ", " + startName + " " + startTime  + " type " + type + " " + testForStopping);//
            }

            //TODO CALL METHOD TO UPDATE RECYCLE VIEW

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

}
