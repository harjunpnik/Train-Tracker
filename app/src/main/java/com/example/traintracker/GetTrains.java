package com.example.traintracker;

import android.content.Context;
import android.os.AsyncTask;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
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

    //  Loads in array for translating purposes
    protected void loadShortCodeTranslatorArray(){
        try {
            //  Read Json file as Array. Takes the string and converts it to JSON Array
            //  This is HashMap is used for converting ShortCodes from the API to the full station names.
            JSONArray jsonObj = new JSONArray(AssetReader.loadStationsFromAsset(thisContext));
            stationNames = new HashMap<>();
            for(int i = 0; i < jsonObj.length(); i++){
                stationNames.put(jsonObj.getJSONObject(i).getString("stationShortCode"),jsonObj.getJSONObject(i).getString("stationName"));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    protected void onPreExecute() {
        //  Sets Loading visible and hides ListView
        ma.setLoadingVisibility(true);
    }

    @Override
    protected JSONArray doInBackground(String... strings) {
        //  AsyncTask for Api request
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
    protected void onPostExecute(JSONArray result) {
        if(result != null) {
            sortData(result);
        }else{
            ma.setLoadingVisibility(false);
            ma.toastError("No trains are running between the stations");
        }
    }

    private void sortData(JSONArray result){

        //  TimeFormatters to Helsinki Time
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ISO_ZONED_DATE_TIME;
        DateTimeFormatter hourMinuteFormatter = DateTimeFormatter.ofPattern("HH:mm").withZone(ZoneId.of("Europe/Helsinki"));
        //  New Lists for trains. ArrayList is used for list view and HashMap is saved and accessed later for fetching base information for MapsActivity
        ArrayList<Train> trains = new ArrayList<>();
        HashMap<String, Train> trainsHashMap = new HashMap<>();


        try {
            //  Loop the response to find all the trains that stop
            for (int i = 0; i < result.length(); i++) {

                //  Assign base variables
                String trainType = result.getJSONObject(i).getString("trainType");
                int trainNumber = result.getJSONObject(i).getInt("trainNumber");
                String startTime = "";
                String arrivalTime = "";
                //  Reset startName and destinationName to "empty"
                String startName = "";
                String destinationName = "";

                //  All the arrivals and departures for the current train
                JSONArray timeTableRows = result.getJSONObject(i).getJSONArray("timeTableRows");
                for(int j = 0; j < timeTableRows.length(); j++ ){

                    //  If current object has same name as start & current object is "DEPARTURE" & train is stopping on station
                    if(startShortCode.equals(timeTableRows.getJSONObject(j).getString("stationShortCode")) && timeTableRows.getJSONObject(j).getString("type").equals("DEPARTURE") && timeTableRows.getJSONObject(j).getBoolean("trainStopping")){
                        //  Save variables
                        startName = stationNames.get(timeTableRows.getJSONObject(j).getString("stationShortCode"));
                        String tempstartTime = result.getJSONObject(i).getJSONArray("timeTableRows").getJSONObject(j).getString("scheduledTime");
                        ZonedDateTime zoneStartTime =  ZonedDateTime.parse(tempstartTime, dateTimeFormatter);
                        startTime = zoneStartTime.format(hourMinuteFormatter);
                    }

                    //  If current object has same name as destination & current object is "ARRIVAL" & train is stopping on station
                    if( destShortCode.equals(timeTableRows.getJSONObject(j).getString("stationShortCode")) && timeTableRows.getJSONObject(j).getString("type").equals("ARRIVAL") && timeTableRows.getJSONObject(j).getBoolean("trainStopping")){
                        //  Save variables
                        destinationName = stationNames.get(timeTableRows.getJSONObject(j).getString("stationShortCode"));
                        String tempDestTime = result.getJSONObject(i).getJSONArray("timeTableRows").getJSONObject(j).getString("scheduledTime");
                        ZonedDateTime zoneDestTime =  ZonedDateTime.parse(tempDestTime, dateTimeFormatter);
                        arrivalTime = zoneDestTime.format(hourMinuteFormatter);
                    }

                    //  If both start and destination has been found, add to lists and break inner loop
                    if(!startName.isEmpty() && !destinationName.isEmpty() ){
                        trains.add(new Train(trainNumber, trainType, startName, destinationName, startTime, arrivalTime));
                        trainsHashMap.put(trains.get(trains.size()-1).getNameFormated(),trains.get(trains.size()-1));
                        break;
                    }
                }


            }

            //  SetsRecyclerView
            ma.setLoadingVisibility(false);
            ma.setListView(trains);
            //  Save Trains for MapsActivity
            FileIO.saveTrains(trainsHashMap, ma);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

}
