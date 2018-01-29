package com.enavamaratha.enavamaratha.activity;

import android.Manifest;
import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.telephony.SmsManager;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.enavamaratha.enavamaratha.R;
import com.enavamaratha.enavamaratha.service.ConnectionDetector;
import com.enavamaratha.enavamaratha.utils.Utility;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;


import static com.enavamaratha.enavamaratha.utils.ApplicationConstants.USER_DETAILS;
import static com.enavamaratha.enavamaratha.utils.ApplicationConstants.USER_NAME;
import static com.enavamaratha.enavamaratha.utils.ApplicationConstants.User_Contact;


import java.util.ArrayList;
import java.util.List;

public class Feedback extends AppCompatActivity implements View.OnClickListener
{
    EditText name,msg,contact;
    ImageButton btnAttachment;
    Button btnmail,btnsms,btnwhtsapp;

    String Name,Message,number;

    String email, message, attachmentFile;
    Uri URI = null;
    private static final int PICK_FROM_GALLERY = 101;
    int columnIndex;

    Context context;
    ArrayList<Uri> arrayUri = new ArrayList<Uri>();

    RelativeLayout llRelFeedBakcForm;
    boolean result = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(R.string.write_to_editor);


        // Multiple Runtime Permission For Send Sms and Gallery

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            initView();
            PermissionCheck();

        } else {
            initView();
        }

    }


    private void initView() {

        name = (EditText) findViewById(R.id.edtfemail);
        msg = (EditText) findViewById(R.id.edtfmsg);
        contact = (EditText) findViewById(R.id.edtfcontact);

        btnAttachment = (ImageButton) findViewById(R.id.btnfattach);
        btnmail = (Button) findViewById(R.id.btnfemail);
        btnsms = (Button) findViewById(R.id.btnfsms);
        btnwhtsapp = (Button) findViewById(R.id.btnfwhatsapp);
        context = getApplicationContext();

        llRelFeedBakcForm = (RelativeLayout) findViewById(R.id.llRelFeedback);

        SharedPreferences prefs = getSharedPreferences(USER_DETAILS, Context.MODE_PRIVATE);

        String username = prefs.getString(USER_NAME, "");
        String usercontact=prefs.getString(User_Contact, "");

        name.setText(username);
        contact.setText(usercontact);

        Name = name.getText().toString();
        Message = msg.getText().toString();
        number=contact.getText().toString();

        btnmail.setOnClickListener(this);
        btnsms.setOnClickListener(this);
        btnAttachment.setOnClickListener(this);
        btnwhtsapp.setOnClickListener(this);

    }


    private void PermissionCheck() {

        Dexter.withActivity(this)
                .withPermissions(
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.SEND_SMS
                )
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport report) {


                        if (report.areAllPermissionsGranted()) {
                            initView();
                            result = true;

                        } else if (report.isAnyPermissionPermanentlyDenied()) {
                            showSettings();
                            result = false;
                        }


                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {

                        token.continuePermissionRequest();


                    }
                }).check();




    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (requestCode == PICK_FROM_GALLERY && resultCode == RESULT_OK) {
            /**
             * Get Path
             */
            Uri selectedImage = data.getData();
            arrayUri.add(selectedImage);
            String[] filePathColumn = { MediaStore.Images.Media.DATA };

            Cursor cursor = getContentResolver().query(selectedImage, filePathColumn, null, null, null);
            cursor.moveToFirst();
            columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            attachmentFile = cursor.getString(columnIndex);
            URI = Uri.parse("file://" + attachmentFile);
            cursor.close();
        }
    }


    // For Permissions go to Settings---
    private void openSettings() {
        Intent myAppSettings = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                Uri.parse("package:" + getApplicationContext().getPackageName()));
        myAppSettings.addCategory(Intent.CATEGORY_DEFAULT);
        myAppSettings.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(myAppSettings);
        finish();
    }


    private void showSettings() {
        Snackbar snackbar = Snackbar
                .make(llRelFeedBakcForm, "Storage and Sms permission required!", Snackbar.LENGTH_LONG)
                .setAction("Settings", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        openSettings();
                    }
                });

        snackbar.show();
    }


    @Override
    protected void onResume() {

        super.onResume();

    }

    @Override
    protected void onPause() {

        super.onPause();
    }

    @Override
    protected void onDestroy()
    {

        super.onDestroy();
    }

    @Override
    public void onClick(View v) {

        if (v == btnAttachment)
        {
            openGallery();

        }


        if(v == btnwhtsapp)
        {
            String Msg = msg.getText().toString();
            String mobilee = contact.getText().toString();
            String Name = name.getText().toString();

            if(!TextUtils.isEmpty(Name) && Utility.validate(Name))
            {
            if (!TextUtils.isEmpty(mobilee) && Utility.validate2(mobilee))

            {

                if (!TextUtils.isEmpty(Msg)) {

                    if (appInstalledOrNot("com.whatsapp")) {

                        new AlertDialog.Builder(this)
                                .setMessage("Do You Really Want to send by WhatsApp..??")
                                .setTitle("Send By WhatsApp")
                                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {

                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {

                                        try {

                                            email = name.getText().toString();

                                            message = msg.getText().toString();

                                            String body =
                                                    "Name :" + name.getText().toString() + "<br>Mob:" + contact.getText().toString() + "<br>Msg :" + msg.getText().toString();


                                            StringBuilder result = new StringBuilder();

                                            result.append("Message Subject :Nava Maratha Apps News" + "\n");
                                            result.append("Name :" + name.getText().toString());
                                            result.append(System.getProperty("line.separator"));
                                            result.append("Mob:" + contact.getText().toString());
                                            result.append(System.getProperty("line.separator"));
                                            result.append("Msg :" + msg.getText().toString());
                                            String msggg = result.toString();


                                            Intent sendIntent = new Intent(Intent.ACTION_SEND);
                                            sendIntent.putExtra(Intent.EXTRA_TEXT, msggg);
                                            sendIntent.setType("text/plain");
                                            sendIntent.setPackage("com.whatsapp");
                                            startActivity(sendIntent);


                                        } catch (Exception e) {

                                        }

                                        clear();

                                    }
                                })


                                .setNegativeButton("No", new DialogInterface.OnClickListener() {

                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                    }

                                })
                                .show();
                    } else {
                        Toast.makeText(this, "WhatsApp Not Installed", Toast.LENGTH_SHORT).show();
                    }
                }


                    else
                    {
                        Toast.makeText(Feedback.this,"Please Enter Your News",Toast.LENGTH_SHORT).show();
                        msg.setError("Please Enter Your News");

                    }


                }
            else
            {
                Toast.makeText(Feedback.this,"Please Enter Correct Number",Toast.LENGTH_SHORT).show();
                contact.setError("Please Enter Correct Number");

                // Toast.makeText(applicationContext, "Please enter valid name",
                //  Toast.LENGTH_LONG).show();
            }
                //When mobile is invalid


            }
            // When name is invalid

            else
            {
                Toast.makeText(Feedback.this,"Please Enter Correct Name",Toast.LENGTH_SHORT).show();
                name.setError("Please Enter Your Correct Name");
                //Toast.makeText(applicationContext, "Please enter valid 10 digit Mobileno",
                //      Toast.LENGTH_LONG).show();
            }



        }
        if (v == btnmail)
        {


            String Msg = msg.getText().toString();
            String mobilee = contact.getText().toString();
            String Name = name.getText().toString();


            if(!TextUtils.isEmpty(Name) && Utility.validate(Name))
            {
            if (!TextUtils.isEmpty(mobilee) && Utility.validate2(mobilee))
            {


                    if (!TextUtils.isEmpty(Msg))
                    {
                        new AlertDialog.Builder(this)
                                .setMessage("Do You Really Want to send by mail..??")
                                .setTitle("Send By eMail")
                                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {

                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {

                                        try {

                                            email = name.getText().toString();

                                            message = msg.getText().toString();

                                            String body =
                                                    "Name :" + name.getText().toString() +  "<br>Mob:" + contact.getText().toString()+"<br>Msg :" + msg.getText().toString() ;

                                            Intent emailIntent = new Intent();
                                            emailIntent.putExtra(Intent.EXTRA_SUBJECT,"Nava Maratha Apps News");
                                            emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{"info@enavamaratha.com"});
                                            emailIntent.putExtra(Intent.EXTRA_TEXT, Html.fromHtml(body));

                                            if (arrayUri.isEmpty()) {
                                                //Send email without photo attached
                                                emailIntent.setAction(Intent.ACTION_SEND);
                                                emailIntent.setType("plain/text");
                                            } else if (arrayUri.size() == 1) {
                                                //Send email with ONE photo attached
                                                emailIntent.setAction(Intent.ACTION_SEND);
                                                emailIntent.putExtra(Intent.EXTRA_STREAM, arrayUri.get(0));
                                                emailIntent.setType("image/* video/*");
                                            } else {
                                                //Send email with MULTI photo attached
                                                emailIntent.setAction(Intent.ACTION_SEND_MULTIPLE);
                                                emailIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, arrayUri);
                                                emailIntent.setType("image/* video/*");
                                            }

                                            startActivity(Intent.createChooser(emailIntent,
                                                    "Sending email..."));

                                            //this.startActivity(Intent.createChooser(emailIntent,"Sending email..."));


                                        } catch (Throwable t) {
                                            Toast.makeText(getApplicationContext(),
                                                    "Request failed try again: " + t.toString(),
                                                    Toast.LENGTH_LONG).show();
                                        }

                                        clear();

                                    }
                                })


                                .setNegativeButton("No", new DialogInterface.OnClickListener() {

                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                    }

                                })
                                .show();
                    }


                    else
                    {
                        Toast.makeText(Feedback.this,"Please Enter Your News",Toast.LENGTH_SHORT).show();
                        msg.setError("Please Enter Your News");

                    }


                }
                //When mobile is invalid
            else
            {

                Toast.makeText(Feedback.this,"Please Enter Correct Number",Toast.LENGTH_SHORT).show();
                contact.setError("Please Enter Correct Number");

                // Toast.makeText(applicationContext, "Please enter valid name",
                //  Toast.LENGTH_LONG).show();
            }


            }
            // When name is invalid

            else
            {
                Toast.makeText(Feedback.this,"Please Enter Your Correct Name",Toast.LENGTH_SHORT).show();
                name.setError("Please Enter Your Correct Name");
                //Toast.makeText(applicationContext, "Please enter valid 10 digit Mobileno",
                //      Toast.LENGTH_LONG).show();
            }




        }


        if (v == btnsms)
        {



            String names = name.getText().toString();
            String mobilees = contact.getText().toString();
            String msgs = msg.getText().toString();

            if (!TextUtils.isEmpty(names) && Utility.validate(names))
            {

                if (!TextUtils.isEmpty(mobilees) && Utility.validate2(mobilees))
            {


                    if (!TextUtils.isEmpty(msgs)) {


                        if (result) {
                            new AlertDialog.Builder(this)
                                    .setMessage("Do You Really Want to send by SMS..??")
                                    .setTitle("Send By SMS")
                                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {

                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {

                                            try {
                                                String phoneNumber = "9209666666";

                                                StringBuilder result = new StringBuilder();

                                                result.append("Message Subject : Nava Maratha Apps News" + "\n");
                                                result.append("Name :" + name.getText().toString());
                                                result.append(System.getProperty("line.separator"));
                                                result.append("Mob:" + contact.getText().toString());
                                                result.append(System.getProperty("line.separator"));
                                                result.append("Msg :" + msg.getText().toString());

                                                String msggg = result.toString();


                                                try {
                                                    SmsManager smsManager = SmsManager.getDefault();
                                                    // Send a text based SMS
                                                    smsManager.sendTextMessage(phoneNumber, null, msggg, null, null);
                                                    Toast.makeText(getApplicationContext(), "Sms Sent..Thanks For Your Feedback..!!  ", Toast.LENGTH_SHORT).show();
                                                    clear();
                                                } catch (Exception e) {
                                                    Toast.makeText(getApplicationContext(),
                                                            "SMS failed, please try again later!",
                                                            Toast.LENGTH_LONG).show();
                                                    e.printStackTrace();
                                                }

                                            } catch (Throwable t) {
                                                Toast.makeText(getApplicationContext(),
                                                        "Request failed try again: " + t.toString(),
                                                        Toast.LENGTH_LONG).show();
                                            }

                                            clear();

                                        }
                                    })


                                    .setNegativeButton("No", new DialogInterface.OnClickListener() {

                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();
                                        }

                                    })
                                    .show();

                        } else {

                            showSettings();

                        }


                    } else {

                        Toast.makeText(Feedback.this,"Please Enter Your News",Toast.LENGTH_SHORT).show();
                        msg.setError("Please Enter Your News");

                    }
                }
                //When mobile is invalid
                else {

                    Toast.makeText(Feedback.this,"Please Enter Correct Number",Toast.LENGTH_SHORT).show();
                    contact.setError("Please Enter Correct Number");

                }

            }
            // When name is invalid
            else {
                Toast.makeText(Feedback.this,"Please Enter Your Correct Name",Toast.LENGTH_SHORT).show();
                name.setError("Please Enter Your Correct Name");

            }



        }

    }

    private void clear()
    {
        // TODO Auto-generated method stub

        name.setText("");

        msg.setText("");

        contact.setText("");

    }

    public void openGallery() {


        Intent intent = new Intent();
        intent.setType("image/* video/* ");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        intent.putExtra("return-data", true);
        startActivityForResult(
                Intent.createChooser(intent, "Complete action using"),
                PICK_FROM_GALLERY);

    }




    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.menu_home, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        // if (id == R.id.action_settings) {
        //   return true;
        // }
        switch (id) {
            case R.id.menu_homee:
                Intent intee = new Intent(Feedback.this,HomeActivity.class);
                intee.putExtra("home","home");
                startActivity(intee);
                return true;


            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    private boolean appInstalledOrNot(String uri) {
        PackageManager pm = getPackageManager();
        try {
            pm.getPackageInfo(uri, PackageManager.GET_ACTIVITIES);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
        }

        return false;
    }


}

