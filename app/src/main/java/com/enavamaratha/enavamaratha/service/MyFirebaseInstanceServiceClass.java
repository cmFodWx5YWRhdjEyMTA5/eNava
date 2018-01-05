package com.enavamaratha.enavamaratha.service;

import android.content.SharedPreferences;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

import static com.enavamaratha.enavamaratha.utils.ApplicationConstants.FIREBASEREGID;
import static com.enavamaratha.enavamaratha.utils.ApplicationConstants.USER_DETAILS;

/**
 * Created by Pooja Mantri on 23/10/17.
 */

public class MyFirebaseInstanceServiceClass extends FirebaseInstanceIdService {

    private static final String TAG = MyFirebaseInstanceServiceClass.class.getSimpleName();


    // [START refresh_token]
    @Override
    public void onTokenRefresh() {
        // Get updated InstanceID token.
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();

        // Save Firebase Token in Shared Preference

        storeRegIdInPref(refreshedToken);


    }
    // [END refresh_token]


    private void storeRegIdInPref(String token) {

        SharedPreferences pref = getApplicationContext().getSharedPreferences(USER_DETAILS, 0);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString(FIREBASEREGID, token);
        editor.commit();

    }


}
