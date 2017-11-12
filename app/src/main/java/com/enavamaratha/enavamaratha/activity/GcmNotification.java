package com.enavamaratha.enavamaratha.activity;

import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.enavamaratha.enavamaratha.Constants;
import com.enavamaratha.enavamaratha.R;

import com.enavamaratha.enavamaratha.provider.FeedData;
import com.enavamaratha.enavamaratha.service.ConnectionDetector;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;

public class GcmNotification extends AppCompatActivity {

    public DBManager dbManager;
    TextView idTextView, titleTextView, descTextView;
    public ListView listView;
    ImageView ads,ads1;

    public SimpleCursorAdapter adapter;

    private DatabaseHelper dbHelper;
    private com.enavamaratha.enavamaratha.provider.DatabaseHelper DBHelper;

    private Context context;
    private SQLiteDatabase database;
    private SQLiteDatabase datab;

    private AdView sAdview,sAdview_right;
    ConnectionDetector cd;


    final String[] from = new String[]{DatabaseHelper._ID,
            DatabaseHelper.MESSAGE, DatabaseHelper.TIME};

    final int[] to = new int[]{R.id.id, R.id.title, R.id.desc};
    private long _id;

    int idt;
    String url;
    String urltype;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.fragment_notification_list);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        dbManager = new DBManager(this);
        dbManager.open();
        final Cursor cursor = dbManager.fetch();

        context = getApplicationContext();
        // for get data from FeedEx.Db
        DBHelper = new com.enavamaratha.enavamaratha.provider.DatabaseHelper(new Handler(), getApplicationContext());
        datab = DBHelper.getWritableDatabase();

        // get Data from notification
        dbHelper = new DatabaseHelper(context);
        database = dbHelper.getWritableDatabase();



        listView = (ListView) findViewById(R.id.list_view);
        listView.setEmptyView(findViewById(R.id.empty));

        adapter = new SimpleCursorAdapter(this, R.layout.activity_gcmnotification, cursor, from, to, 0);
        adapter.notifyDataSetChanged();

        listView.setAdapter(adapter);
         urltype = getIntent().getStringExtra("UrlType");
         url = getIntent().getStringExtra("url");

       /* // smal ads on left side
        RelativeLayout smallad=(RelativeLayout)findViewById(R.id.smallad_left);
        sAdview = new AdView(getApplicationContext());
        AdSize smallsize = new AdSize(50,50);
        sAdview.setAdSize(smallsize);
        sAdview.setAdUnitId("ca-app-pub-4094279933655114/3492658587");
        smallad.addView(sAdview);
        AdRequest adre=new AdRequest.Builder().build();
        sAdview.loadAd(adre);

        // small ads on right side
        RelativeLayout smallad_right=(RelativeLayout)findViewById(R.id.smallad_right);
        sAdview_right = new AdView(getApplicationContext());
        AdSize smalls = new AdSize(50,50);
        sAdview_right.setAdSize(smalls);
        sAdview_right.setAdUnitId("ca-app-pub-4094279933655114/2015925381");
        smallad_right.addView(sAdview_right);
        AdRequest adreq=new AdRequest.Builder().build();
        sAdview_right.loadAd(adreq);*/


      /*  if(url!= null)
        {
            if (url.contains("."))
            {
                if (urltype.contains("Web"))
                {
                    Intent i = new Intent(this, PollActivity.class);
                    i.putExtra("poll", "Web");
                    i.putExtra("Notification", url);
                    startActivity(i);

                }

                else if (urltype.contains("App"))
                {

                    Intent i = new Intent(this, HomeActivity.class);
                    i.putExtra("Notification", url);
                    startActivity(i);


                    // get post id and check in databsae from that there entry id
                }
            }
        }



        else
        {
               System.out.println("Current Actitivy");
        }


        for disable listitem click
        if(listview.getChildAt(selectedPosition).isEnabled())
{
    listview.getChildAt(selectedPosition).setEnabled(false);
}
         */


        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Cursor curs = (Cursor) parent.getItemAtPosition(position);

                String Url = curs.getString(curs.getColumnIndex(DatabaseHelper.URL));
                String UrlType = curs.getString(curs.getColumnIndex(DatabaseHelper.URLTYPE));
                if (UrlType != null)
                {

                    // if url contain web
                    if (UrlType.contains("Web"))
                    {
                        Intent i = new Intent(getApplicationContext(), PollActivity.class);
                        i.putExtra("poll", "Web");
                        i.putExtra("Notification", Url);
                        startActivity(i);

                    }
                    // if url contains app url
                    else if (UrlType.contains("App"))
                    {
                        Uri mUr = Uri.parse("content://com.enavamaratha.enavamaratha.provider.FeedData/all_entries");
                        if (Url != null)
                        {
                            int _idd;
                            long entryidd;
                            long entryy = 0;
                            String re;

                            String[] cols = new String[]{FeedData.EntryColumns._ID, FeedData.EntryColumns.FEED_ID, FeedData.EntryColumns.GUID};
                            String filter = FeedData.EntryColumns.GUID + "='" + Url + "'";
                            Cursor cursor = datab.query(FeedData.EntryColumns.TABLE_NAME, cols, filter, null, null, null, null, null);

                            // check url is exist in db
                            if (cursor != null && cursor.getCount() > 0 && cursor.moveToFirst())
                            {
                                do {
                                    _idd = cursor.getColumnIndex(FeedData.EntryColumns._ID);
                                    re = cursor.getString(_idd);
                                    //convert string to long because our id is in long
                                    entryidd = Long.parseLong(re);
                                    entryy = entryidd;
                                    System.out.println("Id  in Country List" + re);
                                    System.out.println("Long Id  in Country List" + entryidd);

                                } while (cursor.moveToNext());

                                startActivity(new Intent(Intent.ACTION_VIEW, ContentUris.withAppendedId(mUr, entryy)));
                            }

                            // else go to home activity
                            else
                            {
                                // make clickable false
                                if (listView.getChildAt(position).isEnabled())
                                {
                                    listView.getChildAt(position).setEnabled(false);
                                }
                            }


                        }
                    }
                    // not web and not app then listview clickable make false
                    else {

                        if (listView.getChildAt(position).isEnabled())
                        {
                            listView.getChildAt(position).setEnabled(false);
                        }
                        //  listView.getChildAt(position).setEnabled(false);
                    }
                   // System.out.println("Url in list item click" + Url);
                    //System.out.println("UrlType in list item click" + UrlType);


                }
            }
        });





        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(final AdapterView<?> parent, final View view, final int position, long id) {
                AlertDialog.Builder builder = new AlertDialog.Builder(GcmNotification.this);
                builder.setTitle("Delete");
                builder.setMessage("Are you sure you want to delete?");

                final Cursor cursor = (Cursor) parent.getItemAtPosition(position);
                final long ID = cursor.getLong(cursor.getColumnIndexOrThrow("_id"));
                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int ii) {

                        // Cursor cursor = (Cursor) parent.getItemAtPosition(position);
                        // Get the state's capital from this listview_item_row in the database.

                        dbManager.delete(ID); // sqlcon is one my class, InfoAPI object

                        adapter.notifyDataSetChanged();
                        // cursor.close();

                    }
                });

                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int ii) {
                                dialog.dismiss();
                            }
                        }
                );
                builder.show();
                //  cursor.close();
                cursor.requery();

                return true;
            }
        });

     //   database.close();

    }


    @Override
    protected void onResume() {

        super.onResume();

       /* if( sAdview!= null ||  sAdview_right!= null)
        {

            sAdview.resume();
            sAdview_right.resume();
        }

        //Show the AdView if the data connection is available

        if(cd.isConnectingToInternet(getApplicationContext()))
        {

            sAdview.setVisibility(View.VISIBLE);
            sAdview_right.setVisibility(View.VISIBLE);


        }


        sAdview.resume();
        sAdview_right.resume();*/

    }

    @Override
    protected void onPause() {


     /*   if(sAdview!=null ||  sAdview_right!=null)
        {

            sAdview.pause();
            sAdview_right.pause();
        }
*/

        super.onPause();
    }

    @Override
    protected void onDestroy()
    {

        /*if( sAdview!=null ||  sAdview_right!=null)
        {

            sAdview.destroy();
            sAdview_right.destroy();
        }
*/


        super.onDestroy();
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_notification, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        // if (id == R.id.action_settings) {
        //   return true;
        // }
        switch (id) {
            case R.id.all:
                deleteall();
                break;

            case R.id.menu_hommee:
                Intent intee = new Intent(GcmNotification.this,HomeActivity.class);
                intee.putExtra("home","home");
                startActivity(intee);
                return true;


            case android.R.id.home:

                Intent intt = new Intent(GcmNotification.this,HomeActivity.class);
                intt.putExtra("home","home");
                startActivity(intt);
                    finish();
                   return true;
               /* finish();
                return true;*/
        }
        return super.onOptionsItemSelected(item);
    }


    private void deleteall() {
        // AlertDialog.Builder builder = new AlertDialog.Builder(this);

        Cursor c = database.rawQuery(" select * from " + DatabaseHelper.TABLE_NAME, null);

        if (c != null && c.getCount() > 0)
        {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Delete All");
            builder.setMessage("Are you sure you want to Delete All Notification?");

            builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {

                public void onClick(DialogInterface dialog, int which) {
                    // Do nothing but close the dialog

                    dbManager.deleteall();
                    listView.setAdapter(null);
                    adapter.notifyDataSetChanged();
                    Toast.makeText(GcmNotification.this, "All notifications deleted", Toast.LENGTH_LONG).show();
                    dialog.dismiss();
                }

            });

            builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // Do nothing
                    dialog.dismiss();
                }
            });

            AlertDialog alert = builder.create();
            alert.show();



        } else
        {
            Toast.makeText(this, "No Notificatoin", Toast.LENGTH_LONG).show();
        }
     //   c.close();
       // database.close();

    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        Intent intt = new Intent(GcmNotification.this,HomeActivity.class);
        intt.putExtra("home","home");
        startActivity(intt);
        finish();

    }
}