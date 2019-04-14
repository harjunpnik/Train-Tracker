package com.example.traintracker;

import android.os.AsyncTask;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

//  AsyncTask for Api requests.
public class HTTPGet extends AsyncTask<String, Void, String>
{


    @Override
    protected String doInBackground(String... strings)
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

            return response;
        }
        catch(Exception e)
        {
            return null;
        }

    }


}
