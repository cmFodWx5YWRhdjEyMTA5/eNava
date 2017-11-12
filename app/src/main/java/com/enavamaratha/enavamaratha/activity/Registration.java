package com.enavamaratha.enavamaratha.activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.enavamaratha.enavamaratha.R;
import com.enavamaratha.enavamaratha.provider.FeedDataContentProvider;
import com.enavamaratha.enavamaratha.service.ConnectionDetector;
import com.enavamaratha.enavamaratha.utils.ApplicationConstants;
import com.enavamaratha.enavamaratha.utils.Utility;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import cz.msebera.android.httpclient.Header;

public class Registration extends AppCompatActivity {
    ProgressDialog prgDialog;
    RequestParams params = new RequestParams();
    GoogleCloudMessaging gcmObj;
    Context applicationContext;
    String regId = "";
    String oneflag = "";

    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

    AsyncTask<Void, Void, String> createRegIdTask;

    public static final String REG_ID = "regId";

    public static final String One_Flag = "oneflag";
    public static final String EMAIL_ID = "eMailId";
    public static final String User_Contact = "contact";

    // All Feeds Url
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

// all Feeds Title in Navigation Drawer
    final String nam = "ठळक बातम्या";
    final String nam1 = "अर्थकारण";
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


    EditText editname, editcontact, editemail, editcity;
    Button btnregister;
    ConnectionDetector cd;
    SQLiteDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.registration);

        applicationContext = getApplicationContext();
        editname = (EditText) findViewById(R.id.input_name);
        editcontact = (EditText) findViewById(R.id.input_contact);
        editemail = (EditText) findViewById(R.id.input_email);
        editcity = (EditText) findViewById(R.id.input_city);
        btnregister=(Button)findViewById(R.id.btn_signup);

        btnregister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (cd.isConnectingToInternet(getApplicationContext()))
                {
                    RegisterUser(v);
                    db = openOrCreateDatabase("MyMb", MODE_PRIVATE, null);
                    db.execSQL("create table if not exists cache(id integer primary key autoincrement, time varchar )");
                    db.execSQL("create table if not exists user(id integer primary key autoincrement, name varchar, mobile varchar,email varchar,city varchar )");
                    Log.i("REGISTEATION", "Create Table in  REGISTERTATION");
                }

                else
                {
                    showAlertDialog(Registration.this, "No Internet Connection", "You don't have internet connection to Register..Please Try Again Later ", false);

                }


            }
        });

        prgDialog = new ProgressDialog(this);
        // Set Progress Dialog Text


        SharedPreferences prefs = getSharedPreferences("UserDetails",
                Context.MODE_PRIVATE);
        String registrationId = prefs.getString(REG_ID, "");

        if (!TextUtils.isEmpty(registrationId)) {

            Intent i = new Intent(applicationContext, HomeActivity.class);
            i.putExtra("regId", registrationId);
            startActivity(i);
            finish();
        }
    }


    public void showAlertDialog(final Context context, String title, String message, Boolean status) {
       AlertDialog.Builder builder = new AlertDialog.Builder(Registration.this);
        builder.setTitle(title);
        builder.setMessage(message);
        AlertDialog dialog = builder.create();
        // display dialog
        dialog.show();
    }


    public void RegisterUser(View view)
    {
        String emailID = editname.getText().toString();
        String EmailID = editemail.getText().toString();
        String Mobilee = editcontact.getText().toString();

        if (!TextUtils.isEmpty(emailID) && Utility.validate(emailID))
        {
            if(!TextUtils.isEmpty(Mobilee) && Utility.validate2(Mobilee))
            {


                    if (checkPlayServices())
                    {
                        registerInBackground(emailID);

                    }



            }
            //When mobile is invalid
            else
            {
                editcontact.setError("Please Enter 10 digit Valid Mobile Number");
                //Toast.makeText(applicationContext, "Please enter valid 10 digit Mobileno",
                  //      Toast.LENGTH_LONG).show();
            }

        }
        // When name is invalid
        else
        {
            editname.setError("Please Enter Correct Name");
           // Toast.makeText(applicationContext, "Please enter valid name",
                  //  Toast.LENGTH_LONG).show();
        }


    }

    private void registerInBackground(final String emailID)
    {




        new AsyncTask<Void, Void, String>()
        {
            @Override
            protected String doInBackground(Void... params)
            {
                String msg = "";
                try {
                    if (gcmObj == null)
                    {
                        gcmObj = GoogleCloudMessaging
                                .getInstance(applicationContext);
                    }
                    regId = gcmObj.register(ApplicationConstants.GOOGLE_PROJ_ID);
                    msg = "Registration ID :" + regId;

                } catch (IOException ex) {
                    msg = "Error :" + ex.getMessage();
                }
                return msg;
            }

            @Override
            protected void onPreExecute() {
                prgDialog.setMessage("Please wait...");
                prgDialog.show();
                // Set Cancelable as False
                prgDialog.setCancelable(false);
            }
            @Override
            protected void onPostExecute(String msg)
            {
                if (!TextUtils.isEmpty(regId))
                {
                    prgDialog.dismiss();
                    storeRegIdinSharedPref(applicationContext, regId, emailID);



                    SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
                    String currentDateandTime = sdf.format(new Date());
                     db.execSQL("INSERT INTO cache (time)VALUES ( '" + currentDateandTime + "')");

                    db.close();
                    System.out.println("insert date into cache" + currentDateandTime);
                    /*Toast.makeText(
                            applicationContext,
                            "Registered with NavaMaratha successfully.\n\n"
                            , Toast.LENGTH_SHORT).show();*/
                } else
                {
                    Toast.makeText(applicationContext,"Please Try Again..!!!",Toast.LENGTH_LONG).show();
                   /* Toast.makeText(
                            applicationContext,
                            "Reg ID Creation Failed.\n\nEither you haven't enabled Internet or NavaMaratha server is busy right now. Make sure you enabled Internet and try registering again after some time."
                            , Toast.LENGTH_LONG).show();*/
                    if (prgDialog != null) {
                        prgDialog.dismiss();
                    }
                }
            }
        }.execute(null, null, null);
    }


    private void storeRegIdinSharedPref(Context context, String regId,String mobile)
    {
        String oneflag = "1";
        String contact = editcontact.getText().toString();
        SharedPreferences prefs = getSharedPreferences("UserDetails", Context.MODE_PRIVATE);


        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(REG_ID, regId);
        editor.putString(EMAIL_ID, mobile);
        editor.putString(User_Contact,contact);
        editor.putString(One_Flag, oneflag);
        editor.commit();
        storeRegIdinServer();

    }

    private void storeRegIdinServer() {
        String devid = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        String mobileno = editcontact.getText().toString();
        String emailid = editemail.getText().toString();
        String name = editname.getText().toString();

        prgDialog.show();
        params.put("devId", devid);
        params.put("regId", regId);
        params.put("name", name);
        params.put("mobile", mobileno);
        params.put("emailid", emailid);


        // Make RESTful webservice call using AsyncHttpClient object
        AsyncHttpClient client = new AsyncHttpClient();
        client.post(ApplicationConstants.APP_SERVER_URL, params,
                new AsyncHttpResponseHandler() {
                    @Override
                    public void onSuccess(int i, Header[] headers, byte[] bytes) {
                        prgDialog.hide();
                        if (prgDialog != null) {
                            prgDialog.dismiss();
                        }
                      /*  Toast.makeText(applicationContext,
                                "Reg Id shared successfully  ",
                                Toast.LENGTH_LONG).show();*/

                        //

                        FeedDataContentProvider.addFeed(Registration.this, ur, nam, true, "", "", 0);
                        FeedDataContentProvider.addFeed(Registration.this, url, nam1, true,"","",0);
                        FeedDataContentProvider.addFeed(Registration.this, url1, name1, true,"","",0);
                        FeedDataContentProvider.addFeed(Registration.this, url2, name2, true,"","",0);
                        FeedDataContentProvider.addFeed(Registration.this, url4, name4, true,"","",0);
                        FeedDataContentProvider.addFeed(Registration.this, url5, name5, true,"","",0);
                        FeedDataContentProvider.addFeed(Registration.this, url6, name6, true,"","",0);
                        FeedDataContentProvider.addFeed(Registration.this, url7, name7, true,"","",0);
                        FeedDataContentProvider.addFeed(Registration.this, url8, name8, true,"","",0);
                        FeedDataContentProvider.addFeed(Registration.this, url9, name9, true,"","",0);
                        FeedDataContentProvider.addFeed(Registration.this, url10, name10, true,"","",0);
                        FeedDataContentProvider.addFeed(Registration.this, url11, name11, true,"","",0);
                        FeedDataContentProvider.addFeed(Registration.this, url12, name12, true,"","",0);
                        FeedDataContentProvider.addFeed(Registration.this, url13, name13, true,"","",0);
                        FeedDataContentProvider.addFeed(Registration.this, url14, name14, true,"","",0);
                        FeedDataContentProvider.addFeed(Registration.this, url15, name15, true,"","",0);

                        //Log.i("Registrtation","Feed Added In Reg ");
                        Intent intent = new Intent(applicationContext, HomeActivity.class);
                        intent.putExtra("regId", regId);
                        //i.putExtra("url", notify);
                        startActivity(intent);
                        finish();
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, byte[] bytes, Throwable error) {

                        prgDialog.hide();
                        if (prgDialog != null) {
                            prgDialog.dismiss();
                        }
                        // When Http response code is '404'
                        if (statusCode == 404) {
                            Toast.makeText(applicationContext,
                                    "Internet Required . Please Try Later.",
                                    Toast.LENGTH_LONG).show();
                            /*Toast.makeText(applicationContext,
                                    "Requested resource not found",
                                    Toast.LENGTH_LONG).show();*/
                        }
                        // When Http response code is '500'
                        else if (statusCode == 500) {
                            Toast.makeText(applicationContext,
                                    "Internet Required . Please Try Later.",
                                    Toast.LENGTH_LONG).show();
                          /*  Toast.makeText(applicationContext,
                                    "Something went wrong at server end",
                                    Toast.LENGTH_LONG).show();*/
                        }
                        // When Http response code other than 404, 500
                        else {
                            Toast.makeText(applicationContext,
                                    "Internet Required . Please Try Later.",
                                    Toast.LENGTH_LONG).show();
                           /* Toast.makeText(
                                    applicationContext,
                                    "Unexpected Error occcured! [Most common Error: Device might "
                                            + "not be connected to Internet or remote server is not up and running], check for other errors as well",
                                    Toast.LENGTH_LONG).show();*/
                        }
                    }
                });

    }


    // Hide Progress

    // When the response returned by REST has Http
    // response code '200'

    // When the response returned by REST has Http
    // response code other than '200' such as '404',
    // '500' or '403' etc

    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil
                .isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Toast.makeText(
                        applicationContext,
                        "This device doesn't support Play services, App will not work normally",
                        Toast.LENGTH_LONG).show();
                finish();
            }
            return false;
        }/**
         else {
         Toast.makeText(
         applicationContext,
         "This device supports Play services, App will work normally",
         Toast.LENGTH_LONG).show();
         }
         **/
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkPlayServices();
    }

}







/*    public boolean validate() {
        boolean valid = true;

        String name = editname.getText().toString();
        String contact=editcontact.getText().toString();
        String email = editemail.getText().toString();
        String city = editcity.getText().toString();

        if (name.isEmpty() || name.length() < 3) {
            editname.setError("at least 3 characters");
            valid = false;
        } else {
            editname.setError(null);
        }

        if (contact.isEmpty() || !android.util.Patterns.PHONE.matcher(contact).matches())
        {
          editcontact.setError("enter a valid contact number");
            valid=false;
        } else
        {
        editcontact.setError(null);
        }
        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            editemail.setError("enter a valid email address");
            valid = false;
        } else {
            editemail.setError(null);
        }

        if (city.isEmpty() || city.length() <4)
        {
            editcity.setError("enter a valid city");
            valid = false;

        } else {
            editcity.setError(null);
        }

        return valid;
    }
}*/