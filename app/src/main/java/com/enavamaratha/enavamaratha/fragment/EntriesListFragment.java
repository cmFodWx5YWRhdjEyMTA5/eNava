package  com.enavamaratha.enavamaratha.fragment;

import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.ListView;
import android.widget.TextView;

//import com.enavamaratha.enavamaratha.view.SwipeRefreshLayout;
import com.melnykov.fab.FloatingActionButton;

import  com.enavamaratha.enavamaratha.Constants;
import  com.enavamaratha.enavamaratha.R;
import  com.enavamaratha.enavamaratha.activity.HomeActivity;
import  com.enavamaratha.enavamaratha.adapter.EntriesCursorAdapter;
import  com.enavamaratha.enavamaratha.provider.FeedData;
import  com.enavamaratha.enavamaratha.provider.FeedData.EntryColumns;
import  com.enavamaratha.enavamaratha.provider.FeedDataContentProvider;
import  com.enavamaratha.enavamaratha.service.FetcherService;
import  com.enavamaratha.enavamaratha.utils.PrefUtils;
import  com.enavamaratha.enavamaratha.utils.UiUtils;

import java.util.Arrays;
import java.util.Date;

public class EntriesListFragment extends SwipeRefreshListFragment {

    private static final String STATE_URI = "STATE_URI";
    private static final String STATE_SHOW_FEED_INFO = "STATE_SHOW_FEED_INFO";
    private static final String STATE_LIST_DISPLAY_DATE = "STATE_LIST_DISPLAY_DATE";

    private static final int ENTRIES_LOADER_ID = 1;
    private static final int NEW_ENTRIES_NUMBER_LOADER_ID = 2;

    private Uri mUri;
    private boolean mShowFeedInfo = false;
    private EntriesCursorAdapter mEntriesCursorAdapter;
    private ListView mListView;
    private SearchView mSearchView;
    private FloatingActionButton mHideReadButton;
    private EntriesListFragment mEntriesFragment;
    private long mListDisplayDate = new Date().getTime();
    private Menu menu;
    private SearchView searchView = null;
    private SearchView.OnQueryTextListener queryTextListener;
    private String extra;
    private long entryid;
    public static final int REFRESH_DELAY = 2000;
    //private SwipeRefreshLayout swipe;


    private final LoaderManager.LoaderCallbacks<Cursor> mEntriesLoader = new LoaderManager.LoaderCallbacks<Cursor>()
    {
        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            String entriesOrder = PrefUtils.getBoolean(PrefUtils.DISPLAY_OLDEST_FIRST, false) ? Constants.DB_ASC : Constants.DB_DESC;
            String where = "(" + EntryColumns.FETCH_DATE + Constants.DB_IS_NULL + Constants.DB_OR + EntryColumns.FETCH_DATE + "<=" + mListDisplayDate + ')';
            if (!FeedData.shouldShowReadEntries(mUri))
            {
                where += Constants.DB_AND + EntryColumns.WHERE_UNREAD;
            }

            CursorLoader cursorLoader = new CursorLoader(getActivity(), mUri, null, where, null, EntryColumns.DATE + entriesOrder);

            cursorLoader.setUpdateThrottle(150);
            return cursorLoader;
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
            mEntriesCursorAdapter.swapCursor(data);
        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {
            mEntriesCursorAdapter.swapCursor(Constants.EMPTY_CURSOR);
        }
    };
    private final OnSharedPreferenceChangeListener mPrefListener = new OnSharedPreferenceChangeListener() {
        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            if (PrefUtils.SHOW_READ.equals(key)) {
                getLoaderManager().restartLoader(ENTRIES_LOADER_ID, null, mEntriesLoader);
                UiUtils.updateHideReadButton(mHideReadButton);
            } else if (PrefUtils.IS_REFRESHING.equals(key)) {
                refreshSwipeProgress();
            }
        }
    };
    private int mNewEntriesNumber, mOldUnreadEntriesNumber = -1;
    private boolean mAutoRefreshDisplayDate = false;
    private final LoaderManager.LoaderCallbacks<Cursor> mEntriesNumberLoader = new LoaderManager.LoaderCallbacks<Cursor>() {
        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            CursorLoader cursorLoader = new CursorLoader(getActivity(), mUri, new String[]{"SUM(" + EntryColumns.FETCH_DATE + '>' + mListDisplayDate + ")", "SUM(" + EntryColumns.FETCH_DATE + "<=" + mListDisplayDate + Constants.DB_AND + EntryColumns.WHERE_UNREAD + ")"}, null, null, null);
            cursorLoader.setUpdateThrottle(150);
            return cursorLoader;
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
            data.moveToFirst();
            mNewEntriesNumber = data.getInt(0);
            mOldUnreadEntriesNumber = data.getInt(1);

            if (mAutoRefreshDisplayDate && mNewEntriesNumber != 0 && mOldUnreadEntriesNumber == 0) {
                mListDisplayDate = new Date().getTime();
                restartLoaders();
            } else {
                refreshUI();
            }

            ((HomeActivity)getActivity()).refreshTitle(mNewEntriesNumber);



            mAutoRefreshDisplayDate = false;
        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null)
        {
            mUri = savedInstanceState.getParcelable(STATE_URI);
            mShowFeedInfo = savedInstanceState.getBoolean(STATE_SHOW_FEED_INFO);
            mListDisplayDate = savedInstanceState.getLong(STATE_LIST_DISPLAY_DATE);

            mEntriesCursorAdapter = new EntriesCursorAdapter(getActivity(), mUri, Constants.EMPTY_CURSOR, mShowFeedInfo);
        }


    }

    @Override
    public void onStart() {
        super.onStart();
        refreshSwipeProgress();
        PrefUtils.registerOnPrefChangeListener(mPrefListener);

        if (mUri != null) {
            // If the list is empty when we are going back here, try with the last display date
            if (mNewEntriesNumber != 0 && mOldUnreadEntriesNumber == 0) {
                mListDisplayDate = new Date().getTime();
            } else {
                mAutoRefreshDisplayDate = true; // We will try to update the list after if necessary
            }

            restartLoaders();
        }
    }

    @Override
    public View inflateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_entry_list, container, true);

        if (mEntriesCursorAdapter != null) {
            setListAdapter(mEntriesCursorAdapter);
        }


        mListView = (ListView) rootView.findViewById(android.R.id.list);
        //mSwipeL =(SwipeRefreshLayout)rootView.findViewById(R.id.swipe);
        mListView.setOnTouchListener(new SwipeGestureListener(mListView.getContext()));
        //mWave = (WaveSwipeRefreshLayout)rootView.findViewById(R.id.WaveRefresh);


        if (PrefUtils.getBoolean(PrefUtils.DISPLAY_TIP, true)) {
            final TextView header = new TextView(mListView.getContext());
            header.setMinimumHeight(UiUtils.dpToPixel(70));
            int footerPadding = UiUtils.dpToPixel(10);
            header.setPadding(footerPadding, footerPadding, footerPadding, footerPadding);
            header.setText(R.string.tip_sentence);
            header.setGravity(Gravity.CENTER_VERTICAL);
            header.setCompoundDrawablePadding(UiUtils.dpToPixel(5));
            header.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_info_outline, 0, R.drawable.ic_cancel, 0);
            header.setClickable(true);
            header.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mListView.removeHeaderView(header);
                    PrefUtils.putBoolean(PrefUtils.DISPLAY_TIP, false);
                }
            });
            mListView.addHeaderView(header);
        }




        /*mListView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

                int topRowVerticalPosition =
                        (mListView == null || mListView.getChildCount() == 0) ? 0 : mListView.getChildAt(0).getTop();
               swipe.setEnabled(firstVisibleItem == 0 && topRowVerticalPosition >= 0);
            }
        });*/



      /*  mPullRefresh.setOnRefreshListener(new PullToRefreshView.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mPullRefresh.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mPullRefresh.setRefreshing(false);
                    }
                },REFRESH_DELAY);
            }
        })*/;





        UiUtils.addEmptyFooterView(mListView, 50);



      /*  mHideReadButton = (FloatingActionButton) rootView.findViewById(R.id.hide_read_button);
      mHideReadButton.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                UiUtils.displayHideReadButtonAction(mListView.getContext());
                return true;
            }
        });
        UiUtils.updateHideReadButton(mHideReadButton);*/

        mSearchView = (SearchView) rootView.findViewById(R.id.searchView);
        mSearchView.setQueryHint("Type Search Text Here...");

        if (savedInstanceState != null) {
            refreshUI(); // To hide/show the search bar
        }

        mSearchView.post(new Runnable() { // Do this AFTER the text has been restored from saveInstanceState
            @Override
            public void run() {
                mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                    @Override
                    public boolean onQueryTextSubmit(String s) {
                        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(mSearchView.getWindowToken(), 0);
                        return false;
                    }

                    @Override
                    public boolean onQueryTextChange(String s) {
                        setData(EntryColumns.SEARCH_URI(s), true);
                        return false;
                    }
                });
            }
        });

        disableSwipe();

        return rootView;
    }

    @Override
    public void onStop() {
        PrefUtils.unregisterOnPrefChangeListener(mPrefListener);
        refreshUI();
        super.onStop();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelable(STATE_URI, mUri);
        outState.putBoolean(STATE_SHOW_FEED_INFO, mShowFeedInfo);
        outState.putLong(STATE_LIST_DISPLAY_DATE, mListDisplayDate);
        refreshUI();
        super.onSaveInstanceState(outState);
    }
    @Override
    public void onRefresh() {
        startRefresh();
    }
    /*@Override
    public void onRefresh() {
        startRefresh();
    }

    @Override
    public void onPullDistance(int distance) {

    }

    @Override
    public void onPullEnable(boolean enable) {

    }*/

    public void onClickHideRead(View view) {
        if (!PrefUtils.getBoolean(PrefUtils.SHOW_READ, true)) {
            PrefUtils.putBoolean(PrefUtils.SHOW_READ, true);
        } else {
            PrefUtils.putBoolean(PrefUtils.SHOW_READ, false);
        }
    }

    @Override
    public void onListItemClick(ListView listView, View view, int position, long id) {
        if (id >= 0) { // should not happen, but I had a crash with this on PlayStore...
            startActivity(new Intent(Intent.ACTION_VIEW, ContentUris.withAppendedId(mUri, id)));
            System.out.println("Uri is of item click is" + mUri);
            System.out.println("Id Of List item" + id);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
    {
        this.menu = menu;
        menu.clear(); // This is needed to remove a bug on Android 4.0.3
        inflater.inflate(R.menu.entry_list, menu);


        if (!PrefUtils.getBoolean(PrefUtils.MARK_AS_READ, false))
        {
            menu.findItem(R.id.menu_all_read).setVisible(true);
        }
        if (EntryColumns.FAVORITES_CONTENT_URI.equals(mUri))
        {
            menu.findItem(R.id.menu_refresh).setVisible(true);
        } else
        {
            menu.findItem(R.id.menu_share_starred).setVisible(false);

        }


        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Uri newUri = null;
        switch (item.getItemId()) {


            case R.id.menu_home:
            {
                Intent intee = new Intent(getActivity(),HomeActivity.class);
                intee.putExtra("home","home");
                startActivity(intee);
                return true;
            }

            case R.id.menu_share_starred: {
                if (mEntriesCursorAdapter != null) {
                    String starredList = "";
                    Cursor cursor = mEntriesCursorAdapter.getCursor();
                    if (cursor != null && !cursor.isClosed()) {
                        int titlePos = cursor.getColumnIndex(EntryColumns.TITLE);
                        int linkPos = cursor.getColumnIndex(EntryColumns.LINK);
                        if (cursor.moveToFirst()) {
                            do {
                                starredList += cursor.getString(titlePos) + "\n" + cursor.getString(linkPos) + "\n\n";
                            } while (cursor.moveToNext());
                        }
                        startActivity(Intent.createChooser(
                                new Intent(Intent.ACTION_SEND).putExtra(Intent.EXTRA_SUBJECT, getString(R.string.share_favorites_title))
                                        .putExtra(Intent.EXTRA_TEXT, starredList).setType(Constants.MIMETYPE_TEXT_PLAIN), getString(R.string.menu_share)
                        ));
                    }
                }
                return true;
            }
            case R.id.menu_refresh: {
                downloadUnmobilitedEntries();
                startRefresh();
                return true;
            }
            case R.id.menu_all_read: {
                if (mEntriesCursorAdapter != null) {
                    mEntriesCursorAdapter.markAllAsRead(mListDisplayDate);

                    // If we are on "all items" uri, we can remove the notification here
                    if (mUri != null && EntryColumns.CONTENT_URI.equals(mUri) && Constants.NOTIF_MGR != null) {
                        Constants.NOTIF_MGR.cancel(0);
                    }
                }
                return true;
            }

        }
        searchView.setOnQueryTextListener(queryTextListener);
        return super.onOptionsItemSelected(item);

    }
    /**
     * Schedules all entries which are not mobilized yet, for download.
     * Then invokes download by calling FetcherService.
     */
    private void downloadUnmobilitedEntries() {
        Cursor cursor = mEntriesCursorAdapter.getCursor();
        if (cursor != null && !cursor.isClosed()) {
            int mobilizedPos = cursor.getColumnIndex(EntryColumns.MOBILIZED_HTML);
            long[] entries = new long[cursor.getCount()];
            int i = 0;
            if (cursor.moveToFirst()) {
                do {
                    if (cursor.isNull(mobilizedPos)) {
                        entries[i++] = cursor.getLong(0);
                        //not mobilized, yet
//                        entries[i++] = (Long.valueOf(cursor.getPosition()));
                    }
                } while (cursor.moveToNext());
            }
            if(i > 0) {
                entries = Arrays.copyOf(entries, i);
                FetcherService.addEntriesToMobilize(entries);
                getActivity().startService(new Intent(getActivity(), FetcherService.class).setAction(FetcherService.ACTION_MOBILIZE_FEEDS));
            }
        }
    }
    public void startRefresh() {
        if (!PrefUtils.getBoolean(PrefUtils.IS_REFRESHING, false))
        {
            if (mUri != null && FeedDataContentProvider.URI_MATCHER.match(mUri) == FeedDataContentProvider.URI_ENTRIES_FOR_FEED)
            {
                Log.i("EntriesListFragment","Calling mURI loop : "+mUri);

                getActivity().startService(new Intent(getActivity(), FetcherService.class).setAction(FetcherService.ACTION_REFRESH_FEEDS).putExtra(Constants.FEED_ID,
                        mUri.getPathSegments().get(1)));
            } else
            {
                Log.i("EntriesListFragment","Calling Without mUri Loop  : ");
                getActivity().startService(new Intent(getActivity(), FetcherService.class).setAction(FetcherService.ACTION_REFRESH_FEEDS));
            }
        }
        refreshSwipeProgress();
    }

    public Uri getUri() {
        return mUri;
    }

    public String getCurrentSearch() {
        return mSearchView == null ? null : mSearchView.getQuery().toString();
    }

    public void setData(Uri uri, boolean showFeedInfo) {
        mUri = uri;
        mShowFeedInfo = showFeedInfo;

        mEntriesCursorAdapter = new EntriesCursorAdapter(getActivity(), mUri, Constants.EMPTY_CURSOR, mShowFeedInfo);
        setListAdapter(mEntriesCursorAdapter);

        mListDisplayDate = new Date().getTime();
        if (mUri != null) {
            restartLoaders();
        }
        refreshUI();
    }

    private void restartLoaders() {
        LoaderManager loaderManager = getLoaderManager();
        loaderManager.restartLoader(ENTRIES_LOADER_ID, null, mEntriesLoader);
        loaderManager.restartLoader(NEW_ENTRIES_NUMBER_LOADER_ID, null, mEntriesNumberLoader);
    }

    private void refreshUI() {
        if (mUri != null && FeedDataContentProvider.URI_MATCHER.match(mUri) == FeedDataContentProvider.URI_SEARCH) {
            mSearchView.setVisibility(View.VISIBLE);
        } else {
            mSearchView.setVisibility(View.GONE);
        }
    }

    private void refreshSwipeProgress() {
        if (PrefUtils.getBoolean(PrefUtils.IS_REFRESHING, false)) {
            showSwipeProgress();
        } else {
            hideSwipeProgress();
        }
    }




    private class SwipeGestureListener extends SimpleOnGestureListener implements OnTouchListener {
        static final int SWIPE_MIN_DISTANCE = 120;
        static final int SWIPE_MAX_OFF_PATH = 150;
        static final int SWIPE_THRESHOLD_VELOCITY = 150;

        private final GestureDetector mGestureDetector;

        public SwipeGestureListener(Context context) {
            mGestureDetector = new GestureDetector(context, this);
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            if (mListView != null && e1 != null && e2 != null && Math.abs(e1.getY() - e2.getY()) <= SWIPE_MAX_OFF_PATH && Math.abs(velocityX) >= SWIPE_THRESHOLD_VELOCITY) {
                long id = mListView.pointToRowId(Math.round(e2.getX()), Math.round(e2.getY()));
                int position = mListView.pointToPosition(Math.round(e2.getX()), Math.round(e2.getY()));
                View view = mListView.getChildAt(position - mListView.getFirstVisiblePosition());

                if (view != null) {
                    // Just click on views, the adapter will do the real stuff
                    if (e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE) {
                        mEntriesCursorAdapter.toggleReadState(id, view);
                    } else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE) {
                        mEntriesCursorAdapter.toggleFavoriteState(id, view);
                    }

                    // Just simulate a CANCEL event to remove the item highlighting
                    mListView.post(new Runnable() { // In a post to avoid a crash on 4.0.x
                        @Override
                        public void run() {
                            MotionEvent motionEvent = MotionEvent.obtain(0, 0, MotionEvent.ACTION_CANCEL, 0, 0, 0);
                            mListView.dispatchTouchEvent(motionEvent);
                            motionEvent.recycle();
                        }
                    });
                    return true;
                }
            }

            return super.onFling(e1, e2, velocityX, velocityY);
        }

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            return mGestureDetector.onTouchEvent(event);
        }
    }


}
