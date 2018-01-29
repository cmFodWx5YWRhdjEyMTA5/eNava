package com.enavamaratha.enavamaratha.activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.provider.Settings;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
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
import com.google.firebase.iid.FirebaseInstanceId;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.TextHttpResponseHandler;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import static com.enavamaratha.enavamaratha.utils.ApplicationConstants.*;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.client.HttpClient;
import cz.msebera.android.httpclient.client.methods.HttpPost;
import cz.msebera.android.httpclient.impl.client.DefaultHttpClient;

public class Registration extends AppCompatActivity {

    private ProgressDialog progressDialog;
    private RequestParams params = new RequestParams();
    private EditText editname, editcontact, editemail, editcity;
    private Button btnregister;
    private String mUserRegId, mUserDevId, mUserName, mUserContact, mUserEmail, mUserCity;
    private TextInputLayout ttlName, ttlContact;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.registration);


        SharedPreferences prefs = getSharedPreferences(USER_DETAILS,
                Context.MODE_PRIVATE);
        String registrationId = prefs.getString(REG_ID, "");
        boolean isFirstLaunch = prefs.getBoolean(IS_FIRST_TIME_LAUNCH, true);


        // If User is Already Register then goto Home Activity
        // If user Register
        if (!TextUtils.isEmpty(registrationId)) {


            // If User Not First Launch then goto Home
            if (!isFirstLaunch) {
                Intent i = new Intent(getApplicationContext(), LadningActivity.class);
                i.putExtra("regId", registrationId);
                startActivity(i);
                finish();
            }

            // Else show App Intro slider
            else {
                Intent i = new Intent(getApplicationContext(), AppIntroActivity.class);
                startActivity(i);
                finish();
            }



        }



        initView();
        initData();


    }


    private void initView() {


        editname = (EditText) findViewById(R.id.input_name);
        editcontact = (EditText) findViewById(R.id.input_contact);
        editcity = (EditText) findViewById(R.id.input_city);
        editemail = (EditText) findViewById(R.id.input_email);
        btnregister = (Button) findViewById(R.id.btn_signup);


        ttlName = (TextInputLayout) findViewById(R.id.ttl_name);
        ttlContact = (TextInputLayout) findViewById(R.id.ttl_contact);


        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Please Wait...");
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setCancelable(false);

    }


    private void initData() {

        mUserRegId = FirebaseInstanceId.getInstance().getToken();
        mUserDevId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        mUserName = editname.getText().toString();
        mUserContact = editcontact.getText().toString();
        mUserEmail = editemail.getText().toString();
        mUserCity = editcity.getText().toString();


        editname.setTag("name");
        editcontact.setTag("contact");


    }


    public void ValidUser(View v)
    {

        initData();

        if (ConnectionDetector.isConnectingToInternet(getApplicationContext()))
        {


            // Using TextWatcher

            // Add TextChanged Listener
            editname.addTextChangedListener(new ValidateText(editname, ttlName));


            // Add TextChanged Listener
            editcontact.addTextChangedListener(new ValidateText(editcontact, ttlContact));


            boolean mValidateName = Utility.validate(mUserName);
            boolean mValidateMobile = Utility.validate2(mUserContact);


            // Validate User Name
            if (mValidateName)
            {
                ttlName.setError(null);
                ttlName.setErrorEnabled(false);


                // Validate User Contact Number

                if (mValidateMobile) {
                    ttlContact.setError(null);
                    ttlContact.setErrorEnabled(false);


                    // Register User

                    RegisterUserWithServer(mUserName, mUserContact, mUserEmail, mUserDevId, mUserRegId);


                }

                // Show Error to TextInputLayout
                else {
                    ttlContact.setErrorEnabled(true);
                    ttlContact.setError("Enter your correct 10 digit mobile number");
                }


            }

            // Show Error to TextInputLayout
            else
            {
                ttlName.setErrorEnabled(true);
                ttlName.setError("Enter your correct name");

            }


        } else {
            showAlertDialog(Registration.this, getResources().getString(R.string.no_internet), getResources().getString(R.string.no_internet_msg), false);
        }

    }


    private void RegisterUserWithServer(final String UserName, final String Contact, String EMail, String DevId, final String UserRegId)
    {

        params.put("devId", DevId);
        params.put("regId", UserRegId);
        params.put("name", UserName);
        params.put("mobile", Contact);
        params.put("emailid", EMail);


        // Make RESTful webservice call using AsyncHttpClient object
        AsyncHttpClient client = new AsyncHttpClient();
        final int DEFAULT_TIMEOUT = 20 * 1000;
        client.setConnectTimeout(DEFAULT_TIMEOUT);
        client.setResponseTimeout(35000);
        client.post(ApplicationConstants.APP_SERVER_URL, params, new TextHttpResponseHandler() {


            @Override
            public void onStart() {
                super.onStart();
                progressDialog.show();

            }


            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {


                if (progressDialog != null) {
                    progressDialog.dismiss();
                }


                Toast.makeText(Registration.this, "Try Again!", Toast.LENGTH_SHORT).show();

            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, String responseString) {

                storeRegIdinSharedPref(getApplicationContext(), UserRegId, UserName, Contact);

                FeedDataContentProvider.addFeed(Registration.this, ur, nam, true, "", "", 0);
                FeedDataContentProvider.addFeed(Registration.this, url, nam1, true, "", "", 0);
                FeedDataContentProvider.addFeed(Registration.this, url1, name1, true, "", "", 0);
                FeedDataContentProvider.addFeed(Registration.this, url2, name2, true, "", "", 0);
                FeedDataContentProvider.addFeed(Registration.this, url4, name4, true, "", "", 0);
                FeedDataContentProvider.addFeed(Registration.this, url5, name5, true, "", "", 0);
                FeedDataContentProvider.addFeed(Registration.this, url6, name6, true, "", "", 0);
                FeedDataContentProvider.addFeed(Registration.this, url7, name7, true, "", "", 0);
                FeedDataContentProvider.addFeed(Registration.this, url8, name8, true, "", "", 0);
                FeedDataContentProvider.addFeed(Registration.this, url9, name9, true, "", "", 0);
                FeedDataContentProvider.addFeed(Registration.this, url10, name10, true, "", "", 0);
                FeedDataContentProvider.addFeed(Registration.this, url11, name11, true, "", "", 0);
                FeedDataContentProvider.addFeed(Registration.this, url12, name12, true, "", "", 0);
                FeedDataContentProvider.addFeed(Registration.this, url13, name13, true, "", "", 0);
                FeedDataContentProvider.addFeed(Registration.this, url14, name14, true, "", "", 0);
                FeedDataContentProvider.addFeed(Registration.this, url15, name15, true, "", "", 0);

                if (progressDialog != null) {
                    progressDialog.dismiss();
                }


                Toast.makeText(Registration.this, "Registered successfully!", Toast.LENGTH_SHORT).show();

                Intent intent = new Intent(getApplicationContext(), AppIntroActivity.class);
                startActivity(intent);
                finish();

            }


        });


        // Old Connection
        /*client.post(ApplicationConstants.APP_SERVER_URL, params,
                new AsyncHttpResponseHandler() {
                    @Override
                    public void onSuccess(int i, Header[] headers, byte[] bytes) {

                        storeRegIdinSharedPref(getApplicationContext(), UserRegId, UserName, Contact);

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

                        if (progressDialog != null) {
                            progressDialog.dismiss();
                        }


                        Toast.makeText(Registration.this, "Registered successfully!", Toast.LENGTH_SHORT).show();

                        Intent intent = new Intent(getApplicationContext(), AppIntroActivity.class);
                        startActivity(intent);
                        finish();


                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {


                        Log.e("Register", "onFailure: Error --- "+error.getMessage());
                        Log.e("Register", "onFailure: Status Code  --- "+statusCode);


                        if (progressDialog != null) {
                            progressDialog.dismiss();
                        }

                        // When Http response code is '404'
                        if (statusCode == 404) {
                            Toast.makeText(getApplicationContext(),
                                    "Internet Required . Please Try Later.",
                                    Toast.LENGTH_LONG).show();

                        }
                        // When Http response code is '500'
                        else if (statusCode == 500) {
                            Toast.makeText(getApplicationContext(),
                                    "Internet Required . Please Try Later.",
                                    Toast.LENGTH_LONG).show();

                        }
                        // When Http response code other than 404, 500
                        else {
                            Toast.makeText(getApplicationContext(),
                                    "Internet Required . Please Try Later.",
                                    Toast.LENGTH_LONG).show();

                        }

                    }

                });*/


    }


    // Store User Data in Preference
    private void storeRegIdinSharedPref(Context context, String regId, String name, String mobile) {

        SharedPreferences prefs = getSharedPreferences(USER_DETAILS, Context.MODE_PRIVATE);


        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(REG_ID, regId);
        editor.putString(USER_NAME, name);
        editor.putString(User_Contact, mobile);
        editor.commit();

    }


    public void showAlertDialog(final Context context, String title, String message, Boolean status) {
        AlertDialog.Builder builder = new AlertDialog.Builder(Registration.this);
        builder.setTitle(title);
        builder.setMessage(message);
        AlertDialog dialog = builder.create();
        // display dialog
        dialog.show();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
    }


    // Custom TextWatcher
    private class ValidateText implements TextWatcher {

        EditText editText;
        TextInputLayout textInputLayout;

        public ValidateText(EditText editText, TextInputLayout textInputLayout) {
            this.editText = editText;
            this.textInputLayout = textInputLayout;
        }


        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {


            String obj = editText.getTag().toString();

            switch (obj) {
                case "name":

                    if (Utility.validate(editname.getText().toString())) {
                        textInputLayout.setError(null);
                        textInputLayout.setErrorEnabled(false);

                    }

                    break;


                case "contact":

                    if (Utility.validate2(editcontact.getText().toString())) {
                        textInputLayout.setError(null);
                        textInputLayout.setErrorEnabled(false);

                    }

                    break;
            }


        }
    }

}