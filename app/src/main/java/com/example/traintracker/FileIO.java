package com.example.traintracker;

import android.app.Activity;
import android.content.Context;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;

//  File handler Class that reads and saves HashMap<String,Train> to internal memory
public class FileIO
{
    private static String fileName = "trains";

    //  Load from file function. Returns new empty list if no file was found
    public static HashMap<String, Train> loadTrains(Activity activity) {

        HashMap<String, Train> trains = new HashMap<>();

        try {
            FileInputStream fIn = activity.openFileInput(fileName);
            ObjectInputStream inObj = new ObjectInputStream(fIn);
            trains = (HashMap<String, Train>) inObj.readObject();
            inObj.close();
            fIn.close();
        }

        catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        return trains;
    }

    // Save to file function.
    public static void saveTrains(HashMap<String, Train> routes, Activity activity) {

        try {
            FileOutputStream fOut = activity.openFileOutput(fileName, Context.MODE_PRIVATE);
            ObjectOutputStream outObj = new ObjectOutputStream(fOut);
            outObj.writeObject(routes);
            outObj.close();
            fOut.close();
        }

        catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        catch (IOException e) {
            e.printStackTrace();
        }
    }
}
