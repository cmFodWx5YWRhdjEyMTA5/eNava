package com.enavamaratha.enavamaratha.service;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import static com.enavamaratha.enavamaratha.utils.ApplicationConstants.GOOGLE_PROJ_ID;
import static com.enavamaratha.enavamaratha.utils.ApplicationConstants.displayMessage;

import com.enavamaratha.enavamaratha.R;
import com.enavamaratha.enavamaratha.activity.GcmNotification;
import com.enavamaratha.enavamaratha.activity.DBManager;
import com.enavamaratha.enavamaratha.activity.HomeActivity;
import com.enavamaratha.enavamaratha.activity.PollActivity;
import com.google.android.gms.gcm.GcmListenerService;

import java.text.SimpleDateFormat;
import java.util.Date;

public class GCMIntentService  extends GcmListenerService
{
private static final int NOTIFICATION_ID = 1;
private static int notificationCount=1;
    NotificationCompat.Builder builder;
       SQLiteDatabase db;
        public static final int notifyID = 9001;
//  private int notificationCount = 1;

    private DBManager dbManager;


  private static final String TAG = "GCMIntentService";
    String message;
    String when;
    String urltype;
    String url;


    //This method will be called on every new message received
    @Override
    public void onMessageReceived(String from, Bundle data)
    {
        Log.i(TAG, "Received message");
        dbManager = new DBManager(this);
        dbManager.open();
        //Getting the message from the bundle
        message = data.getString("message");
        urltype = data.getString("urltype");
        url = data.getString("url");

        Log.e(TAG,"Message  "+data);
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MMM-yyyy HH:mm a");
        Date date = new Date();
        when=dateFormat.format(date);
        Log.i(TAG, "Notification Message" + message);
        Log.i(TAG, "INSERT into DATABASE Query");

        if((url.contains(".")) || (url.contains("/")) )
        {
            sendNotification(message, url, urltype);
            dbManager.insert(message, when,url,urltype);
            System.out.println("Url in Not Null" + url);
        }
        else
        {
            sendNotification(message, null, urltype);
            dbManager.insert(message, when,null,null);
            System.out.println("Url in Null" + url);
        }

        System.out.println("Url in gcmnotification" + url);
        System.out.println("UrlTyp in gcm notification" + urltype);

        Log.i(TAG,"Insert into databas emanager");
        dbManager.close();
    }


    private void sendNotification(String msg,String url,String UrlType) {
        // TODO Auto-generated method stub


        // if url is not null

         if (url!=null)
        {

            if(urltype.contains("App"))
            {
                Intent resultIntent = new Intent(this, HomeActivity.class);

                resultIntent.putExtra("msg", msg);
                resultIntent.putExtra("url", url);
                resultIntent.putExtra("UrlType", UrlType);

                PendingIntent resultPendingIntent = PendingIntent.getActivity(this, 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);

                NotificationCompat.Builder mNotifyBuilder;
                NotificationManager mNotificationManager;

                mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

                mNotifyBuilder = new NotificationCompat.Builder(this)
                        .setContentTitle("eNavaMaratha")
                        .setStyle(new NotificationCompat.BigTextStyle().bigText(msg))
                        .setWhen(System.currentTimeMillis()) //
                        .setLights(0xffffffff, 0, 0)
                        .setSmallIcon(R.drawable.llogo);

                Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                mNotifyBuilder.setSound(alarmSound);
                long[] pattern = {1000, 1000, 1000};
                mNotifyBuilder.setVibrate(pattern);

                mNotifyBuilder.setContentIntent(resultPendingIntent);

                mNotifyBuilder.setContentText(msg);

                // Set autocancel
                mNotifyBuilder.setAutoCancel(true);
                // Post a notification
                mNotificationManager.notify(NOTIFICATION_ID, mNotifyBuilder.build());
            }

            else if(urltype.contains("Web"))
            {



                Intent resultIntent = new Intent(this, PollActivity.class);
                resultIntent.setAction(Intent.ACTION_MAIN);
                resultIntent.addCategory(Intent.CATEGORY_LAUNCHER);
                resultIntent.putExtra("msg", msg);
                resultIntent.putExtra("poll","Web");
                resultIntent.putExtra("Notification", url);
                resultIntent.putExtra("UrlType", UrlType);

                PendingIntent resultPendingIntent = PendingIntent.getActivity(this, 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);

                NotificationCompat.Builder mNotifyBuilder;
                NotificationManager mNotificationManager;

                mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

                mNotifyBuilder = new NotificationCompat.Builder(this)
                        .setContentTitle("eNavaMaratha")
                        .setStyle(new NotificationCompat.BigTextStyle().bigText(msg))
                        .setWhen(System.currentTimeMillis()) //
                        .setLights(0xffffffff, 0, 0)
                        .setSmallIcon(R.drawable.llogo);

                Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                mNotifyBuilder.setSound(alarmSound);
                long[] pattern = {1000, 1000, 1000};
                mNotifyBuilder.setVibrate(pattern);

                mNotifyBuilder.setContentIntent(resultPendingIntent);

                mNotifyBuilder.setContentText(msg);

                // Set autocancel
                mNotifyBuilder.setAutoCancel(true);
                // Post a notification
                mNotificationManager.notify(NOTIFICATION_ID, mNotifyBuilder.build());
            }
        }


        else
         {
             Intent resultIntent = new Intent(this, GcmNotification.class);
             resultIntent.putExtra("msg", msg);
             resultIntent.putExtra("url", "");
             resultIntent.putExtra("UrlType", "");

             System.out.println("Intente in Null" + url);

             PendingIntent resultPendingIntent = PendingIntent.getActivity(this, 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);

             NotificationCompat.Builder mNotifyBuilder;
             NotificationManager mNotificationManager;

             mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

             mNotifyBuilder = new NotificationCompat.Builder(this)
                     .setContentTitle("eNavaMaratha")
                     .setStyle(new NotificationCompat.BigTextStyle().bigText(msg))
                     .setWhen(System.currentTimeMillis()) //
                     .setLights(0xffffffff, 0, 0)
                     .setSmallIcon(R.drawable.llogo);

             Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
             mNotifyBuilder.setSound(alarmSound);
             long[] pattern = {1000, 1000, 1000};
             mNotifyBuilder.setVibrate(pattern);

             mNotifyBuilder.setContentIntent(resultPendingIntent);

             mNotifyBuilder.setContentText(msg);

             // Set autocancel
             mNotifyBuilder.setAutoCancel(true);
             // Post a notification
             mNotificationManager.notify(NOTIFICATION_ID, mNotifyBuilder.build());
         }
    }
    }


