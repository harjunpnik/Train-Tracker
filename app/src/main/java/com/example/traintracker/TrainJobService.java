package com.example.traintracker;

import android.app.Notification;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.os.AsyncTask;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ExecutionException;

public class TrainJobService extends JobService {
    private static final String TAG = "TrainJobService";

    private NotificationManagerCompat norificationManager;
    private int trainNumber;
    private String trainStartNameShortCode;
    private String trainDestNameShortCode;
    private DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ISO_ZONED_DATE_TIME;
    private DateTimeFormatter hourMinuteFormatter = DateTimeFormatter.ofPattern("HH:mm").withZone(ZoneId.of("Europe/Helsinki"));

    @Override
    public boolean onStartJob(JobParameters params) {
        trainNumber = params.getExtras().getInt("trainNumber");
        trainStartNameShortCode = params.getExtras().getString("trainStartNameShortCode");
        trainDestNameShortCode = params.getExtras().getString("trainDestNameShortCode");
        norificationManager = NotificationManagerCompat.from(this);
        Log.d(TAG, "Job Started");

        doBackgroundWork(params);
        return true;
    }


    private void doBackgroundWork(JobParameters params){

        try {
            //  Get Leaving time string, returns null by default
            String leavingTime = new TrainScheduledTime(trainNumber, trainStartNameShortCode).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR).get();

            ZonedDateTime departureTime =  ZonedDateTime.parse(leavingTime, dateTimeFormatter);

            Notification notification = checkTime(departureTime);
            if(notification != null) {
                norificationManager.notify(1, notification);
            }
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        jobFinished(params, false);
    }

    //  Compares train leaving time to time now and sends notification based on the time for train departure
    private Notification checkTime(ZonedDateTime departureTime){
        Notification notification = null;

        //  If null, cancel JobScheduler
        if(departureTime == null){
            JobScheduler scheduler = (JobScheduler) getSystemService(JOB_SCHEDULER_SERVICE);
            scheduler.cancelAll();
        //  If train has left, cancel JobScheduler
        }else if(departureTime.isBefore(ZonedDateTime.now())){
            JobScheduler scheduler = (JobScheduler) getSystemService(JOB_SCHEDULER_SERVICE);
            scheduler.cancelAll();
        //  If train has not left
        }else if(departureTime.isAfter(ZonedDateTime.now())){
            Duration duration = Duration.between( ZonedDateTime.now(), departureTime );
            // and duration is less than 75 minutes
            if(duration.getSeconds() < (75 *60)){
                String departureTimeHHmm = departureTime.format(hourMinuteFormatter);

                if(duration.getSeconds() < (60 *60)){ // and duration is less than 60 minutes


                    if(duration.getSeconds() < (45 *60)){// and duration is less than 45 minutes

                        if(duration.getSeconds() < (30 *60)){// and duration is less than 30 minutes

                            if(duration.getSeconds() < (15 *60)){// and duration is less than 15 minutes
                                // Else send notification for train to leave between 15 and 0 minutes
                                notification = notificationMaker("TRAIN " + trainNumber + " IS LEAVING IN UNDER 15 MINUTES"  , "Train " + trainNumber + " " + trainStartNameShortCode + "-" + trainDestNameShortCode + ", is leaving at " + departureTimeHHmm );

                            }else{// Else send notification for train to leave between 30 and 15 minutes
                                notification = notificationMaker("TRAIN " + trainNumber + " IS LEAVING IN UNDER 30 MINUTES"  , "Train " + trainNumber + " " + trainStartNameShortCode + "-" + trainDestNameShortCode + ", is leaving at " + departureTimeHHmm );
                            }
                        }else{// Else send notification for train to leave between 45 and 30 minutes
                            notification = notificationMaker("TRAIN " + trainNumber + " IS LEAVING IN UNDER 45 MINUTES" , "Train " + trainNumber + " " + trainStartNameShortCode + "-" + trainDestNameShortCode + ", is leaving at " + departureTimeHHmm );
                        }
                    }else{// Else send notification for train to leave between 60 and 45 minutes
                        notification = notificationMaker("TRAIN " + trainNumber + " IS LEAVING IN UNDER AN HOUR" , "Train " + trainNumber + " " + trainStartNameShortCode + "-" + trainDestNameShortCode + ", is leaving at " + departureTimeHHmm );
                    }
                }else{// Else send notification for train to leave between 75 and 60 minutes
                    notification = notificationMaker("TRAIN " + trainNumber + " IS LEAVING IN ABOUT AN HOUR" , "Train " + trainNumber + " " + trainStartNameShortCode + "-" + trainDestNameShortCode + ", is leaving at " + departureTimeHHmm );
                }

            }




        }

        return notification;
    }

    //  Creates notification
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
