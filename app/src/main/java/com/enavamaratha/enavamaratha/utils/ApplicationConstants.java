package com.enavamaratha.enavamaratha.utils;

import android.content.Context;
import android.content.Intent;
 
public final class  ApplicationConstants 
{



	public static final String APP_SERVER_URL = "http://paper.enavamaratha.com//GetData.aspx?shareRegId=true";

	// eNavaMaratha Project Number
	 public static final String GOOGLE_PROJ_ID = "164887624783";



	static final String MSG_KEY = "m";
	static final String TAG = "FUNDO TOYS";
	static final String EXTRA_MESSAGE = "message";
	static final String DISPLAY_MESSAGE_ACTION="com.fundotoys.DISPLAY_MESSAGE";



	 
	public static void displayMessage(Context context, String message,String Url,String UrlType)
	{
        Intent intent = new Intent(DISPLAY_MESSAGE_ACTION);
        intent.putExtra(EXTRA_MESSAGE, message);
        context.sendBroadcast(intent);
    }
}

