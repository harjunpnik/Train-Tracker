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
    private MainActivity ma;
    private String startShortCode;
    private String destShortCode;
    private HashMap<String,String> stationNames;
    private Context thisContext;

    public GetTrains(MainActivity ma, String startShortCode, String destShortCode, Context thisContext){
        this.ma = ma;
        this.startShortCode =  startShortCode;
        this.destShortCode = destShortCode;
        this.thisContext = thisContext;
        loadShortCodeTranslatorArray();
    }

    protected void loadShortCodeTranslatorArray(){
        try {
            //  Read Json file as Array. Takes the string and converts it to JSON Array
            JSONArray jsonObj = new JSONArray(AssetReader.loadStationsFromAsset(thisContext));
            stationNames = new HashMap<>();
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
        DateTimeFormatter houtMinuteFormatter = DateTimeFormatter.ofPattern("HH:mm").withZone(ZoneId.of("Europe/Helsinki"));
        ArrayList<Train> trains = new ArrayList();


        try {
            for (int i = 0; i < result.length(); i++) {
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
                        String tempstartTime = result.getJSONObject(i).getJSONArray("timeTableRows").getJSONObject(j).getString("scheduledTime");

                        ZonedDateTime zoneStartTime =  ZonedDateTime.parse(tempstartTime, dateTimeFormatter);

                        startTime = zoneStartTime.format(houtMinuteFormatter);
                    }

                    if( destShortCode.equals(timeTableRows.getJSONObject(j).getString("stationShortCode")) && timeTableRows.getJSONObject(j).getString("type").equals("ARRIVAL") && timeTableRows.getJSONObject(j).getBoolean("trainStopping")){
                        destinationName = stationNames.get(timeTableRows.getJSONObject(j).getString("stationShortCode"));
                        String tempDestTime = result.getJSONObject(i).getJSONArray("timeTableRows").getJSONObject(j).getString("scheduledTime");

                        ZonedDateTime zoneDestTime =  ZonedDateTime.parse(tempDestTime, dateTimeFormatter);

                        arrivalTime = zoneDestTime.format(houtMinuteFormatter);
                    }

                }

                if(!startName.isEmpty() && !destinationName.isEmpty() ){
                    trains.add(new Train(trainNumber, trainType, startName, destinationName, startTime, arrivalTime));
                }
            }

            ma.setRecyclerView(trains);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

}
