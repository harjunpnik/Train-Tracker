package com.example.traintracker;

import android.content.Context;

import java.io.IOException;
import java.io.InputStream;

public class AssetReader {

    //  https://stackoverflow.com/questions/13814503/reading-a-json-file-in-android
    //  The code from StackOverflow was used. Reads stations.json and returns a string of the file in UTF-8.
    public static String loadStationsFromAsset(Context context) {
        String json = null;
        try {
            InputStream inStream = context.getAssets().open("stations.json");

            int size = inStream.available();

            byte[] buffer = new byte[size];

            inStream.read(buffer);

            inStream.close();

            json = new String(buffer, "UTF-8");


        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        return json;

    }
}
