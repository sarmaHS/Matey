package com.mateyinc.marko.matey.activity.adapters;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.mateyinc.marko.matey.R;
import com.mateyinc.marko.matey.activity.Util;
import com.mateyinc.marko.matey.activity.home.HomeActivity;
import com.mateyinc.marko.matey.activity.profile.ProfileActivity;
import com.mateyinc.marko.matey.activity.rounded_image_view.RoundedImageView;
import com.mateyinc.marko.matey.activity.view.BulletinRepliesViewActivity;
import com.mateyinc.marko.matey.data_and_managers.BulletinManager;
import com.mateyinc.marko.matey.model.Bulletin;

import java.util.ArrayList;
import java.util.Date;

public class BulletinRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final ArrayList<Bulletin> mData;
    private final Context mContext;
    private final BulletinManager mManager;
    private static final int FIRST_ITEM = 1;
    private static final int ITEM = 2;

    public BulletinRecyclerViewAdapter(Context context) {
        mContext = context;
        mManager = BulletinManager.getInstance(context);
        mData = mManager.getBulletinList();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case ITEM: {
                View view = LayoutInflater.from(mContext)
                        .inflate(R.layout.bulletin_list_item, parent, false);
                return new ViewHolder(view);
            }
            case FIRST_ITEM: {
                View view = LayoutInflater.from(mContext)
                        .inflate(R.layout.bulletin_first_list_item, parent, false);
                return new ViewHolderFirst(view);
            }
            default:
                return null;
        }
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder mHolder, final int position) {
        switch (getItemViewType(position)) {
            // Parsing data to views if available
            case ITEM: {
                BulletinRecyclerViewAdapter.ViewHolder holder = (ViewHolder) mHolder;
                Bulletin bulletin = mManager.getBulletin(position);
                try {
                    holder.mMessage.setText(bulletin.getMessage());
                } catch (Exception e) {
                    Log.e(this.getClass().getSimpleName(), e.getLocalizedMessage(), e);
                    holder.mMessage.setText(mContext.getString(R.string.error_message));
                }
                try {
                    holder.mName.setText(bulletin.getFirstName() + " " + mManager.getBulletin(position).getLastName());
                } catch (Exception e) {
                    Log.e(this.getClass().getSimpleName(), e.getLocalizedMessage(), e);
                    holder.mName.setText(mContext.getString(R.string.error_message));
                }
                try {
                    holder.mDate.setText(Util.getReadableDateText(bulletin.getDate()));
                } catch (Exception e) {
                    Log.e(this.getClass().getSimpleName(), e.getLocalizedMessage(), e);
                    holder.mDate.setText(Util.getReadableDateText(new Date()));
                }

                // Adding replies programmatically
                try {
                    Resources resources = mContext.getResources();
                    int repliesCount = bulletin.getReplies().size();
                    int margin = 0;
                    int marginIncrease = Util.getDp(15, resources);
                    int height = Util.getDp(24, resources);

                    // Adding image view
                    RoundedImageView imageView = null;
                    for (int i = 0; i < repliesCount; i++) {
                        if (i == 3) break; // Add no more than 3 views
                        imageView = new RoundedImageView(mContext);

                        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(height, height);
                        params.addRule(RelativeLayout.CENTER_VERTICAL);
                        params.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
                        params.addRule(RelativeLayout.ALIGN_PARENT_START);
                        params.leftMargin = margin;
                        margin += marginIncrease; // increasing margin for next view

                        imageView.setImageResource(R.drawable.empty_photo);
                        imageView.setOval(true);
                        imageView.setBorderWidth(0);
                        imageView.setLayoutParams(params);
                        imageView.setId(i);

                        holder.rlReplies.addView(imageView);
                    }


                    // Adding text view
                    TextView textView = null;
                    if (repliesCount > 3) {
                        textView = new TextView(mContext);

                        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT);
                        layoutParams.addRule(RelativeLayout.CENTER_VERTICAL);
                        layoutParams.addRule(RelativeLayout.RIGHT_OF, 2);
                        layoutParams.leftMargin = Util.getDp(2,mContext.getResources());
                        textView.setLayoutParams(layoutParams);

                        textView.setGravity(Gravity.CENTER_VERTICAL);
                        String text = String.format(mContext.getString(R.string.bulletin_reply_text), repliesCount - 3);
                        textView.setText(text);

                        holder.rlReplies.addView(textView);

                    }

                    View.OnClickListener listener = new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent i = new Intent(mContext, BulletinRepliesViewActivity.class);
                            i.putExtra(BulletinRepliesViewActivity.POST_ID, mData.get(position).getPostID());
                            mContext.startActivity(i);
                        }
                    };


                    // TODO - add click listener in view holder
                    if (null != textView)
                        textView.setOnClickListener(listener);
                    if (imageView != null)
                        imageView.setOnClickListener(listener);

                } catch (Exception e) {
                    Log.e(this.getClass().getSimpleName(), e.getLocalizedMessage(), e);
                }
                break;
            }
            case FIRST_ITEM: {
                BulletinRecyclerViewAdapter.ViewHolderFirst holder = (ViewHolderFirst) mHolder;
                holder.ivProfilePic.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent i = new Intent(mContext, ProfileActivity.class);
                        mContext.startActivity(i);
                    }
                });
                holder.ibAttachment.setOnTouchListener((HomeActivity) mContext);
                holder.ibLocation.setOnTouchListener((HomeActivity) mContext);
                break;
            }
        }
    }

    @Override
    public int getItemViewType(int position) {
        return position == 0 ? FIRST_ITEM : ITEM;
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView mMessage;
        public final TextView mName;
        public final TextView mDate;
        public final RelativeLayout rlReplies;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mMessage = (TextView) view.findViewById(R.id.tvMessage);
            mName = (TextView) view.findViewById(R.id.tvName);
            mDate = (TextView) view.findViewById(R.id.tvDate);
            rlReplies = (RelativeLayout) view.findViewById(R.id.rlReplies);
        }
    }

    public static class ViewHolderFirst extends RecyclerView.ViewHolder implements View.OnClickListener {
        public final View mView;
        public final ImageView ivProfilePic;
        public final TextView btnSendToSea;
        public final ImageButton ibLocation;
        public final ImageButton ibAttachment;
        public ViewHolderClickCaller mListener;

        public ViewHolderFirst(View view) {
            super(view);
            mView = view;
            ivProfilePic = (ImageView) view.findViewById(R.id.ivProfilePic);
            btnSendToSea = (TextView) view.findViewById(R.id.tvSendToSea);
            ibLocation = (ImageButton) view.findViewById(R.id.ibLocation);
            ibAttachment = (ImageButton) view.findViewById(R.id.ibAttachment);
        }

        @Override
        public void onClick(View v) {

        }

        public interface ViewHolderClickCaller {
            void onApprove(View caller, View rootView);
        }
    }
}