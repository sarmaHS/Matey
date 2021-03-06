package com.mateyinc.marko.matey.activity.home;

import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.mateyinc.marko.matey.R;
import com.mateyinc.marko.matey.activity.NewPostActivity;
import com.mateyinc.marko.matey.adapters.BulletinsAdapter;
import com.mateyinc.marko.matey.data.DataContract.BulletinEntry;
import com.mateyinc.marko.matey.internet.OperationManager;

import java.util.ArrayList;
import java.util.Iterator;

import static com.mateyinc.marko.matey.data.DataAccess.BULLETIN_COLUMNS;

public class BulletinsFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>, Response.Listener<String>, Response.ErrorListener {
    public static final int BULLETINS_LOADER = 100;

    private HomeActivity mContext;
    private BulletinsAdapter mAdapter;
    private BroadcastReceiver mDataDownloaded;
    private RecyclerView mRecycleView;
    private EndlessScrollListener mScrollListener;

    /**
     * The list which hold the updated bulletin positions;
     * Used to notify adapter about update;
     */
    public static ArrayList<Integer> mUpdatedPositions = new ArrayList<>();

    private LinearLayout llNoData, rlNewPostView;
    private SwipeRefreshLayout mRefreshLayout;
    private ProgressBar mProgressBar;
    private CoordinatorLayout mMainFeedLayout;
    private OperationManager mOperationManager;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public BulletinsFragment() {
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = (HomeActivity) context;
        mOperationManager = OperationManager.getInstance(context);
        mOperationManager.addSuccessListener(this);
        mOperationManager.addErrorListener(this);

        getLoaderManager().initLoader(BULLETINS_LOADER, null, this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mMainFeedLayout = (CoordinatorLayout) inflater.inflate(R.layout.fragment_bulletins, container, false);

        // Programmatically creating recycle view
        mRecycleView = new RecyclerView(getContext());
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        mRecycleView.setLayoutParams(params);
        mAdapter = new BulletinsAdapter(mContext);

        // Set the adapter
        Context context = mMainFeedLayout.getContext();
        LinearLayoutManager layoutManager = new LinearLayoutManager(context);
        mRecycleView.setLayoutManager(layoutManager);
        mRecycleView.setAdapter(mAdapter);

        // Add the view
        mRefreshLayout = (SwipeRefreshLayout) mMainFeedLayout.findViewById(R.id.swiperefresh);
        mRefreshLayout.addView(mRecycleView);
        mRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mOperationManager.downloadNewsFeed(true, mContext);
            }
        });

        mScrollListener = new EndlessScrollListener(layoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount) {
                mOperationManager.downloadNewsFeed(mContext);
            }
        };

        mMainFeedLayout.findViewById(R.id.fabNewPost).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, NewPostActivity.class);
                mContext.startActivity(intent);
            }
        });

        return mMainFeedLayout;
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d("BulletinsFragment", "onResume is called.");

        // If items are updated, notify adapter
        if (mUpdatedPositions.size() != 0) {
            Iterator i = mUpdatedPositions.iterator();
            while (i.hasNext()) {
                int pos = (Integer) i.next();
                mAdapter.notifyItemChanged(pos);
                i.remove();
            }
        }

        // Settings list scroll listener
        mRecycleView.addOnScrollListener(mScrollListener);

        // Whole list downloaded broadcast
        LocalBroadcastManager.getInstance(mContext).registerReceiver(mDataDownloaded = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                OperationManager.mCurrentPage++; // Update curPage
//                mScrollListener.mLoading = false;
                Log.d("BulletinsFragment", "Bulletin downloaded broadcast received. Current page=" + OperationManager.mCurrentPage);
            }
        }, new IntentFilter(OperationManager.BULLETIN_LIST_LOADED));
    }

    @Override
    public void onPause() {
        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(mDataDownloaded);
        mRecycleView.removeOnScrollListener(mScrollListener); // Removing scroll listener

        super.onPause();
    }

    @Override
    public void onResponse(String response) {
        mScrollListener.onDownloadFinished();
        mRefreshLayout.setRefreshing(false);
    }

    @Override
    public void onErrorResponse(VolleyError error) {
        mScrollListener.onDownloadFinished();
        mRefreshLayout.setRefreshing(false);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(getActivity(),
                BulletinEntry.CONTENT_URI,
                BULLETIN_COLUMNS,
                null, null,
                BulletinEntry.COLUMN_DATE + BulletinEntry.DEFAULT_SORT);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        updateEmptyView(data.getCount());
        mAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.swapCursor(null);
    }


    private final static String EMPTY_VIEW_TAG = "emptyview";

    /**
     * Show an 'empty' view when there's no data to display
     */
    private void updateEmptyView(int dataCount) {
        if (dataCount == 0) {
            // If there is no data, add empty view to the layout
            TextView noDataTV = new TextView(mMainFeedLayout.getContext());
            noDataTV.setTag(EMPTY_VIEW_TAG);
            noDataTV.setGravity(Gravity.CENTER);
            ViewGroup.LayoutParams params = new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            noDataTV.setLayoutParams(params);
            noDataTV.setText(getString(R.string.empty_list));

            mMainFeedLayout.addView(noDataTV);
        } else {
            // If there is data we need to check if there was an empty view already added to the layout so we remove it
            View view = mMainFeedLayout.findViewWithTag(EMPTY_VIEW_TAG);
            if (view != null) {
                mMainFeedLayout.removeView(view);
            }
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case BulletinsAdapter.COPYTEXT_ID:
                ClipboardManager clipboard = (ClipboardManager) mContext.getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText(getString(R.string.clipboard_copiedtext_label), BulletinsAdapter.clickedText);
                clipboard.setPrimaryClip(clip);
                Toast.makeText(mContext, R.string.toast_textcopied, Toast.LENGTH_LONG).show();
                break;
        }
        return super.onContextItemSelected(item);
    }


}
