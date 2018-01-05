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
        if(getintentvalue.equals( getIntent().getStringExtra("methodName")))
        {

            if((isTableExists(mytable1)) == true)
            {
               db.execSQL("delete from mytable1");

                // update cache table with current date
                final String ROWID = "id";
                SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
                String currentDateandTime = sdf.format(new Date());

                ContentValues args = new ContentValues();
                args.put("time",currentDateandTime );
                int updatev = db.update("cache", args, ROWID + "=" +1, null);

                // call clear method
                clear();
            }


            else
            {
                Intent i = new Intent(getApplicationContext(), GeneralPrefsActivity.class);
                startActivity(i);

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

            deleteDir(cache);


        }

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
