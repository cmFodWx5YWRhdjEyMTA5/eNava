package com.enavamaratha.enavamaratha.adapter;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Typeface;
import android.support.v7.widget.CardView;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.enavamaratha.enavamaratha.R;


/**
 * Created by Pooja Mantri on 31/7/17.
 */

public class NotificationAdapter extends CursorAdapter {


    public NotificationAdapter(Context context, Cursor c) {
        super(context, c);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.list_item_notification, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {


        // Find fields to populate in inflated template
        CardView notificaionView = (CardView) view.findViewById(R.id.NotifficationView);
        TextView txtId = (TextView) view.findViewById(R.id.txtNotificationid);
        TextView txtNotification = (TextView) view.findViewById(R.id.txtNotificationtitle);
        TextView txtDate = (TextView) view.findViewById(R.id.txtNotificationdesc);

        int position = cursor.getPosition();

        if (position % 3 == 0) {
            notificaionView.setCardBackgroundColor(view.getResources().getColor(R.color.lightBlue));
        } else if (position % 3 == 1) {
            notificaionView.setCardBackgroundColor(view.getResources().getColor(R.color.lightPink));
        } else if (position % 3 == 2) {
            notificaionView.setCardBackgroundColor(view.getResources().getColor(R.color.lightYelloow));
        }


        // Extract properties from cursor
        String id = cursor.getString(cursor.getColumnIndexOrThrow("_id"));
        String tempmessage = cursor.getString(cursor.getColumnIndexOrThrow("message"));
        String time = cursor.getString(cursor.getColumnIndexOrThrow("time"));


        txtId.setText(id);
        txtNotification.setText(tempmessage);
        txtDate.setText(time);


    }
}
