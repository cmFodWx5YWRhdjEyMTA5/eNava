
package  com.enavamaratha.enavamaratha.adapter;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.ResourceCursorAdapter;
import android.widget.TextView;

import  com.enavamaratha.enavamaratha.R;
import  com.enavamaratha.enavamaratha.provider.FeedData.FilterColumns;
import  com.enavamaratha.enavamaratha.utils.PrefUtils;

public class FiltersCursorAdapter extends ResourceCursorAdapter {

    private int mFilterTextColumnPosition;
    private int mIsAppliedToTitleColumnPosition;
    private int mIsAcceptRulePosition;
    private int mSelectedFilter = -1;
    private boolean isZero = false;

    public FiltersCursorAdapter(Context context, Cursor cursor) {
        super(context, R.layout.item_rule_list, cursor, 0);
        reinit(cursor);
    }

    @Override
    public void bindView(View view, final Context context, Cursor cursor) {
        TextView isAcceptRuleTextView = (TextView) view.findViewById(android.R.id.text1);
        TextView filterTextTextView = (TextView) view.findViewById(android.R.id.text2);
        TextView isAppliedToTitleTextView = (TextView) view.findViewById(R.id.text3);

        if (cursor.getPosition() == mSelectedFilter) {
            //view.setBackgroundResource(PrefUtils.getBoolean(PrefUtils.LIGHT_THEME, true) ? R.color.light_accent_color : R.color.dark_accent_color);
            view.setBackgroundResource( R.color.light_accent_color );
        } else {
            view.setBackgroundResource(android.R.color.transparent);
        }

        boolean isAcceptRule = cursor.getInt(mIsAcceptRulePosition) == 1;
        isAcceptRuleTextView.setText(isAcceptRule ? R.string.accept : R.string.reject);
        isAcceptRuleTextView.setTextColor(isAcceptRule ? ContextCompat.getColor(context, R.color.green) :
                ContextCompat.getColor(context, R.color.red));
        filterTextTextView.setText(cursor.getString(mFilterTextColumnPosition));
        isAppliedToTitleTextView.setText(cursor.getInt(mIsAppliedToTitleColumnPosition) == 1 ? R.string.filter_apply_to_title : R.string.filter_apply_to_content);

        Activity mActivity = (context instanceof Activity) ? (Activity) context : null;
        if (mActivity != null) {
            if (!isZero) {
                mActivity.findViewById(R.id.empty).setVisibility(View.GONE);
            } else {
                mActivity.findViewById(R.id.empty).setVisibility(View.VISIBLE);
            }
        }
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
        if(cursor != null) {
            if (cursor.getCount() > 0) {
                mFilterTextColumnPosition = cursor.getColumnIndex(FilterColumns.FILTER_TEXT);
                mIsAppliedToTitleColumnPosition = cursor.getColumnIndex(FilterColumns.IS_APPLIED_TO_TITLE);
                mIsAcceptRulePosition = cursor.getColumnIndex(FilterColumns.IS_ACCEPT_RULE);
                isZero = false;
            } else {
                isZero = true;
            }
        }
    }

    public int getSelectedFilter() {
        return mSelectedFilter;
    }

    public void setSelectedFilter(int filterPos) {
        mSelectedFilter = filterPos;
    }
}
