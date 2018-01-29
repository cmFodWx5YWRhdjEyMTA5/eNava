package com.enavamaratha.enavamaratha.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.enavamaratha.enavamaratha.R;
import com.github.paolorotolo.appintro.AppIntro;
import com.github.paolorotolo.appintro.AppIntroFragment;

import static com.enavamaratha.enavamaratha.utils.ApplicationConstants.IS_FIRST_TIME_LAUNCH;
import static com.enavamaratha.enavamaratha.utils.ApplicationConstants.REG_ID;
import static com.enavamaratha.enavamaratha.utils.ApplicationConstants.USER_DETAILS;

public class AppIntroActivity extends AppIntro {


    // For App Intro slider using library ---- https://github.com/apl-devs/AppIntro
    private SharedPreferences prefs;
    private SharedPreferences.Editor editor;
    private String regId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // setContentView(R.layout.activity_app_intro);


        initView();
        setSlides();


    }


    private void initView() {
        prefs = getSharedPreferences(USER_DETAILS, Context.MODE_PRIVATE);
        regId = prefs.getString(REG_ID, "");
        editor = prefs.edit();
    }


    private void setSlides() {
        /* Available animations:
        * setFadeAnimation()
setZoomAnimation()
setFlowAnimation()
setSlideOverAnimation()
setDepthAnimation()
        * */

        setZoomAnimation();

        // addSlide(AppIntroFragment.newInstance(title, description, image, backgroundColor));
        addSlide(AppIntroFragment.newInstance("Read ePaper", "You can read ePaper online and offline also.", R.drawable.screen_epaper, getResources().getColor(R.color.bg_screen3)));
        addSlide(AppIntroFragment.newInstance("Read Headlines", "You can read headlines online and offline.", R.drawable.screen_nws, getResources().getColor(R.color.bg_screen3)));
        addSlide(AppIntroFragment.newInstance("Categorise News", "You can read category wise news.", R.drawable.screen_cat, getResources().getColor(R.color.bg_screen3)));
        addSlide(AppIntroFragment.newInstance("Favourite News", "You can make favourite news using right swipe.", R.drawable.screen_fav, getResources().getColor(R.color.bg_screen3)));
        addSlide(AppIntroFragment.newInstance("Send News", "You can send news with attaching media.", R.drawable.screen_feedback, getResources().getColor(R.color.bg_screen3)));
        addSlide(AppIntroFragment.newInstance("Poll ", "You can answer the poll question.", R.drawable.screen_poll, getResources().getColor(R.color.bg_screen3)));



        /*
        *  //    addSlide(AppIntroFragment.newInstance("Read Full News","Read full news without internet connection.",R.drawable.screen_full_news,getResources().getColor(R.color.bg_screen4)));
        *  //      addSlide(AppIntroFragment.newInstance("Search News","You can search news.",R.drawable.screen_serach,getResources().getColor(R.color.bg_screen6)));
        *  //        addSlide(AppIntroFragment.newInstance("Play Games","You can play game without internet.",R.drawable.screen_game,getResources().getColor(R.color.bg_screen8)));
        * */


    }


    @Override
    public void onDonePressed() {
        super.onDonePressed();

        setPrefs();
        Intent intent = new Intent(getApplicationContext(), LadningActivity.class);
        intent.putExtra("regId", regId);
        startActivity(intent);
        finish();

    }


    @Override
    public void onSkipPressed() {
        super.onSkipPressed();
        setPrefs();
        Intent intent = new Intent(getApplicationContext(), LadningActivity.class);
        intent.putExtra("regId", regId);
        startActivity(intent);
        finish();
    }


    private void setPrefs() {

        editor.putBoolean(IS_FIRST_TIME_LAUNCH, false);
        editor.apply();
    }
}
