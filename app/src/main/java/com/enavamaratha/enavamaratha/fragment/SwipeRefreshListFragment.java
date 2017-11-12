
package  com.enavamaratha.enavamaratha.fragment;

import android.app.ListFragment;
import android.os.Bundle;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ListView;

import com.enavamaratha.enavamaratha.R;
import com.enavamaratha.enavamaratha.view.SwipeRefreshLayout;
//import com.enavamaratha.enavamaratha.view.SwipeRefreshLayout;


public abstract class SwipeRefreshListFragment extends ListFragment implements SwipeRefreshLayout.OnRefreshListener {

    private SwipeRefreshLayout mRefreshLayout;


   
    private ListView mListView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mRefreshLayout = new SwipeRefreshLayout(inflater.getContext())

          {
             @Override
            public boolean canChildScrollUp() {
                return mListView != null && mListView.getFirstVisiblePosition() != 0;
            }
        };
        inflateView(inflater, mRefreshLayout, savedInstanceState);

        mListView = (ListView) mRefreshLayout.findViewById(android.R.id.list);
        if (mListView != null) {
            // HACK to be able to know when we are on the top of the list (for the swipe refresh)
            mListView.addHeaderView(new View(mListView.getContext()));
        }

        return mRefreshLayout;
    }

       /* mCircleRefreshLayout = new SwipeRefreshL(inflater.getContext()) {
            @Override
            public boolean canChildScrollUp() {
                return mListView != null && mListView.getFirstVisiblePosition() != 0;
            }
        };
        inflateView(inflater, mCircleRefreshLayout, savedInstanceState);

        mListView = (ListView) mCircleRefreshLayout.findViewById(android.R.id.list);
        if (mListView != null) {
            // HACK to be able to know when we are on the top of the list (for the swipe refresh)
            mListView.addHeaderView(new View(mListView.getContext()));
        }

        return mCircleRefreshLayout;
    }*/

    abstract public View inflateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState);

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        mRefreshLayout.setColorScheme(R.color.Indigo_300,
                R.color.red,
                R.color.Indigo_900,
                R.color.Indigo_400);
        mRefreshLayout.setOnRefreshListener(this);

        mListView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                int topRowVerticalPosition =
                        (mListView == null || mListView.getChildCount() == 0) ?
                                0 : mListView.getChildAt(0).getTop();
                mRefreshLayout.setEnabled(firstVisibleItem == 0 && topRowVerticalPosition >= 0);
            }
        });

        }

    /**
     * It shows the SwipeRefreshLayout progress
     */
    public void showSwipeProgress() {
        mRefreshLayout.setRefreshing(true);
       // mCircleRefreshLayout.setRefreshing(true);
    }

    /**
     * It shows the SwipeRefreshLayout progress
     */
    public void hideSwipeProgress() {
        mRefreshLayout.setRefreshing(false);
        //mCircleRefreshLayout.setRefreshing(false);
    }

    /**
     * Enables swipe gesture
     */
    public void enableSwipe() {
        mRefreshLayout.setEnabled(true);
       // mCircleRefreshLayout.setEnabled(true);
    }

    /**
     * Disables swipe gesture. It prevents manual gestures but keeps the option tu show
     * refreshing programatically.
     */
    public void disableSwipe() {
       mRefreshLayout.setEnabled(false);
        //mCircleRefreshLayout.setEnabled(false);
    }

    /**
     * Get the refreshing status
     */
    public boolean isRefreshing() {
        return mRefreshLayout.isRefreshing();
        //return mCircleRefreshLayout.isRefreshing();
    }

}