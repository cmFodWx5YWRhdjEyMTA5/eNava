

package  com.enavamaratha.enavamaratha.adapter;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.text.Html;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.ResourceCursorAdapter;
import android.widget.TextView;

import com.amulyakhare.textdrawable.TextDrawable;
import com.amulyakhare.textdrawable.util.ColorGenerator;
import com.squareup.picasso.Picasso;

import  com.enavamaratha.enavamaratha.Constants;
import  com.enavamaratha.enavamaratha.MainApplication;
import  com.enavamaratha.enavamaratha.R;
import  com.enavamaratha.enavamaratha.provider.FeedData;
import  com.enavamaratha.enavamaratha.provider.FeedData.EntryColumns;
import  com.enavamaratha.enavamaratha.provider.FeedData.FeedColumns;
import  com.enavamaratha.enavamaratha.utils.CircleTransform;
import  com.enavamaratha.enavamaratha.utils.NetworkUtils;
import  com.enavamaratha.enavamaratha.utils.StringUtils;

import org.ocpsoft.prettytime.PrettyTime;

import java.text.SimpleDateFormat;
import java.util.Date;


public class EntriesCursorAdapter extends ResourceCursorAdapter {

    private final Uri mUri;
    private final boolean mShowFeedInfo;
    private final CircleTransform mCircleTransform = new CircleTransform();
    private int mIdPos, mTitlePos, mMainImgPos, mDatePos, mIsReadPos, mFavoritePos, mFeedIdPos, mFeedIconPos, mFeedNamePos;

    public EntriesCursorAdapter(Context context, Uri uri, Cursor cursor, boolean showFeedInfo) {
        super(context, R.layout.item_entry_list, cursor, 0);
        mUri = uri;
        mShowFeedInfo = showFeedInfo;

        reinit(cursor);
    }

    @Override
    public void bindView(View view, final Context context, Cursor cursor) {
        if (view.getTag(R.id.holder) == null) {
            ViewHolder holder = new ViewHolder();
            holder.titleTextView = (TextView) view.findViewById(android.R.id.text1);
            holder.dateTextView = (TextView) view.findViewById(android.R.id.text2);
            holder.mainImgView = (ImageView) view.findViewById(R.id.main_icon);
            holder.starImgView = (ImageView) view.findViewById(R.id.favorite_icon);
            view.setTag(R.id.holder, holder);
        }

        final ViewHolder holder = (ViewHolder) view.getTag(R.id.holder);

        String titleText = cursor.getString(mTitlePos);
        holder.titleTextView.setText(titleText);

        final long feedId = cursor.getLong(mFeedIdPos);
        String feedName = cursor.getString(mFeedNamePos);

        String mainImgUrl = cursor.getString(mMainImgPos);
        mainImgUrl = TextUtils.isEmpty(mainImgUrl) ? null : NetworkUtils.getDownloadedOrDistantImageUrl(cursor.getLong(mIdPos), mainImgUrl);

        ColorGenerator generator = ColorGenerator.DEFAULT;
        int color = generator.getColor(Long.valueOf(feedId)); // The color is specific to the feedId (which shouldn't change)
        TextDrawable letterDrawable = TextDrawable.builder().buildRound((feedName != null ? feedName.substring(0, 1).toUpperCase() : ""), color);
        if (mainImgUrl != null) {
            // For Letter Drawable
           // Picasso.with(context).load(mainImgUrl).transform(mCircleTransform).placeholder(letterDrawable).error(letterDrawable).into(holder.mainImgView);

            // For display Nava Maratha Logo
            Picasso.with(context).load(mainImgUrl).transform(mCircleTransform).placeholder(R.drawable.llogo).error(R.drawable.llogo).into(holder.mainImgView);
        } else {
            Picasso.with(context).cancelRequest(holder.mainImgView);
            holder.mainImgView.setImageResource(R.drawable.llogo);
        }

        holder.isFavorite = cursor.getInt(mFavoritePos) == 1;

        holder.starImgView.setVisibility(holder.isFavorite ? View.VISIBLE : View.INVISIBLE);

        //TimeAgo timeAgo = new TimeAgo();
        //String result = timeAgo.getTimeAgo(YOUR_PAST_DATE);



        // Get Date from database
        Date begin  = new Date(cursor.getLong(mDatePos));

        // date convert to milliseconds
        long milli = begin.getTime();

     //   System.out.println("Date in milliseconds " + milli);

        // using pretty tim library and get time like today , yesterday and days ago.
        PrettyTime prettyTime = new PrettyTime();

        String noww = prettyTime.format( new Date (milli));

     //   System.out.println("Prettyt Time Date " + noww);

        if (mShowFeedInfo && mFeedNamePos > -1) {
            if (feedName != null) {
             //   holder.dateTextView.setText(Html.fromHtml(new StringBuilder("<font color='#247ab0' size = 20>").append(feedName).append("</font>").append(Constants.COMMA_SPACE).append(StringUtils.getDateTimeString(cursor.getLong(mDatePos))).toString()));
                holder.dateTextView.setText(Html.fromHtml(new StringBuilder("<font color='#247ab0'>").append(feedName).append("</font>").append(Constants.COMMA_SPACE).append(noww).toString()));
            } else {
                holder.dateTextView.setText(noww);
            }
        } else {
            holder.dateTextView.setText(noww);
        }

        if (cursor.isNull(mIsReadPos)) {
            holder.titleTextView.setEnabled(true);
            holder.titleTextView.setTextColor(Color.BLACK);
            holder.dateTextView.setEnabled(true);
            holder.isRead = false;
        } else {
            holder.titleTextView.setEnabled(false);
            holder.titleTextView.setTextColor(Color.LTGRAY);
            holder.dateTextView.setEnabled(false);
            holder.isRead = true;
        }
    }

    public void toggleReadState(final long id, View view) {
        final ViewHolder holder = (ViewHolder) view.getTag(R.id.holder);

        if (holder != null) { // should not happen, but I had a crash with this on PlayStore...
            holder.isRead = !holder.isRead;

            if (holder.isRead) {
                holder.titleTextView.setEnabled(false);
                holder.titleTextView.setTextColor(Color.LTGRAY);
                holder.dateTextView.setEnabled(false);
            } else {
                holder.titleTextView.setEnabled(true);
                holder.titleTextView.setTextColor(Color.BLACK);
                holder.dateTextView.setEnabled(true);
            }

            new Thread() {
                @Override
                public void run() {
                    ContentResolver cr = MainApplication.getContext().getContentResolver();
                    Uri entryUri = ContentUris.withAppendedId(mUri, id);
                    cr.update(entryUri, holder.isRead ? FeedData.getReadContentValues() : FeedData.getUnreadContentValues(), null, null);
                }
            }.start();
        }
    }

    public void toggleFavoriteState(final long id, View view) {
        final ViewHolder holder = (ViewHolder) view.getTag(R.id.holder);

        if (holder != null) { // should not happen, but I had a crash with this on PlayStore...
            holder.isFavorite = !holder.isFavorite;

            if (holder.isFavorite) {
                holder.starImgView.setVisibility(View.VISIBLE);
            } else {
                holder.starImgView.setVisibility(View.INVISIBLE);
            }

            new Thread() {
                @Override
                public void run() {
                    ContentValues values = new ContentValues();
                    values.put(EntryColumns.IS_FAVORITE, holder.isFavorite ? 1 : 0);

                    ContentResolver cr = MainApplication.getContext().getContentResolver();
                    Uri entryUri = ContentUris.withAppendedId(mUri, id);
                    cr.update(entryUri, values, null, null);
                }
            }.start();
        }
    }

    public void markAllAsRead(final long untilDate) {
        new Thread() {
            @Override
            public void run() {
                ContentResolver cr = MainApplication.getContext().getContentResolver();
                String where = EntryColumns.WHERE_UNREAD + Constants.DB_AND + '(' + EntryColumns.FETCH_DATE + Constants.DB_IS_NULL + Constants.DB_OR + EntryColumns.FETCH_DATE + "<=" + untilDate + ')';
                cr.update(mUri, FeedData.getReadContentValues(), where, null);
            }
        }.start();
    }

    @Override
    public void changeCursor(Cursor cursor) {
        reinit(cursor);
        super.changeCursor(cursor);
    }

    @Override
    public Cursor swapCursor(Cursor newCursor) {
        reinit(newCursor);
        return super.swapCursor(newCursor);
    }

    @Override
    public void notifyDataSetChanged() {
        reinit(null);
        super.notifyDataSetChanged();
    }

    @Override
    public void notifyDataSetInvalidated() {
        reinit(null);
        super.notifyDataSetInvalidated();
    }


    private void reinit(Cursor cursor) {
        if (cursor != null && cursor.getCount() > 0) {
            mIdPos = cursor.getColumnIndex(EntryColumns._ID);
            mTitlePos = cursor.getColumnIndex(EntryColumns.TITLE);
            mMainImgPos = cursor.getColumnIndex(EntryColumns.IMAGE_URL);
            mDatePos = cursor.getColumnIndex(EntryColumns.DATE);
            mIsReadPos = cursor.getColumnIndex(EntryColumns.IS_READ);
            mFavoritePos = cursor.getColumnIndex(EntryColumns.IS_FAVORITE);
            mFeedNamePos = cursor.getColumnIndex(FeedColumns.NAME);
            mFeedIdPos = cursor.getColumnIndex(EntryColumns.FEED_ID);
            mFeedIconPos = cursor.getColumnIndex(FeedColumns.ICON);
        }
    }

    private static class ViewHolder {
        public TextView titleTextView;
        public TextView dateTextView;
        public ImageView mainImgView;
        public ImageView starImgView;
        public boolean isRead, isFavorite;
    }
}
