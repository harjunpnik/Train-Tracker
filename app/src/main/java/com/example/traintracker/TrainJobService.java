package com.example.traintracker;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.os.AsyncTask;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;

import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;

public class TrainJobService extends JobService {
    private static final String TAG = "TrainJobService";
    private boolean jobCancelled = false;

    private NotificationManagerCompat norificationManager;

    private int trainNumber;
    private String trainStartNameShortCode;

    DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ISO_ZONED_DATE_TIME;
    DateTimeFormatter hourMinuteFormatter = DateTimeFormatter.ofPattern("HH:mm").withZone(ZoneId.of("Europe/Helsinki"));

    @Override
    public boolean onStartJob(JobParameters params) {
        trainNumber = params.getExtras().getInt("trainNumber");
        trainStartNameShortCode = params.getExtras().getString("trainStartNameShortCode");
        norificationManager = NotificationManagerCompat.from(this);



        Log.d(TAG, "Job Started");
        //ASYNC TASK CHECKS ETC
        doBackgroundWork(params);

        return true;
    }

    private void doBackgroundWork(JobParameters params){

        //TrainScheduledTime trainTime = new TrainScheduledTime(trainNumber, trainStartName);
        String leavingTime = "";
        try {
             leavingTime = new TrainScheduledTime(trainNumber, trainStartNameShortCode).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR).get();

            ZonedDateTime departureTime =  ZonedDateTime.parse(leavingTime, dateTimeFormatter);
            checkTime(departureTime);
            //String departureTime = zoneDestTime.format(hourMinuteFormatter);
            //leavingTime = trainTime.execute().get();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Notification notification = notificationMaker("TRAIN IS LATE " + trainStartNameShortCode + " " + trainNumber, "Train is comming at this time " + leavingTime);


        norificationManager.notify(1, notification);
        jobFinished(params, false);
    }

    private void checkTime(ZonedDateTime departureTime){
        System.out.println(Duration.between(departureTime , ZonedDateTime.now()));
        //System.out.println(ZonedDateTime.now() - departureTime >= 20*60*1000);

        if(departureTime == null){
            JobScheduler scheduler = (JobScheduler) getSystemService(JOB_SCHEDULER_SERVICE);
            //TODO CANCEL ONLY CERTAIN TRAIN
            scheduler.cancelAll();
        }else if(departureTime.isBefore(ZonedDateTime.now())){
            JobScheduler scheduler = (JobScheduler) getSystemService(JOB_SCHEDULER_SERVICE);
            //TODO slap this in or statement with above, but first double check if it works
            scheduler.cancelAll();
        }else if(departureTime.isAfter(ZonedDateTime.now())){

        }

        System.out.println(departureTime == null);
        System.out.println(" some time to check " + departureTime.isAfter(ZonedDateTime.now()));


    }

    private Notification notificationMaker(String titleText, String contentText){

        Notification notification = new NotificationCompat.Builder(this, App.CHANNEL_1_ID)
                .setSmallIcon(R.drawable.ic_train)
                .setContentTitle(titleText)
                .setContentText(contentText )
                .build();

        return notification;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        Log.d(TAG, "Job Ended");
        return false;
    }
}
