package com.enavamaratha.enavamaratha.service;


import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.Log;

import com.enavamaratha.enavamaratha.R;
import com.enavamaratha.enavamaratha.activity.DBManager;
import com.enavamaratha.enavamaratha.activity.EpaperPdfActivity;
import com.enavamaratha.enavamaratha.activity.GcmNotification;
import com.enavamaratha.enavamaratha.activity.HomeActivity;
import com.enavamaratha.enavamaratha.activity.PollActivity;
import com.enavamaratha.enavamaratha.utils.GetePaperUrlDateFormat;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;

import static com.enavamaratha.enavamaratha.utils.ApplicationConstants.NOTIFICATION_ID;
import static com.enavamaratha.enavamaratha.utils.ApplicationConstants.REG_ID;
import static com.enavamaratha.enavamaratha.utils.ApplicationConstants.USER_DETAILS;

public class MyFirebaseMessagingService extends FirebaseMessagingService {


    private static final String TAG = MyFirebaseMessagingService.class.getSimpleName();
    private DBManager dbManager;

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {


        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0) {


            try {
                JSONObject json = new JSONObject(remoteMessage.getData());


                handleDataMessage(json);
            } catch (Exception e) {


            }

        }

        // Check if message contains a notification payload.
        if (remoteMessage.getNotification() != null) {


        }


    }
// [END receive_message]


    private void handleDataMessage(JSONObject json) {


        try {


            String message = json.getString("message");

            String mUrl = "";
            String mUrlType = "";
            String mNotificationHeading = "";

            // If Data Contain Url
            if (json.has("url")) {
                mUrl = json.getString("url");
            }

            // If Data contain UrlType
            if (json.has("urltype")) {
                mUrlType = json.getString("urltype");
            }


            if (json.has("Title")) {
                mNotificationHeading = json.getString("Title");
            }


            // If dyanmic Notificaiton heading is null then set hardcoded title
            if (mNotificationHeading == null) {
                mNotificationHeading = "eNavaMaratha";
            }


            dbManager = new DBManager(this);
            dbManager.open();


            SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MMM-yyyy HH:mm a");
            Date date = new Date();
            String when = dateFormat.format(date);

            SharedPreferences prefs = getSharedPreferences(USER_DETAILS, Context.MODE_PRIVATE);
            String registrationId = prefs.getString(REG_ID, "");


            // registrationId is not empty in Preference then show notifications and insert in Database
            if (!TextUtils.isEmpty(registrationId)) {


                if ((mUrl.contains(".")) || (mUrl.contains("/"))) {
                    dbManager.insert(message, when, mUrl, mUrlType);
                    sendNotification(message, mUrl, mUrlType, mNotificationHeading);


                } else {
                    dbManager.insert(message, when, null, null);
                    sendNotification(message, null, mUrlType, mNotificationHeading);


                }
            }

            // registrationId is empty in Preference then Don't show notifications and not store in database
            else {

                // Do Nothing if Register id is empty
            }

            dbManager.close();


        } catch (JSONException e) {


        } catch (Exception e) {

        }


    }


    private void sendNotification(String msg, String url, String urltype, String NotificationTitle) {
        // TODO Auto-generated method stub


        // if url is not null

        if (url != null) {


            // If Url type is App then Open News if Available
            // Else Only Open Home Activity
            if (urltype.contains("App")) {
                Intent resultIntent = new Intent(this, HomeActivity.class);

                resultIntent.putExtra("msg", msg);
                resultIntent.putExtra("url", url);
                resultIntent.putExtra("UrlType", urltype);


                sendNotifications(resultIntent, NotificationTitle, msg);
            }

            // If Urltype is Web then open that url in Webview
            else if (urltype.contains("Web")) {


                Intent resultIntent = new Intent(this, PollActivity.class);
                resultIntent.setAction(Intent.ACTION_MAIN);
                resultIntent.addCategory(Intent.CATEGORY_LAUNCHER);
                resultIntent.putExtra("msg", msg);
                resultIntent.putExtra("poll", "Web");
                resultIntent.putExtra("Notification", url);
                resultIntent.putExtra("UrlType", urltype);


                sendNotifications(resultIntent, NotificationTitle, msg);

            }


            // If UrlType is Epaper
            // Pass Epaper Date to Epaper Pdf Activity
            // For open ePaper Activity --- then Download That epaper then view that ePaper
            else if (urltype.contains("ePaper")) {

                // Send Date to ePaperPdfActivity as a Intent Extra
                // To Download that date pdf and view
                GetePaperUrlDateFormat getDate = new GetePaperUrlDateFormat();

                Intent resultIntent = new Intent(this, EpaperPdfActivity.class);
                resultIntent.putExtra("date", getDate.ePaperPdfUrl(url));
                resultIntent.putExtra("pdf", getDate.ePaperPdfName(url));


                sendNotifications(resultIntent, NotificationTitle, msg);

            }
        } else {
            Intent resultIntent = new Intent(this, GcmNotification.class);
            resultIntent.putExtra("msg", msg);
            resultIntent.putExtra("url", "");
            resultIntent.putExtra("UrlType", "");


            sendNotifications(resultIntent, NotificationTitle, msg);
        }
    }


    // Check Android Os Version And As Per That Change Notification Icon
    // For Below Marshmallow Apply Simple Logo and
    // Above Marshmallow Transparent png image Required
    private int getNotificationIcon() {
        boolean useWhiteIcon = (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP);
        return useWhiteIcon ? R.drawable.ic_notification_name : R.drawable.llogo;
    }


    private void sendNotifications(Intent resultIntent, String NotificationTitle, String msg) {


        PendingIntent resultPendingIntent = PendingIntent.getActivity(this, 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder mNotifyBuilder;
        NotificationManager mNotificationManager;

        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        Bitmap largeIcon = BitmapFactory.decodeResource(getResources(), R.drawable.llogo);
        mNotifyBuilder = new NotificationCompat.Builder(this)
                .setContentTitle(NotificationTitle)
                .setWhen(System.currentTimeMillis()) //
                .setLights(0xffffffff, 0, 0)
                .setContentText(msg)

                .setAutoCancel(true)
                .setSmallIcon(getNotificationIcon());


        // Set Large Icon Above Marshmallow
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            mNotifyBuilder.setPriority(Notification.PRIORITY_MAX);
            mNotifyBuilder.setLargeIcon(largeIcon);
        }


        // Set Color Above Marshmallow
        // Set Notification Priority
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            mNotifyBuilder.setColor(ContextCompat.getColor(getApplicationContext(), R.color.white));

        }


        Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        mNotifyBuilder.setSound(alarmSound);
        long[] pattern = {1000, 1000, 1000};
        mNotifyBuilder.setVibrate(pattern);

        mNotifyBuilder.setContentIntent(resultPendingIntent);

        // Post a notification
        mNotificationManager.notify(NOTIFICATION_ID, mNotifyBuilder.build());
    }


}



