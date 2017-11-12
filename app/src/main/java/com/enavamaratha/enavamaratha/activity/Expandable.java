package com.enavamaratha.enavamaratha.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.enavamaratha.enavamaratha.R;
import com.enavamaratha.enavamaratha.adapter.ExpandableListAdapter;
import com.enavamaratha.enavamaratha.service.ConnectionDetector;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;

import java.util.ArrayList;

public class Expandable extends AppCompatActivity {


    private ExpandableListAdapter ExpAdapter;
    private ArrayList<Group> ExpListItems;
    private ExpandableListView ExpandList;
    private AdView sAdview,sAdview_right;
    ConnectionDetector cd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_expandable);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        /*// smal ads on left side
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


        ExpandList = (ExpandableListView) findViewById(R.id.lvExp);
        String extra = getIntent().getStringExtra("contact");
        String contact = "contact";

        if (extra.equals(contact))
        {
            ExpListItems = SetStandardGroups1();
            getSupportActionBar().setTitle(R.string.contact);

        } else if (extra.equals("emergency"))
        {
            // preparing list data
            ExpListItems = SetStandardGroups();
            getSupportActionBar().setTitle(R.string.emergency);

        }

       // ExpListItems = SetStandardGroups();
        ExpAdapter = new ExpandableListAdapter(getApplicationContext(), ExpListItems);
        ExpandList.setAdapter(ExpAdapter);

        // On child Click
        ExpandList.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id)
            {

              String num = ((TextView)v.findViewById(R.id.lblListItem1)).getText().toString();

                String address1 =((TextView)v.findViewById(R.id.lblListItem)).getText().toString();

                if(address1.contains("कै.आचार्य गुंदेचा चौक,गंज बाजार")) {
                   /* String numb="0241-2414141";
                    Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + numb));
                    startActivity(intent);*/

                    Uri gmmIntentUri = Uri.parse("geo:0,0?q=Ganj+Bazaar,+Nalegaon,+Ahmednagar,+Maharashtra+414001");
                    Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                    mapIntent.setPackage("com.google.android.apps.maps");
                    startActivity(mapIntent);
                }

               else if(num.contains("0241-2414141/2417777/2415555")  )
                {
                    String numb="0241-2414141";
                    Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + numb));
                    startActivity(intent);
                } else if(num.equals("info@enavamaratha.com"))
                {

                    Intent emailintent = new Intent(android.content.Intent.ACTION_SEND);
                    emailintent.setType("plain/text");
                    emailintent.putExtra(android.content.Intent.EXTRA_EMAIL,new String[] {"info@enavamaratha.com" });
                    emailintent.putExtra(android.content.Intent.EXTRA_SUBJECT, "");
                    emailintent.putExtra(android.content.Intent.EXTRA_TEXT,"");
                    startActivity(Intent.createChooser(emailintent, "Send mail..."));
                }

                else if(num.contains("www.enavamaratha.com"))
                {

                    Uri uri = Uri.parse("http://web1.abmra.in/");
                    Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                    startActivity(intent);
                }



                else {
                    Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + num));
                    startActivity(intent);
                }

                return true;

            }
        });

    }

    // About eNavaMaratha Contacts
    public ArrayList<Group> SetStandardGroups1() {


        String head_names[] = {"दैनिक नवा मराठा ", "संपादकीय विभाग ", "जाहिरात विभाग", "अंक वितरण विभाग"};

        String names[] = {"मुख्य कार्यालय:","कै.आचार्य गुंदेचा चौक,गंज बाजार,","अहमदनगर - 414001", "पोस्ट बॉक्स :53", "ईमेल :", "संकेतस्थळ :", "दूरध्वनी :",
                "सुभाष भांगे :", "विजय माने :", "सुनील हारदे :", "सुधीर पवार :",
                "संजय कपिले :",
                "दूरध्वनी :", "ईमेल-",
        };
        String contact[] = {"","Map","", "", "info@enavamaratha.com", " www.enavamaratha.com", " 0241-2414141/2417777/2415555",
                "9767119990", "9226764128", "9421558804", "8888271886",
                "9260643687",
                "0241-2414141/2417777", "info@enavamaratha.com",

        };


        ArrayList<Group> list = new ArrayList<Group>();

        ArrayList<Child> ch_list;


        int j = 0;
        int size=7;

        for (String group_name : head_names)
        {
            Group gru = new Group();
            gru.setName(group_name);

            ch_list = new ArrayList<Child>();


            for (; j < size; j++)
            {
                Child ch = new Child();
                ch.setName(names[j]);
                ch.setNumber(contact[j]);
                ch_list.add(ch);
            }
            gru.setItems(ch_list);
            list.add(gru);
            System.out.println("Size in for loop " + size);

            if(size == 7 )
            {
                size=size+4;
                System.out.println("Size in for loop size== 7 :"+size);
            }
           else if(size == 11)
            {
                size = size+1;
                System.out.println("Size in for loop size== 11 : "+size);
            }
          else if (size==12 )
            {
                size= size+2;
                System.out.println("Size in for loop size== 12 :"+size);
            }

        }
                return list;
    }

    // Emergency Contacts
    public ArrayList<Group> SetStandardGroups()
    {


        String group_names[] =
                { "रुग्णवाहिका", "पोलीस", "जिल्हाधिकारी", "महानगरपालिका", "महावितरण" };

        // Names of ambulance,police etc.(only 4 child under group) Textview 1
        String country_names[] = { "रावसाहेब पटवर्धन", "शणेश्वर ट्रस्ट", "फिरोदिया ट्रस्ट", "नोबेल रुग्णवाहिका",
                "कोतवाली पोलिस स्टेशन", "तोफखाना पोलिस स्टेशन", "भिंगार पोलिस स्टेशन", "एम.आई.डी.सी. पोलिस स्टेशन",
                "जिल्हाधिकारी ", "निवासी उपजिल्हाधिकारी ", "जिल्हा नियोजन अधिकारी ", "जिल्हा पुरवठा अधिकारी",
                "मुख्य कार्यालय ", "मा. महापौर साहेब ", "मा.उप महापौर साहेब ", "मा. सभापती (स्थायी समिती)",
                "अधिक्षक अभियंता ", "कार्यकारी अभियंता (ग्रामीण)", "कार्यकारी अभियंता (शहर) ","पाॅवर  हाऊस "                };

        // Numbers of ambulance ,police etc. Textview 2
        String numbers[]={" 9422224956","9146235853","0241-2355120","9423928700",
                "0241-2416117","0241-2416118","0241-2416121"," 0241-2416123",
                "0241-2345001","0241-2345004","0241-2345864","0241-2326273",
                "0241-2343622,2340522","0241-2345127,2326682","0241-2323019","0241-2320807",
                "0241-2353492,2353645","0241-2356299,2353122","0241-2357960,2354269","0241-2341044"
              };



        ArrayList<Group> list = new ArrayList<Group>();

        ArrayList<Child> ch_list;

        int size = 4;
        int j = 0;

        for (String group_name : group_names) {
            Group gru = new Group();
            gru.setName(group_name);

            ch_list = new ArrayList<Child>();
            for (; j < size; j++) {
                Child ch = new Child();
                ch.setName(country_names[j]);
                ch.setNumber(numbers[j]);
                ch_list.add(ch);
            }
            gru.setItems(ch_list);
            list.add(gru);

            size = size + 4;
        }

        return list;
    }


    @Override
    protected void onResume() {

        super.onResume();

      /*  if( sAdview!= null ||  sAdview_right!= null)
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

/*

        if(sAdview!=null ||  sAdview_right!=null)
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
/*

        if( sAdview!=null ||  sAdview_right!=null)
        {

            sAdview.destroy();
            sAdview_right.destroy();
        }

*/


        super.onDestroy();
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
                Intent intee = new Intent(Expandable.this,HomeActivity.class);
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




  /*  private void prepareListDataforcontact() {
        listDataHeader = new ArrayList<String>();
        listDataChild = new HashMap<String, List<String>>();

        // Adding child data

        listDataHeader.add(" दैनिक नवा मराठा ");
        listDataHeader.add(" संपादकीय विभाग ");
        listDataHeader.add(" जाहिरात विभाग");
        listDataHeader.add(" अंक वितरण विभाग");



        List<String> nava = new ArrayList<String>();

        nava.add("मुख्य कार्यालय" +"\n"+"कै.आचार्य गुंदेचा चौक,"+"\n"+"गंज बाजार,"+"\n"+"अहमदनगर - 414001");
        nava.add("पोस्ट बॉक्स : 53");
        nava.add("ईमेल : info@enavamaratha.com");
        nava.add("संकेतस्थळ : www.enavamaratha.com");
        nava.add("दूरध्वनी : 0241- 2414141 / 2417777 / 2415555");

        // Adding child data
        List<String> sampdak = new ArrayList<String>();
        sampdak.add("सुभाष भांगे :9767119990");
        sampdak.add("विजय माने : 9226764128");
        sampdak.add("सुनील हारदे : 9421558804");
        sampdak.add("सुधीर पवार : 8888271886");



        List<String> jahirat = new ArrayList<String>();
        jahirat.add("संजय कपिले : 9260643687");




        List<String> vitran = new ArrayList<String>();
        vitran.add("0241- 2414141 / 2417777");
        vitran.add("ईमेल- info@enavamaratha.com");


        listDataChild.put(listDataHeader.get(0),nava);
        listDataChild.put(listDataHeader.get(1), sampdak); // Header, Child data
        listDataChild.put(listDataHeader.get(2), jahirat);
        listDataChild.put(listDataHeader.get(3), vitran);


    }




*/