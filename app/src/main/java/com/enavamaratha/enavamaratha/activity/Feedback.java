package com.enavamaratha.enavamaratha.activity;

import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.provider.MediaStore;
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
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.enavamaratha.enavamaratha.R;
import com.enavamaratha.enavamaratha.service.ConnectionDetector;
import com.enavamaratha.enavamaratha.utils.Utility;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;

import java.util.ArrayList;

public class Feedback extends AppCompatActivity implements View.OnClickListener
{
    EditText name,msg,contact;
    ImageButton btnAttachment;
    Button btnmail,btnsms,btnwhtsapp;
    TextView txtinfo;
    String Name,Message,number;

    String email, message, attachmentFile;
    Uri URI = null;
    private static final int PICK_FROM_GALLERY = 101;
    int columnIndex;

    Context context;
    ArrayList<Uri> arrayUri = new ArrayList<Uri>();
    //InterstitialAd mInterstitialAd;
    private AdView sAdview,sAdview_right;
    ConnectionDetector cd;

    public static final String EMAIL_ID = "eMailId";
    public static final String User_Contact = "contact";



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(R.string.write_to_editor);


        name = (EditText) findViewById(R.id.edtemail);
        msg = (EditText) findViewById(R.id.edtmsg);
        contact=(EditText)findViewById(R.id.edtcontact);

        btnAttachment=(ImageButton)findViewById(R.id.btnattach);
        btnmail = (Button) findViewById(R.id.btnemail);
        btnsms = (Button) findViewById(R.id.btnsms);
        btnwhtsapp=(Button)findViewById(R.id.btnwhatsapp);
        context = getApplicationContext();

        // Big Add
       /* mInterstitialAd = new InterstitialAd(this);
        // set the ad unit ID
        mInterstitialAd.setAdUnitId(getString(R.string.interstitial_full_screen));
        AdRequest adRequest = new AdRequest.Builder()
               // . addTestDevice("CE5BF23EF32893496DAAAEA8CBB1EB93")
                .build();
        // Load ads into Interstitial Ads
        mInterstitialAd.loadAd(adRequest);
        mInterstitialAd.setAdListener(new AdListener()
        {
            public void onAdLoaded() {
                showInterstitial();
            }
        });*/


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


        SharedPreferences prefs = getSharedPreferences("UserDetails", Context.MODE_PRIVATE);

        String username=prefs.getString(EMAIL_ID, "");
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

  /*  private void showInterstitial() {
        if (mInterstitialAd.isLoaded()) {
            mInterstitialAd.show();
        }
    }*/
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
            Log.e("Attachment Path:", attachmentFile);
            URI = Uri.parse("file://" + attachmentFile);
            cursor.close();
        }
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

       /* if( sAdview!=null ||  sAdview_right!=null)
        {

            sAdview.destroy();
            sAdview_right.destroy();
        }
*/


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

                    if (!TextUtils.isEmpty(Msg))
                    {
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
                                                    "Name :" + name.getText().toString() + "<br>Mob:" + contact.getText().toString()+ "<br>Msg :" + msg.getText().toString();


                                            StringBuilder result = new StringBuilder();

                                            result.append("Message Subject :Nava Maratha Apps News"+"\n");
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


                                        }catch(Exception e)
                                        {

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
                        new AlertDialog.Builder(this)
                                .setMessage("Do You Really Want to send by SMS..??")
                                .setTitle("Send By SMS")
                                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {

                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {

                                        try {
                                            String phoneNumber = "8446447744";

                                            StringBuilder result = new StringBuilder();

                                            result.append("Message Subject : Nava Maratha Apps News"+"\n");
                                            result.append("Name :" + name.getText().toString());
                                            result.append(System.getProperty("line.separator"));
                                            result.append("Mob:" + contact.getText().toString());
                                            result.append(System.getProperty("line.separator"));
                                            result.append("Msg :" + msg.getText().toString());

                                            String msggg = result.toString();


                                            System.out.println("Total Message From StringBuilder:" + msggg);
                                            try {
                                                SmsManager smsManager = SmsManager.getDefault();
                                                // Send a text based SMS
                                                smsManager.sendTextMessage(phoneNumber, null, msggg, null, null);
                                                Toast.makeText(getApplicationContext(), "Sms Sent..Thanks For Your Feedback..!!  ", Toast.LENGTH_SHORT).show();
                                                clear();
                                            } catch (Exception e)
                                            {
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

                        Toast.makeText(Feedback.this,"Please Enter Your News",Toast.LENGTH_SHORT).show();
                        msg.setError("Please Enter Your News");

                    }
                }
                //When mobile is invalid
                else {

                    Toast.makeText(Feedback.this,"Please Enter Correct Number",Toast.LENGTH_SHORT).show();
                    contact.setError("Please Enter Correct Number");
                    // Toast.makeText(applicationContext, "Please enter valid name",
                    //  Toast.LENGTH_LONG).show();
                }

            }
            // When name is invalid
            else {
                Toast.makeText(Feedback.this,"Please Enter Your Correct Name",Toast.LENGTH_SHORT).show();
                name.setError("Please Enter Your Correct Name");
                //Toast.makeText(applicationContext, "Please enter valid 10 digit Mobileno",
                //      Toast.LENGTH_LONG).show();
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



}

