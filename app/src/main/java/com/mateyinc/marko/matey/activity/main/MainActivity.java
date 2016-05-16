package com.mateyinc.marko.matey.activity.main;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.util.Patterns;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.BounceInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.OvershootInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.Profile;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.mateyinc.marko.matey.R;
import com.mateyinc.marko.matey.gcm.MateyGCMPreferences;
import com.mateyinc.marko.matey.inall.MotherActivity;
import com.mateyinc.marko.matey.internet.procedures.FacebookLoginAs;
import com.mateyinc.marko.matey.internet.procedures.LoginAs;
import com.mateyinc.marko.matey.internet.procedures.RegisterAs;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

@SuppressLint("NewApi")
public class MainActivity extends MotherActivity {

    private static final int SHORT_ANIM_TIME = 150;
    private static final int MED_ANIM_TIME = 500;
    private static final int INTERMED_ANIM_TIME = 700;
    private static final int LONG_ANIM_TIME = 1000;
    private static final long SERVER_TIMER = 500;
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST  = 1000;
    private static final String TAG = "com.matey.android";

    private ImageView ivLogoText, ivLoadingHead, ivLogoBubbleText, ivLogoClouds;
    private Button btnLogin, btnReg, btnFb;
    private RelativeLayout rlLoginButtons, rlMain;
    private LinearLayout llEmail, llPass, line1, line2;
    public EditText etPass;
    public AutoCompleteTextView etEmail;

    private float mLoginBtnMoveY;
    private float mRegBtnMoveY;
    private int mLoginBtnBotMargin;
    private boolean mLoginFormVisible;
    public boolean mRegFormVisible;
    private int mRegBtnBotMargin;
    private float mLogoMoveOnRegBtn;
    private float mLoadingHeadIntroTransl;

    private Resources resources;
    CallbackManager fbCallbackManager;

    // GCM
    private boolean isReceiverRegistered;
    private BroadcastReceiver mRegistrationBroadcastReceiver;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        FacebookSdk.sdkInitialize(getApplicationContext());
        fbCallbackManager = CallbackManager.Factory.create();
        facebookLogin();

        //TelephonyManager tMgr = (TelephonyManager)this.getSystemService(Context.TELEPHONY_SERVICE);
        //Log.d("pnum", tMgr.getLine1Number());

        getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
        if (getSupportActionBar() != null)
            getSupportActionBar().hide();
        setContentView(R.layout.activity_main);
        super.setSecurePreferences(this);

        init();
        // sceptic tommy starts checking everything
		super.startTommy();

        // Registering BroadcastReceiver
//        registerReceiver();

    }

    /**
     * Check the device to make sure it has the Google Play Services APK. If
     * it doesn't, display a dialog that allows users to download the APK from
     * the Google Play Store or enable it in the device's system settings.
     */
    public boolean checkPlayServices() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode)) {
                apiAvailability.getErrorDialog(this, resultCode, PLAY_SERVICES_RESOLUTION_REQUEST)
                        .show();
            } else {
                Log.i(TAG, "This device is not supported.");
                finish();
            }
            return false;
        }
        return true;
    }

    private void registerReceiver(){
        if(!isReceiverRegistered) {
            LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver,
                    new IntentFilter(MateyGCMPreferences.REGISTRATION_COMPLETE));
            isReceiverRegistered = true;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Registering BroadcastReceiver
//        registerReceiver();
    }



    @Override
    protected void onDestroy() {
//        LocalBroadcastManager.getInstance(this).unregisterReceiver(mRegistrationBroadcastReceiver);
//        isReceiverRegistered = false;
        super.onDestroy();

        if (tommy.getStatus() != AsyncTask.Status.FINISHED) {
            tommy.cancel(true);
        }

    }

    private void init() {
        btnLogin = (ButtonLoginPage) findViewById(R.id.btnLogin);
        btnReg = (ButtonLoginPage) findViewById(R.id.btnReg);
        btnFb = (Button) findViewById(R.id.btnFacebook);
        ivLogoText = (ImageView) findViewById(R.id.ivLoginLogoText);
        llEmail = (LinearLayout) findViewById(R.id.llEmail);
        llPass = (LinearLayout) findViewById(R.id.llPass);
        line1 = (LinearLayout) findViewById(R.id.llLine1);
        line2 = (LinearLayout) findViewById(R.id.llLine2);
        rlLoginButtons = (RelativeLayout) findViewById(R.id.rlLoginButtons);
        ivLoadingHead = (ImageView) findViewById(R.id.ivLoadingHead);
        etEmail = (AutoCompleteTextView) findViewById(R.id.etEmail);
        etPass = (EditText) findViewById(R.id.etPass);
        ivLogoBubbleText = (ImageView) findViewById(R.id.ivLoginLogoBubble);
        rlMain = (RelativeLayout) findViewById(R.id.rlMain);
        ivLogoClouds = (ImageView) findViewById(R.id.ivLoginLogoClouds);
        resources = getResources();

//        mRegistrationBroadcastReceiver = new BroadcastReceiver() {
//            @Override
//            public void onReceive(Context context, Intent intent) {
//                SharedPreferences sharedPreferences =
//                        PreferenceManager.getDefaultSharedPreferences(context);
//                boolean sentToken = sharedPreferences
//                        .getBoolean(MateyGCMPreferences.SENT_TOKEN_TO_SERVER, false);
//                if (sentToken) {
//                } else {
//                }
//            }
//        };

        startIntro();
        setOnClickListeners();
        setAutocompleteEmailValues();

    }

    private void setOnClickListeners() {
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mLoginFormVisible)
                    showLoginForm();
                else {

                    String email = etEmail.getText().toString();
                    String pass = etPass.getText().toString();

                    Bundle bundle = new Bundle();

                    if(!isValidEmailAddress(email)) {
                        bundle.putString("message", "Wrong email format. fon");
                        showDialog(1004, bundle);
                    } else if(!isValidPassword(pass)) {
                        bundle.putString("message", "Password needs to be at least 5 characters long.");
                        showDialog(1004, bundle);
                    } else {
                        // starting login process
                        MainActivity activity = (MainActivity) v.getContext();
                        LoginAs loginAs = new LoginAs(activity);
                        loginAs.execute(email, pass, activity.securePreferences.getString("device_id"));
                    }

                }
            }
        });

        btnReg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mRegFormVisible)
                    showRegForm();
                else {

                    String email = etEmail.getText().toString();
                    String pass = etPass.getText().toString();

                    Bundle bundle = new Bundle();

                    if(!isValidEmailAddress(email)) {
                        bundle.putString("message", "Wrong email format. fon");
                        showDialog(1004, bundle);
                    } else if(!isValidPassword(pass)) {
                        bundle.putString("message", "Password needs to be at least 5 characters long.");
                        showDialog(1004, bundle);
                    } else {
                        // start asynchronous task to handle user registration
                        RegisterAs registerAs = new RegisterAs((MainActivity) v.getContext());
                        registerAs.execute(email, pass, "no", "");
                    }

                }

            }

        });

        btnFb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                fbAnswerType = 0;
                LoginManager.getInstance().logInWithReadPermissions(MainActivity.this, Arrays.asList("public_profile", "user_friends", "email"));

            }
        });

    }

    private void setAutocompleteEmailValues() {
        Pattern emailPattern = Patterns.EMAIL_ADDRESS;
        Account[] accounts = AccountManager.get(this).getAccounts();

        Set<String> suggestedEmails = new HashSet<String>();

        for (Account account : accounts) {
            if (emailPattern.matcher(account.name).matches()) {
                suggestedEmails.add(account.name);
            }
        }

        etEmail.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line, new ArrayList<String>(suggestedEmails)));
    }

    public boolean isValidEmailAddress(String email) {
        String ePattern = "^[a-zA-Z0-9.!#$%&'*+/=?^_`{|}~-]+@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\])|(([a-zA-Z\\-0-9]+\\.)+[a-zA-Z]{2,}))$";
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(ePattern);
        java.util.regex.Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }

    public boolean isValidPassword(String password) {
        if(password.length() < 5) return false;
        return true;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        fbCallbackManager.onActivityResult(requestCode, resultCode, data);
    }



    // when facebook login is done
    // this method will get on work
    protected void facebookLogin() {

        LoginManager.getInstance().registerCallback(fbCallbackManager,
                new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(final LoginResult loginResult) {

                        final AccessToken accessToken = loginResult.getAccessToken();
                        final Profile profile = Profile.getCurrentProfile();

                        // OVDE RADNJE NAKON LOGIN-A
                        if (fbAnswerType == 0) {

                            if (profile.getId() != null) {

                                FacebookLoginAs fbLogin = new FacebookLoginAs(MainActivity.this);
                                fbLogin.execute(accessToken.getToken(), profile.getId(), securePreferences.getString("device_id"));

                            }

                        } else {

                            String email = etEmail.getText().toString();
                            String pass = etPass.getText().toString();

                            // start asynchronous task to handle user registration
                            RegisterAs registerAs = new RegisterAs(MainActivity.this);
                            registerAs.execute(email, pass, "yes", accessToken.getToken());

                        }

                    }

                    @Override
                    public void onCancel() {

                    }

                    @Override
                    public void onError(FacebookException exception) {

                    }
                });

    }

    private void showLoginForm() {
        startFadeAnimation(btnReg, 1, 0, SHORT_ANIM_TIME, false);
        startFadeAnimation(btnFb, 1, 0, SHORT_ANIM_TIME, false);
        startFadeAnimation(ivLogoText, 1, 0, SHORT_ANIM_TIME, false);
        startFadeAnimation(ivLogoBubbleText, 1, 0, SHORT_ANIM_TIME, false);

        showLoginFormAnim();
    }

    private void showRegForm() {
        startFadeAnimation(btnLogin, 1, 0, SHORT_ANIM_TIME);
        startFadeAnimation(btnFb, 1, 0, SHORT_ANIM_TIME);
        startFadeAnimation(ivLogoText, 1, 0, SHORT_ANIM_TIME);
        startFadeAnimation(ivLogoBubbleText, 1, 0, SHORT_ANIM_TIME);
        showRegFormAnim();
    }

    // Animations
    //////////////////////////////////////////////////////////////////////////
    private void startIntro() {

        // Get view's h and w after its layout process then start anim
        final ViewTreeObserver viewTreeObserver = ivLoadingHead.getViewTreeObserver();
        if (viewTreeObserver.isAlive()) {
            viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    ivLoadingHead.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    startIntroAnim(ivLoadingHead, 2, 1, 2, 1, 500);
                }
            });
        }


    }

    // Buttons anim
    private void showLoginFormAnim() {
        TranslateAnimation animation = new TranslateAnimation(0, 0, 0, mLoginBtnMoveY = calculateLoginBtnTransl());
        animation.setDuration(500);
        animation.setFillAfter(false);
        animation.setAnimationListener(new LoginBtnDownAnimListener());
        mLoginBtnBotMargin = ((RelativeLayout.LayoutParams) btnLogin.getLayoutParams()).bottomMargin;
        btnLogin.startAnimation(animation);

        // For anim control, only animate once on button click if form is visible
        mLoginFormVisible = true;
    }

    private void startLoginReverseAnim() {
        startFadeAnimation(llPass, 1, 0, SHORT_ANIM_TIME);
        startFadeAnimation(llEmail, 1, 0, SHORT_ANIM_TIME);
        startFadeAnimation(line2, 1, 0, SHORT_ANIM_TIME);
        startFadeAnimation(line1, 1, 0, SHORT_ANIM_TIME);

        // Return buttons to theirs original states
        TranslateAnimation animation = new TranslateAnimation(0, 0, 0, -mLoginBtnMoveY);
        animation.setDuration(500);
        animation.setFillAfter(false);
        animation.setAnimationListener(new LoginBtnUpAnimListener());

        btnLogin.startAnimation(animation);
    }

    private void showRegFormAnim() {
        btnReg.animate().setDuration(500).setListener(null)
                .yBy(mRegBtnMoveY = calculateRegBtnTransl());


        ivLoadingHead.animate().setInterpolator(new DecelerateInterpolator()).scaleX(0.75f).scaleY(0.75f).
                yBy(-resources.getDimension(R.dimen.anim_logo_transl_xy))
                .xBy(-resources.getDimension(R.dimen.anim_logo_transl_xy)).
                setDuration(MED_ANIM_TIME)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        startFadeAnimation(llPass, 0, 1, SHORT_ANIM_TIME);
                        startFadeAnimation(llEmail, 0, 1, SHORT_ANIM_TIME);
                        startFadeAnimation(line1, 0, 1, SHORT_ANIM_TIME);
                        startFadeAnimation(line2, 0, 1, SHORT_ANIM_TIME);
                    }
                });

        // For anim control, only animate once on button click if form is visible
        mRegFormVisible = true;
    }


    public void startRegReverseAnim() {
        startFadeAnimation(llPass, 1, 0, SHORT_ANIM_TIME);
        startFadeAnimation(llEmail, 1, 0, SHORT_ANIM_TIME);
        startFadeAnimation(line2, 1, 0, SHORT_ANIM_TIME);
        startFadeAnimation(line1, 1, 0, SHORT_ANIM_TIME);

        // Return buttons to theirs original states
        btnReg.animate().setDuration(500)
                .yBy(-mRegBtnMoveY)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        btnFb.setVisibility(View.VISIBLE);
                        btnLogin.setVisibility(View.VISIBLE);

                        startFadeAnimation(btnFb, 0, 1, SHORT_ANIM_TIME);
                        startFadeAnimation(btnLogin, 0, 1, SHORT_ANIM_TIME);
                        startFadeAnimation(ivLogoText, 0, 1, SHORT_ANIM_TIME);
                        startFadeAnimation(ivLogoBubbleText, 0, 1, SHORT_ANIM_TIME);

                    }
                });

        ivLoadingHead.animate().scaleX(1.25f).scaleY(1.25f)
                .yBy(resources.getDimension(R.dimen.anim_logo_transl_xy))
                .xBy(resources.getDimension(R.dimen.anim_logo_transl_xy))
                .setDuration(MED_ANIM_TIME)
                .setListener(null);

    }

    private float calculateLoginBtnTransl() {
        return btnFb.getTop() - btnLogin.getTop();
    }

    private float calculateRegBtnTransl() {
        return btnFb.getTop() - btnReg.getTop();
    }

    private float calculateLogoTransl() {
        return getResources().getDimension(R.dimen.anim_logo_transl_xy);
    }

    // Fade anim
    private void startFadeAnimation(final View view, final int from, final int to, int time) {
        if (from < to) {
            view.setVisibility(View.VISIBLE);
            view.setAlpha(0f);
        }
        view.animate().setDuration(time).setInterpolator(new LinearInterpolator())
                .alpha(to).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                if (from > to)
                    view.setVisibility(View.GONE);
                else
                    view.setVisibility(View.VISIBLE);
            }
        });
    }

    private void startFadeAnimationWithDelay(final View view, final int from, final int to, int time) {
        if (from < to) {
            view.setVisibility(View.VISIBLE);
            view.setAlpha(0f);
        }
        view.animate().setDuration(time).setInterpolator(new AccelerateInterpolator(1f))
                .alpha(to).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                if (from > to)
                    view.setVisibility(View.GONE);
                else
                    view.setVisibility(View.VISIBLE);
            }
        });
    }

    private void startFadeAnimation(final View view, final int from, final int to, int time, final boolean hide) {
        if (from < to) {
            view.setVisibility(View.VISIBLE);
            view.setAlpha(0f);
        }
        view.animate().setDuration(time).setInterpolator(new LinearInterpolator())
                .alpha(to).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                if (hide)
                    view.setVisibility(View.GONE);
                else if (from > to)
                    view.setVisibility(View.INVISIBLE);
                else
                    view.setVisibility(View.VISIBLE);
            }
        });
    }

    // Loading anim
    private void startIntroAnim(final View view, int fromX, int toX, int fromY, int toY, int time) {
        view.setVisibility(View.VISIBLE);
        view.setAlpha(0);
        view.setScaleX(2f);
        view.setScaleY(2f);
//        AnimatorSet set = new AnimatorSet();
//        set
//                .play(ObjectAnimator.ofFloat(view, View.ALPHA, 0, 1f))
//                .with(ObjectAnimator.ofFloat(view, View.SCALE_X, 2f, 1f))
//                .with(ObjectAnimator.ofFloat(view, View.SCALE_Y, 2f, 1f));
//        set.setInterpolator(new AccelerateInterpolator(2.5f));
//        set.setDuration(time);
//        set.start();

        view.animate().setDuration(time).setInterpolator(new AccelerateInterpolator(2.5f)).setListener(null)
                .alpha(1f).scaleX(1f).scaleY(1f)
                .withEndAction(new Runnable() {
            @Override
            public void run() {
                mLoadingHeadIntroTransl = ivLoadingHead.getTop();
                goUpAnim(500);
            }
        });

//        CountDownTimer timer = new CountDownTimer(time * 2, time) {
//            @Override
//            public void onTick(long millisUntilFinished) {
//            }
//
//            @Override
//            public void onFinish() {
//                mLoadingHeadIntroTransl = ivLoadingHead.getTop();
//                goUpAnim(500);
//            }
//        };
//        timer.start();

    }

    private void goUpAnim(final int time) {
        ivLoadingHead.animate().setDuration(time).setInterpolator(new LinearInterpolator()).setStartDelay(100)
                .y(mLoadingHeadIntroTransl - 30)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        ivLoadingHead.clearAnimation();
                        if (!mServerReady) {
                            goDownAnim(700);
                        } else
                            endLoadingAnim();                    }
                });
    }

    private void goDownAnim(final int time) {
        ivLoadingHead.animate().setDuration(time).setInterpolator(new BounceInterpolator()).setStartDelay(200)
                .y(mLoadingHeadIntroTransl +60)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        ivLoadingHead.clearAnimation();
                        if (!mServerReady) {
                            goUpAnim(700);
                        } else
                            endLoadingAnim();                    }
                });
    }

    private void endLoadingAnim() {
        // Return pic to center and  move layout
        // TODO - rework other anims via ViewPropertyAnimatior because it does not interfere with system transitions
        ivLoadingHead.animate().y(0)
                .setInterpolator(new OvershootInterpolator(2.2f))
                .setDuration(800)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {

                        startFadeAnimation(ivLogoClouds, 0, 1, MED_ANIM_TIME);
                        startFadeAnimation(ivLogoBubbleText, 0, 1, MED_ANIM_TIME);
                        startFadeAnimation(ivLogoText, 0, 1, MED_ANIM_TIME);

                        new CountDownTimer(900, 900) {
                            @Override
                            public void onTick(long millisUntilFinished) {

                            }

                            @Override
                            public void onFinish() {
                                startFadeAnimation(rlLoginButtons, 0, 1, SHORT_ANIM_TIME);
                            }
                        }.start();

                    }
                });
    }
    //////////////////////////////////////////////////////////////////////////


    // Animation listeners
    //////////////////////////////////////////////////////////////////////////
    private class LoginBtnDownAnimListener implements Animation.AnimationListener {

        @Override
        public void onAnimationEnd(Animation animation) {
            btnLogin.clearAnimation();

            // Set button position after anim
            RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(btnLogin.getWidth(), btnLogin.getHeight());
            layoutParams.setMargins(0, 0, 0, rlLoginButtons.getHeight() - btnFb.getBottom());
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
            layoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL);

            btnLogin.setLayoutParams(layoutParams);

            startFadeAnimation(llPass, 0, 1, SHORT_ANIM_TIME);
            startFadeAnimation(llEmail, 0, 1, SHORT_ANIM_TIME);
            startFadeAnimation(line1, 0, 1, SHORT_ANIM_TIME);
            startFadeAnimation(line2, 0, 1, SHORT_ANIM_TIME);
        }

        @Override
        public void onAnimationRepeat(Animation animation) {
        }

        @Override
        public void onAnimationStart(Animation animation) {

        }

    }

    private class LoginBtnUpAnimListener implements Animation.AnimationListener {

        @Override
        public void onAnimationEnd(Animation animation) {
            btnLogin.clearAnimation();
            btnFb.setVisibility(View.INVISIBLE);
            btnReg.setVisibility(View.INVISIBLE);

            // Set button position back to original layout position
            RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(btnLogin.getWidth(), btnLogin.getHeight());
            layoutParams.setMargins(0, 0, 0, mLoginBtnBotMargin);
            layoutParams.addRule(RelativeLayout.ALIGN_START, R.id.btnReg);
            layoutParams.addRule(RelativeLayout.ALIGN_LEFT, R.id.btnReg);
            layoutParams.addRule(RelativeLayout.ABOVE, R.id.btnReg);
            layoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL);

            btnLogin.setLayoutParams(layoutParams);

            startFadeAnimation(btnFb, 0, 1, SHORT_ANIM_TIME);
            startFadeAnimation(btnReg, 0, 1, SHORT_ANIM_TIME);
            startFadeAnimation(ivLogoText, 0, 1, SHORT_ANIM_TIME);
            startFadeAnimation(ivLogoBubbleText, 0, 1, SHORT_ANIM_TIME);
        }

        @Override
        public void onAnimationRepeat(Animation animation) {
        }

        @Override
        public void onAnimationStart(Animation animation) {
        }

    }
    //////////////////////////////////////////////////////////////////////////


    @Override
    protected void onPause() {
        super.onPause();
    }

    // Controlling physical back button
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (llEmail.getVisibility() == View.VISIBLE && btnLogin.getVisibility() == View.VISIBLE) {
                startLoginReverseAnim();
                mLoginFormVisible = false;
                return true;
            } else if (llEmail.getVisibility() == View.VISIBLE && btnReg.getVisibility() == View.VISIBLE) {
                startRegReverseAnim();
                mRegFormVisible = false;
                return true;
            } else
                return super.onKeyDown(keyCode, event);

        }
        return super.onKeyDown(keyCode, event);
    }

}
