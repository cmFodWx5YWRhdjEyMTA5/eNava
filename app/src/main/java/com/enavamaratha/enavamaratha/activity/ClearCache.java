package com.enavamaratha.enavamaratha.activity;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.Toast;

import com.enavamaratha.enavamaratha.R;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ClearCache extends AppCompatActivity
{
    Context context ;
    SQLiteDatabase db;
    String mytable1="mytable1";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_clear_cache);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        context=getApplicationContext();

        db= openOrCreateDatabase("MyMb", MODE_PRIVATE, null);

        String getintentvalue="myMethod";

        String simple=getIntent().getStringExtra("methodName");
        Log.i("Intent in clear","Intent Value"+simple);
        if(getintentvalue.equals( getIntent().getStringExtra("methodName")))
        {

            if((isTableExists(mytable1)) == true)
            {
               db.execSQL("delete from mytable1");
               // Log.i("Table is exists", "TAble is exist");

                // update cache table with current date
                final String ROWID = "id";
                SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
                String currentDateandTime = sdf.format(new Date());

                ContentValues args = new ContentValues();
                args.put("time",currentDateandTime );
                int updatev = db.update("cache", args, ROWID + "=" +1, null);
               // Log.i("Updated Value is :", "" + updatev);

                // call clear method
                clear();
            }


            else
            {
                //Toast.makeText(context, "Your All Cache is clear", Toast.LENGTH_SHORT).show();
                Intent i = new Intent(getApplicationContext(), GeneralPrefsActivity.class);
                startActivity(i);
                System.out.println("TAble is not exist");

              //  Log.i("Table is Not exists","TAble not exist");
            }
        }


    }
    private  void clear()
    {
        // TODO Auto-generated method stub
        File cache = new File(context.getFilesDir(), "/Epaper/");
        if (cache.exists() && cache.isDirectory())
        {
            Date lastModDate = new Date(cache.lastModified());
            Log.i("File last modified @ : ", lastModDate.toString());
            System.out.println("File last modified @ : "+ lastModDate.toString());
            deleteDir(cache);
            //  DELDir(cache);
            // Toast.makeText(context, "Your All Cache is clear", Toast.LENGTH_SHORT).show();


        }

       // Log.i("Setting", "cache directory" + cache);
        new AlertDialog.Builder(this)
            .setIcon(R.drawable.ic_error_outline)
            .setTitle("Cache Clear ")
            .setMessage(" All Saved ePapers Deleted")
            .setPositiveButton("Ok", new DialogInterface.OnClickListener()
            {
                @Override
                public void onClick(DialogInterface dialog, int which)
                {
                    Intent i = new Intent(getApplicationContext(), GeneralPrefsActivity.class);
                    startActivity(i);
                }

            })

                .show();





    }



/*    if(cacheDir.isDirectory())
    {
    File[] files=cacheDir.listFiles();

    for(File file:files)
    {
        if(null != file)
        {


            long lastModified = file.lastModified();

            if (0 < lastModified) {
                Date lastMDate = new Date(lastModified);
                Date today = new Date(System.currentTimeMillis());

                if (null != lastMDate && null != today) {
                    long diff = today.getTime() - lastMDate.getTime();
                    long diffDays = diff / (24 * 60 * 60 * 1000);
                    if (15 < diffDays)
                    {
                        file.delete();
                    }
                }
            }

        }
    }
}*/




    private boolean deleteDir(File dir)
    {
        // TODO Auto-generated method stub
        {
            if (dir.isDirectory())
            {
                // last modified date

                String[] children = dir.list();
                for (int i = 0; i < children.length; i++)
                {
                    boolean success = deleteDir(new File(dir, children[i]));

                    if (!success)
                    {
                        return false;
                    }
                }
            }
            // The directory is now empty so delete it
            return dir.delete();
        }


    }

    public boolean isTableExists(String tableName)
    {


        Cursor cursor = db.rawQuery("select DISTINCT tbl_name from sqlite_master where tbl_name = '"+tableName+"'", null);
        if(cursor!=null)
        {
            if(cursor.getCount()>0)
            {
                cursor.close();
                return true;
            }
            cursor.close();
        }
        return false;
    }
}
