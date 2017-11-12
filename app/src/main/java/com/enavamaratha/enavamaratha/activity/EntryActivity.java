

package  com.enavamaratha.enavamaratha.activity;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;

import  com.enavamaratha.enavamaratha.Constants;
import  com.enavamaratha.enavamaratha.R;
import  com.enavamaratha.enavamaratha.fragment.EntryFragment;
import  com.enavamaratha.enavamaratha.utils.PrefUtils;
import  com.enavamaratha.enavamaratha.utils.UiUtils;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;

import java.text.SimpleDateFormat;
import java.util.Date;

public class EntryActivity extends BaseActivity {

    private EntryFragment mEntryFragment;
    InterstitialAd mInterstitialAd;
    static int count=0;
    SQLiteDatabase db;
    private static final String TAG_ACTIVITY="EntryActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        UiUtils.setPreferenceTheme(this);
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_entry);

        mEntryFragment = (EntryFragment) getFragmentManager().findFragmentById(R.id.entry_fragment);
        if (savedInstanceState == null) { // Put the data only the first time (the fragment will save its state)
            mEntryFragment.setData(getIntent().getData());
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        db = openOrCreateDatabase("MyMb", MODE_PRIVATE, null);
        db.execSQL("create table if not exists bigadv(id integer primary key autoincrement, date varchar , count integer, maxcount intger)");
        // Big Add

        // get Todays Date


      /*  // Big Advertise
        mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId(getString(R.string.inter_ad_unit));
        AdRequest adRequest = new AdRequest.Builder()
              //  . addTestDevice("CE5BF23EF32893496DAAAEA8CBB1EB93")
                .build();
        mInterstitialAd.loadAd(adRequest);
        mInterstitialAd.setAdListener(new AdListener()
        {
            public void onAdLoaded()
            {
                showInterstitial();

            }
        });*/

        if (PrefUtils.getBoolean(PrefUtils.DISPLAY_ENTRIES_FULLSCREEN, false))
        {
            setImmersiveFullScreen(true);
        }
    }

    /*private void showInterstitial()
    {


        int maxcount=30;
        int firstcount=0;

        final String ROWID = "id";

        String date = new SimpleDateFormat("yyyy-MM-dd").format(new Date());

        Cursor c = db.rawQuery("select * from bigadv", null);

        if (c != null && c.getCount() > 0 && c.moveToFirst())
        {

            String gatedate = c.getString(c.getColumnIndex("date"));
            int _id = c.getInt(c.getColumnIndex("id"));
            int gatecount = c.getInt(c.getColumnIndex("count"));
            int gatemaxcount=c.getInt(c.getColumnIndex("maxcount"));


            if(gatedate.equals(date))
            {
                if(gatecount <= gatemaxcount)
                {
                    //increase count by 1
                    gatecount++;
                    if ((gatecount % 3) == 0 && gatecount <= gatemaxcount)
                    {

                        if (mInterstitialAd.isLoaded())
                        {

                            // update count and show ads
                            ContentValues args = new ContentValues();
                            args.put("count", gatecount);
                            int updatev = db.update("bigadv", args, ROWID + "=" + 1, null);
                            Log.i(TAG_ACTIVITY, "Count For Big Ads " +gatecount);
                            mInterstitialAd.show();

                        }


                    }
                    // update count value
                    else
                    {

                        ContentValues args = new ContentValues();
                        args.put("count",gatecount);
                        int updatev = db.update("bigadv", args, ROWID + "=" + _id, null);
                        Log.i(TAG_ACTIVITY, "Count For Big Ads  " + gatecount);

                    }
                }


            }

            // update todaysdate and count is zero
            else
            {
                ContentValues args = new ContentValues();
                args.put("date",date);
                args.put("count",firstcount);
                int updatev = db.update("bigadv", args, ROWID + "=" + _id, null);
                Log.i(TAG_ACTIVITY, "Date  For Big Ads " + date);
            }


        }

        // insert values first time
        else
        {
            db.execSQL("INSERT INTO bigadv (date,count,maxcount)VALUES ( '" + date + "', '" + firstcount + "','" + maxcount + "' )");
            System.out.println("Insert data for first time");
        }


        c.close();
        db.close();
    }*/

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home)
        {
            Bundle b = getIntent().getExtras();
            if (b != null && b.getBoolean(Constants.INTENT_FROM_WIDGET, false))
            {
                Intent intent = new Intent(this, HomeActivity.class);
                startActivity(intent);
            }
            finish();
            return true;
        }

        return false;
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        mEntryFragment.setData(intent.getData());
    }
}