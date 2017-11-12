package com.enavamaratha.enavamaratha;

import android.app.Application;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import com.enavamaratha.enavamaratha.provider.FeedData;
import com.squareup.okhttp.Cache;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.picasso.OkHttpDownloader;
import com.squareup.picasso.Picasso;

import com.enavamaratha.enavamaratha.provider.FeedDataContentProvider;
import com.enavamaratha.enavamaratha.utils.PrefUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

public class MainApplication extends Application {

    private static Context mContext;
    //http://localhost/NavaMaratha/category/headlines/feed/


    //final String ur="http://192.168.1.21/NavaMaratha/category/headlines/feed/?orderby=modified";
    // demo
    // final String ur="http://web1.abmra.in/category/headlines/feed";
    final String ur = "http://web1.abmra.in/category/headlines/feed/?orderby=modified";
    final String url = "http://web1.abmra.in/category/econo/feed/?orderby=modified";
    final String url1 = "http://web1.abmra.in/category/health/feed/?orderby=modified";
    final String url2 = "http://web1.abmra.in/category/scien/feed/?orderby=modified";
    final String url4 = "http://web1.abmra.in/category/entertainment/feed/?orderby=modified";
    final String url5 = "http://web1.abmra.in/category/religious/feed/?orderby=modified";
    final String url6 = "http://web1.abmra.in/category/astro/feed/?orderby=modified";
    final String url7 = "http://web1.abmra.in/category/meet/feed/?orderby=modified";
    final String url8 = "http://web1.abmra.in/category/tourist/feed/?orderby=modified";
    final String url9 = "http://web1.abmra.in/category/home/feed/?orderby=modified";
    final String url10 = "http://web1.abmra.in/category/recepi/feed/?orderby=modified";
    final String url11 = "http://web1.abmra.in/category/child/feed/?orderby=modified";
    final String url12 = "http://web1.abmra.in/category/info/feed/?orderby=modified";
    final String url13 = "http://web1.abmra.in/category/thoughts/feed/?orderby=modified";
    final String url14 = "http://web1.abmra.in/category/jobs/feed/?orderby=modified";
    final String url15 = "http://web1.abmra.in/category/property/feed/?orderby=modified";

    // add  other menus in navigation drawer like poll,settings,feedback etc.


    final String nam = "ठळक बातम्या";
    final String name = "अर्थकारण";
    final String name1 = "आरोग्य";
    final String name2 = "विज्ञान";
    final String name4 = "मनोरंजन";
    final String name5 = "आत्मधन";
    final String name6 = "राशिभविष्य ";
    final String name7 = "मुलाखत";
    final String name8 = "पर्यटन";
    final String name9 = " वास्तू ";
    final String name10 = "पाककला ";
    final String name11 = " मुलांचे विषय ";
    final String name12 = "सामन्य ज्ञान";
    final String name13 = " सुविचार ";
    final String name14 = "नोकरी विषयी";
    final String name15 = "प्रॉप्रटी";


    final boolean pls = true;
    private static final String DATABASE_NAME = "FeedEx.db";
    private static final String NOTIFICATION_DATABASE = "MyMb";

    public static Context getContext() {
        return mContext;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = getApplicationContext();

        PrefUtils.putBoolean(PrefUtils.IS_REFRESHING, false); // init
      //  final TypedArray selectedValues = getResources().obtainTypedArray(R.array.settings_keep_time_values);
//        final Integer keepTime = selectedValues.getInt(mKeepTime.getSelectedItemPosition(),0);


       /* FeedDataContentProvider.addFeed(MainApplication.this, ur, nam, pls,"","",0);
        FeedDataContentProvider.addFeed(MainApplication.this, url, name, pls,"","",0);
        FeedDataContentProvider.addFeed(MainApplication.this, url1, name1, pls,"","",0);
        FeedDataContentProvider.addFeed(MainApplication.this, url2, name2, pls,"","",0);
        FeedDataContentProvider.addFeed(MainApplication.this, url4, name4, pls,"","",0);
        FeedDataContentProvider.addFeed(MainApplication.this, url5, name5, pls,"","",0);
        FeedDataContentProvider.addFeed(MainApplication.this, url6, name6, pls,"","",0);
        FeedDataContentProvider.addFeed(MainApplication.this, url7, name7, pls,"","",0);
        FeedDataContentProvider.addFeed(MainApplication.this, url8, name8, pls,"","",0);
        FeedDataContentProvider.addFeed(MainApplication.this, url9, name9, pls,"","",0);
        FeedDataContentProvider.addFeed(MainApplication.this, url10, name10, pls,"","",0);
        FeedDataContentProvider.addFeed(MainApplication.this, url11, name11, pls,"","",0);
        FeedDataContentProvider.addFeed(MainApplication.this, url12, name12, pls,"","",0);
        FeedDataContentProvider.addFeed(MainApplication.this, url13, name13, pls,"","",0);
        FeedDataContentProvider.addFeed(MainApplication.this, url14, name14, pls,"","",0);
        FeedDataContentProvider.addFeed(MainApplication.this, url15, name15, pls,"","",0);*/

      //  System.out.println("Add rss Feed");
       // exportDB();


       // System.out.println("Add Menu in Navigation");


        //Picasso Coding


        Picasso.Builder builder = new Picasso.Builder(this);
        long cacheMaxSize = 180 * 1024 * 1024; // 180 MB

       // Log.i("Value of cache size", "MB:" + cacheMaxSize);

        OkHttpClient okHttpClient = new OkHttpClient();

        File customCacheDirectory = new File(getFilesDir(), "/Epaper/");

        if (!customCacheDirectory.exists()) {
            customCacheDirectory.mkdir();
          //  Log.i("Global", "Create directory" + customCacheDirectory);

        }

        okHttpClient.setCache(new Cache(customCacheDirectory, cacheMaxSize));
       // Log.i("Global", "Set Cache" + customCacheDirectory);

        OkHttpDownloader okHttpDownloader = new OkHttpDownloader(okHttpClient);

        //Picasso picasso = new Picasso.Builder(getApplicationContext()).downloader(okHttpDownloader).build();
        builder.downloader(okHttpDownloader);
        Picasso built = builder.build();
        // for showing indicators of images
        //  built.setIndicatorsEnabled(true);
        //built.setLoggingEnabled(true);
        Picasso.setSingletonInstance(built);


    }


    private void exportDB()
    {
        File sd = Environment.getExternalStorageDirectory();
        File data = Environment.getDataDirectory();
        FileChannel source=null;
        FileChannel destination=null;
       String currentDBPath = "/data/"+ "com.enavamaratha.enavamaratha" +"/databases/"+DATABASE_NAME;
        String currentD ="/data/"+"com.enavamaratha.enavamaratha"+"/databases/"+NOTIFICATION_DATABASE;
       String backupDBPath =DATABASE_NAME;
        String backupD = NOTIFICATION_DATABASE;
        File currentDB = new File(data, currentDBPath);
        File CurrentDB = new File(data,currentD);
        File backupDB = new File(sd, backupDBPath);
        File BackupDB= new File(sd,backupD);
        try {
           source = new FileInputStream(currentDB).getChannel();
           destination = new FileOutputStream(backupDB).getChannel();
            destination.transferFrom(source, 0, source.size());

            source = new FileInputStream(CurrentDB).getChannel();
            destination = new FileOutputStream(BackupDB).getChannel();
            destination.transferFrom(source,0,source.size());
            source.close();
            destination.close();
           // Toast.makeText(this, "DB Exported!", Toast.LENGTH_LONG).show();
        } catch(IOException e) {
            e.printStackTrace();
        }
    }
}


