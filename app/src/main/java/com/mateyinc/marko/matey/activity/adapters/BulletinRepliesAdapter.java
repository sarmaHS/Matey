package com.mateyinc.marko.matey.activity.adapters;

import android.content.Intent;
import android.content.res.Resources;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.mateyinc.marko.matey.R;
import com.mateyinc.marko.matey.activity.Util;
import com.mateyinc.marko.matey.activity.profile.ProfileActivity;
import com.mateyinc.marko.matey.activity.view.BulletinViewActivity;
import com.mateyinc.marko.matey.data.DataAccess;
import com.mateyinc.marko.matey.data.DataContract;
import com.mateyinc.marko.matey.data.OperationManager;
import com.mateyinc.marko.matey.inall.MotherActivity;
import com.mateyinc.marko.matey.model.Bulletin;
import com.mateyinc.marko.matey.model.Reply;
import com.mateyinc.marko.matey.model.UserProfile;

import java.util.LinkedList;

import static com.mateyinc.marko.matey.R.color.light_gray;


public class BulletinRepliesAdapter extends RecycleCursorAdapter {
    private final String TAG = BulletinRepliesAdapter.class.getSimpleName();

    public interface ReplyClickedInterface {
        void showPopupWindow(Reply reply);
        void showReplyKeyboard();
    }

    private RecyclerView mRecycleView;
    private UserProfile mCurUserProfile;
    private Resources mResources;
    private boolean mOnlyShowReplies = true;
    /** Indicates if there's no data to show, so only show main bulletin **/
    private boolean mNoData = false;
    private Bulletin mCurBulletin;

    private int ITEM = 1;
    private int FIRST_ITEM = 0;

    /** Used for showing popup windows for replying to reply **/
    private ReplyClickedInterface showPopupInterface = null;

    public BulletinRepliesAdapter(MotherActivity context, RecyclerView view, LinkedList data) {
        mContext = context;
        mRecycleView = view;
        mManager = OperationManager.getInstance(context);

        init();
    }

    public BulletinRepliesAdapter(MotherActivity context, RecyclerView view) {
        mContext = context;
        mRecycleView = view;
        mManager = OperationManager.getInstance(context);

        init();
    }

    private void init() {
        mCurUserProfile = new UserProfile();
        mResources = mContext.getResources();
    }

    @Override
    public int getItemViewType(int position) {
        if (mOnlyShowReplies)
            return ITEM;
        return position == 0 ? FIRST_ITEM : ITEM;
    }

    public boolean isDataAvailable(){
        return !mNoData;
    }

    @Override
    public int getItemCount() {
        // Tricking adapter to show only main bulletin info if there's no data
        int count = super.getItemCount();
        if (count == 0) {
            mNoData = true;
            return 1;
        } else
            mNoData = false;

        return count;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext)
                .inflate(R.layout.reply_replies_list_item, parent, false);

        if (viewType == FIRST_ITEM) {
            LinearLayout linearLayout = new LinearLayout(mContext);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            linearLayout.setOrientation(LinearLayout.VERTICAL);
            linearLayout.setLayoutParams(layoutParams);

            View topView = LayoutInflater.from(mContext)
                    .inflate(R.layout.bulletin_list_item, parent, false);

            FrameLayout divider = new FrameLayout(mContext);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (int) Util.parseDp(0.5f, mContext.getResources()));
            divider.setLayoutParams(params);
            divider.setBackgroundColor(mContext.getResources().getColor(R.color.light_gray));

            TextView textView = new TextView(mContext);
            textView.setTag("repliestext");
            params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            textView.setTextSize(mContext.getResources().getDimensionPixelSize(R.dimen.bulletin_textSize_message));
            textView.setText(R.string.bulletin_repliesView_repliesText);

            if (android.os.Build.VERSION.SDK_INT >= 21) {
                topView.setElevation(0f);
            }

            linearLayout.addView(topView);

            if (mNoData) {
                linearLayout.addView(divider);
                linearLayout.addView(view);
            }

            return new ViewHolder(linearLayout, getViewHolderListener(), showPopupInterface != null);
        } else {
            return new ViewHolder(view, getViewHolderListener(), showPopupInterface != null);
        }
    }


    private ViewHolder.ViewHolderClickListener getViewHolderListener() {
        return new ViewHolder.ViewHolderClickListener() {

            public void onRepliesClick(View caller, View rootView, boolean onlyShowReplies) {
//                int position = mRecycleView.getChildAdapterPosition(rootView);
//                if (onlyShowReplies) {
//                    Intent i = new Intent(mContext, BulletinViewActivity.class);
//                    i.putExtra(BulletinViewActivity.EXTRA_BULLETIN_ID, position);
//                    mContext.startActivity(i);
//                } else {
//                    Intent i = new Intent(mContext, BulletinViewActivity.class);
//                    i.putExtra(BulletinViewActivity.EXTRA_BULLETIN_ID, position);
//                    i.putExtra(BulletinViewActivity.EXTRA_NEW_REPLY, true);
//                    mContext.startActivity(i);
//                }
            }

            @Override
            public void onApproveClicked(View caller, View rootView) {
                int position = mRecycleView.getChildAdapterPosition(rootView); // Get child position in adapter
                mCursor.moveToPosition(position);
                Reply r = DataAccess.getReply(position, mCursor);

                if (r.hasReplyApproveWithId(mCurUserProfile.getUserId())) { // Unlike
                    // Remove approve from data and from database
                    for (UserProfile p : r.getReplyApproves()) {
                        if (p.getUserId() == mCurUserProfile.getUserId())
                            r.removeApprove(p);
                    }
//                    mManager.addReply(r, mCurBulletin, );

                    ((ImageView) caller).setColorFilter(mResources.getColor(light_gray)); // Changing the color of button
                    ((BulletinViewActivity) mContext).notifyBulletinFragment();

                } else { // Like
                    // Add approve  to database
                    r.addApprove(mCurUserProfile); // adding Reply to bulletin
//                    mManager.addReply(r);

                    ((BulletinViewActivity) mContext).notifyBulletinFragment();
                }
            }

            @Override
            public void onShowApprovesClicked(View caller, View rootView) {
                // TODO - finish method
            }

            @Override
            public void onNameClicked(View caller, View rootView) {
                int position = mRecycleView.getChildAdapterPosition(rootView);
                Intent i = new Intent(mContext, ProfileActivity.class);
                mCursor.moveToPosition(position);
                i.putExtra(ProfileActivity.EXTRA_PROFILE_ID, mCursor.getInt(BulletinViewActivity.COL_USER_ID));
                mContext.startActivity(i);
            }

            @Override
            public void onReplyClick(View caller, int adapterViewPosition) {
                mCursor.moveToPosition(adapterViewPosition);
                Reply r = DataAccess.getReply(adapterViewPosition, mCursor);
                showPopupInterface.showPopupWindow(r);
            }
        };
    }


    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder mHolder, final int position) {
        if (getItemViewType(position) == FIRST_ITEM) {
            BulletinRepliesAdapter.ViewHolder holder = (ViewHolder) mHolder;
            ((TextView) holder.mView.findViewById(R.id.tvBulletinUserName)).setText(mCurBulletin.getFirstName().concat(" ").concat(mCurBulletin.getLastName()));
            ((TextView) holder.mView.findViewById(R.id.tvBulletinDate)).setText(Util.getReadableDateText(mCurBulletin.getDate()));
            ((TextView) holder.mView.findViewById(R.id.tvBulletinSubject)).setText(mCurBulletin.getSubject());
            ((TextView) holder.mView.findViewById(R.id.tvBulletinMessage)).setText(mCurBulletin.getMessage());
            ((TextView) holder.mView.findViewById(R.id.tvBulletinStats)).setText(mCurBulletin.getStatistics(mContext));
            LinearLayout llReply = (LinearLayout) holder.mView.findViewById(R.id.llBulletinReply);
            LinearLayout llBoost = (LinearLayout) holder.mView.findViewById(R.id.llBoost);

            llReply.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showPopupInterface.showReplyKeyboard();
                }
            });

            llBoost.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mManager.newPostLike(mCurBulletin, mContext);
                    mContext.getContentResolver().notifyChange(DataContract.ReplyEntry.CONTENT_URI, null);
                }
            });

            holder.mView.findViewById(R.id.ivBulletinProfilePic).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i = new Intent(mContext, ProfileActivity.class);
                    i.putExtra(ProfileActivity.EXTRA_PROFILE_ID, mCurBulletin.getUserID());
                    mContext.startActivity(i);
                }
            });

            // If there's no data, show only main bulletin info
            if (mNoData)
                return;
        }
        final Reply reply = DataAccess.getReply(position, mCursor);
        BulletinRepliesAdapter.ViewHolder holder = (ViewHolder) mHolder;

        String text = reply.getUserFirstName() + " " + reply.getUserLastName();
        holder.tvName.setText(text);
        holder.tvDate.setText(Util.getReadableDateText(reply.getReplyDate()));
        holder.tvMessage.setText(reply.getReplyText());
        holder.tvStats.setText(reply.getStatistics(mContext));
    }

    /**
     * Tells adapter to show main post of replies as first post.
     * For viewing bulletin this method should be called to show bulletin first,
     * but when viewing replies of reply this method should not be called.
     * @param b {@link Bulletin} bulletin to show.
     */
    public void showMainPostInfo(Bulletin b) {
        mCurBulletin = b;
        mOnlyShowReplies = false;
    }

    public void setBulletin(Bulletin b) {
        mCurBulletin = b;
    }

    /**
     * Setting communication interface so popup window and keyboard can show up when clicked in reply button.
     * @param replyClickedInterface {@link ReplyClickedInterface} interface to setup in adapter
     */
    public void setReplyPopupInterface(ReplyClickedInterface replyClickedInterface) {
        showPopupInterface = replyClickedInterface;
    }


    protected static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private static final String TAG_NAME = "name";
        private static final String TAG_APPROVE = "approvebtn";
        private static final String TAG_SHOW_APPROVES = "showapprvs";


        private static final String TV_MESSAGE = "tvMessage";
        private static final String BTN_REPLY_TAG = "replytag";
        private static final String BTN_ARR_TAG = "arrtag";

        final LinearLayout mView;
        final TextView tvMessage;
        final TextView tvName, tvDate, tvStats;
        final ImageView ivProfilePic;
        final LinearLayout btnReply, btnArr;

        private final ViewHolderClickListener mListener;

        public ViewHolder(View view, ViewHolderClickListener listener, boolean showReplyButton) {
            super(view);
            mView = (LinearLayout)view;
            btnArr = (LinearLayout) view.findViewById(R.id.llArr);
            btnReply = (LinearLayout) view.findViewById(R.id.llReReply);
            tvStats = (TextView) view.findViewById(R.id.tvReplyStats);
            if (!showReplyButton) {
                ((RelativeLayout)mView.findViewById(R.id.rlReplyInfo)).removeView(btnReply);
                RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) tvStats.getLayoutParams();
                params.addRule(RelativeLayout.RIGHT_OF, R.id.llArr);
                params.addRule(RelativeLayout.END_OF, R.id.llArr);
                tvStats.setLayoutParams(params);
            } else {
                btnReply.setTag(BTN_REPLY_TAG);
            }

            tvMessage = (TextView) view.findViewById(R.id.tvReplyMessage);
            tvMessage.setTag(TV_MESSAGE);
            tvName = (TextView) view.findViewById(R.id.tvReplyName);
            tvDate = (TextView) view.findViewById(R.id.tvReplyTime);
            ivProfilePic = (ImageView) view.findViewById(R.id.ivReplyProfilePic);
            mListener = listener;

            tvName.setTag(TAG_NAME);
            ivProfilePic.setTag(TAG_NAME);

            tvName.setOnClickListener(this);
            ivProfilePic.setOnClickListener(this);
            tvMessage.setOnClickListener(this);
            if (showReplyButton)
                btnReply.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {

            String tag = v.getTag().toString();
            if (tag.equals(TAG_NAME)) {
                mListener.onNameClicked(v, mView);
            } else if (tag.equals(TAG_APPROVE)) {
                mListener.onApproveClicked(v, mView);
            } else if (tag.equals(TAG_SHOW_APPROVES)) {
                mListener.onShowApprovesClicked(v, mView);
            } else if (tag.equals(BTN_REPLY_TAG)){
                mListener.onReplyClick(v, getAdapterPosition());
            } else if (tag.equals(TV_MESSAGE)) {
                mListener.onReplyClick(v, getAdapterPosition());
            }
        }

        protected interface ViewHolderClickListener {
            void onApproveClicked(View caller, View rootView);

            void onShowApprovesClicked(View caller, View rootView);

            void onNameClicked(View caller, View rootView);

            void onReplyClick(View caller, int adapterViewPosition);
        }
    }
}
