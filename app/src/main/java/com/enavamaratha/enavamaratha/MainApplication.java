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

    public static Context getContext() {
        return mContext;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = getApplicationContext();

        PrefUtils.putBoolean(PrefUtils.IS_REFRESHING, false); // init


        Picasso.Builder builder = new Picasso.Builder(this);
        long cacheMaxSize = 180 * 1024 * 1024; // 180 MB

        OkHttpClient okHttpClient = new OkHttpClient();

        File customCacheDirectory = new File(getFilesDir(), "/Epaper/");

        if (!customCacheDirectory.exists()) {
            customCacheDirectory.mkdir();


        }

        okHttpClient.setCache(new Cache(customCacheDirectory, cacheMaxSize));


        OkHttpDownloader okHttpDownloader = new OkHttpDownloader(okHttpClient);

        //Picasso picasso = new Picasso.Builder(getApplicationContext()).downloader(okHttpDownloader).build();
        builder.downloader(okHttpDownloader);
        Picasso built = builder.build();
        // for showing indicators of images
        //  built.setIndicatorsEnabled(true);
        //built.setLoggingEnabled(true);
        Picasso.setSingletonInstance(built);


    }

}


