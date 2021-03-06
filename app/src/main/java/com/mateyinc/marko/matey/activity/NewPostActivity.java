package com.mateyinc.marko.matey.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.mateyinc.marko.matey.R;
import com.mateyinc.marko.matey.activity.maps.MapsActivity;
import com.mateyinc.marko.matey.adapters.FilesAdapter;
import com.mateyinc.marko.matey.data.FilePath;
import com.mateyinc.marko.matey.internet.OperationManager;
import com.mateyinc.marko.matey.inall.MotherActivity;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import static com.mateyinc.marko.matey.inall.MyApplication.PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE;


public class NewPostActivity extends MotherActivity {

    private static final String TAG = NewPostActivity.class.getSimpleName();
    private static final int MIN_CHAR_LIMIT = 5;

    private EditText etNewPostMsg, etNewPostSubject;
    private TextView tvPost, tvNewPostHeading;
    private ImageButton ibBack;
    private ImageView ivAddPhoto, ivAddLocation, ivAddFile, ivSend;
    private Toolbar mToolbar;
    private RecyclerView rvFileList;

    private FilesAdapter mFilesAdapter;

    /**
     * Contains the id of the post that is being replied to.
     **/
    public static final String EXTRA_POST_ID = "replied_postid";
    /**
     * Contains the user name who posted the bulletin.
     **/
    public static final String EXTRA_USER_NAME = "bulletin_user_name";
    /**
     * Contains text that is being replied to, thus indicating that this isn't new post.
     **/
    public static final String EXTRA_REPLY_SUBJECT = "post_subject";

    /**
     * Contains marked position on the map (lat,long,title).
     **/
    public static final String EXTRA_MAP_POSITIONS = "map_position";


    private static final int IMAGE_CAPTURE_REQ_CODE = 1002;
    private static final int PICK_FILE_REQ_CODE = 1000;
    private static final int GALLERY_REQ_CODE = 1001;
    private static final int MAPS_REQ_CODE = 1003;

    private String selectedFilePath;
    private File mImageFile;
    private String mCurrentPhotoPath;

    /**
     * Indicates if this new post is reply on reply
     */
    private boolean mIsReplyOnBulletin = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_post);

        init();
        setUI();
    }

    private void init() {
        // Settings the app bar via custom toolbar
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        etNewPostMsg = (EditText) findViewById(R.id.etNewPostMsg);
        etNewPostSubject = (EditText) findViewById(R.id.etNewPostSubject);
        ivAddFile = (ImageView) findViewById(R.id.ivAddFile);
        ivAddLocation = (ImageView) findViewById(R.id.ivAddLocation);
        ivAddPhoto = (ImageView) findViewById(R.id.ivAddPhoto);
        ivSend = (ImageView) findViewById(R.id.ivSend);
        ibBack = (ImageButton) findViewById(R.id.ibBack);
        tvPost = (TextView) findViewById(R.id.tvPost);
        tvNewPostHeading = (TextView) findViewById(R.id.tvNewPostHeading);
        tvPost.setEnabled(false); // Can't post until something is typed in

        // Setup recycle view
        rvFileList = (RecyclerView) findViewById(R.id.rvFileList);
        mFilesAdapter = new FilesAdapter(NewPostActivity.this, rvFileList);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(NewPostActivity.this);
        linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        rvFileList.setLayoutManager(linearLayoutManager);
        rvFileList.setAdapter(mFilesAdapter);

        setClickListeners();
    }

    private void setUI() {
        Intent i = getIntent();

        // Setting ui if this is a reply on reply
        if (i.hasExtra(EXTRA_REPLY_SUBJECT)) {
            mIsReplyOnBulletin = true;
            String text = i.getStringExtra(EXTRA_REPLY_SUBJECT);
            etNewPostSubject.setText(text);
            etNewPostSubject.setFocusable(false);
            etNewPostMsg.setHint(null);
            etNewPostMsg.requestFocus();
            tvNewPostHeading.setText(String.format(Locale.US, getString(R.string.new_post_heading)
                    , getIntent().getExtras().getString(EXTRA_USER_NAME)));
        }
    }


    @SuppressWarnings({"ApiCall"})
    private void setClickListeners() {
        ivAddLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

//                boolean isMarshMallow = Build.VERSION.SDK_INT >= 23;
//                if (isMarshMallow && checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
//                        != PackageManager.PERMISSION_GRANTED) {
//
//                    // Should we show an explanation?
//                    if (shouldShowRequestPermissionRationale(
//                            Manifest.permission.ACCESS_FINE_LOCATION)) {
//                        // Explain to the user why we need to read the contacts
//                    }
//
//                    requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
//                            PERMISSIONS_REQUEST_ACCESS_LOCATION);
//
//                    return;
//                }
                Intent i = new Intent(NewPostActivity.this, MapsActivity.class);
                startActivityForResult(i, MAPS_REQ_CODE);
            }
        });

        ivAddFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isMarshMallow = Build.VERSION.SDK_INT >= 23;
                if (isMarshMallow && checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {

                    // Should we show an explanation?
                    if (shouldShowRequestPermissionRationale(
                            android.Manifest.permission.READ_EXTERNAL_STORAGE)) {
                        // Explain to the user why we need to read the contacts
                    }

                    requestPermissions(new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE},
                            PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);

                    return;
                }


                Intent intent = new Intent();
                //sets the select file to all types of files
                intent.setType("*/*");
                //allows to select data and return it
                intent.setAction(Intent.ACTION_GET_CONTENT);
                //starts new activity to select file and return data
                startActivityForResult(Intent.createChooser(intent, "Choose File to Upload.."), PICK_FILE_REQ_CODE);

            }
        });

        ivAddPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isMarshMallow = Build.VERSION.SDK_INT >= 23;
                if (isMarshMallow && checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {

                    // Should we show an explanation?
                    if (shouldShowRequestPermissionRationale(
                            android.Manifest.permission.READ_EXTERNAL_STORAGE)) {
                        // Explain to the user why we need to read the contacts
                    }

                    requestPermissions(new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE},
                            PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);

                    return;
                }

                AlertDialog.Builder builder = new AlertDialog.Builder(NewPostActivity.this);
                builder.setPositiveButton(R.string.chose_from_gal_label, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        choseImage();
                    }
                });
                builder.setNegativeButton(getString(R.string.take_a_pic_label), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        captureImage();
                    }
                });
                builder.setTitle(R.string.choosing_new_pic_title);
                builder.setMessage(R.string.choosing_new_pic_msg);
                builder.create().show();
            }

        });

        ibBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        etNewPostSubject.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                enableButton(s);
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                enableButton(s);
            }

            @Override
            public void afterTextChanged(Editable s) {
                enableButton(s);
            }

            private void enableButton(CharSequence s) {
                if (s == null || s.length() < MIN_CHAR_LIMIT) {
                    tvPost.setEnabled(false);
                    ivSend.setEnabled(false);
                } else {
                    ivSend.setEnabled(true);
                    tvPost.setEnabled(true);
                }
            }
        });

        ivSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                postNewPost();
            }
        });
        tvPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                postNewPost();
            }
        });
    }

    /**
     * Helper method for posting new bulletin.
     */
    private void postNewPost() {
        if (mIsReplyOnBulletin) {
            OperationManager operationManager = OperationManager.getInstance(NewPostActivity.this);
            operationManager.postNewReply(etNewPostMsg.getText().toString(),
                    getIntent().getExtras().getLong(EXTRA_POST_ID), NewPostActivity.this);
        } else {
            OperationManager operationManager = OperationManager.getInstance(NewPostActivity.this);
            operationManager.postNewBulletin(etNewPostSubject.getText().toString()
                    , etNewPostMsg.getText().toString(), mFilesAdapter.getData(), NewPostActivity.this);

        }
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK)
            if ((requestCode == PICK_FILE_REQ_CODE || requestCode == GALLERY_REQ_CODE
                    || requestCode == IMAGE_CAPTURE_REQ_CODE)) {

                if (data == null || data.getData() == null) {
                    //no data present
                    return;
                }

                Uri selectedFileUri = data.getData();
                selectedFilePath = FilePath.getPath(this, selectedFileUri);
                Log.i(TAG, "Selected File Path:" + selectedFilePath);

                if (selectedFilePath != null && !selectedFilePath.equals("")) {
                    mFilesAdapter.addData(selectedFilePath);
                } else {
                    Toast.makeText(this, R.string.file_not_supported, Toast.LENGTH_SHORT).show();
                }
            } else if (requestCode == MAPS_REQ_CODE) {
                try {
                    JSONArray array = new JSONArray(data.getStringExtra(EXTRA_MAP_POSITIONS));
                    for (int i = 0; i<array.length(); i++){
                        mFilesAdapter.addData(array.getJSONObject(i).toString());
                    }
                } catch (JSONException e) {
                    Log.e(TAG, e.getLocalizedMessage(), e);
                }
            }

    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    /**
     * Method for sending intent for user to chose an image file
     */
    private void choseImage() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");

        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(intent, GALLERY_REQ_CODE);
        } else {
            Toast.makeText(this, R.string.no_image_picker, Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Method for sending intent for user to capture new image
     */
    private void captureImage() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);

        if (intent.resolveActivity(getPackageManager()) != null) {
            try {
                mImageFile = createImageFile();
            } catch (IOException ex) {
                Log.e(TAG, ex.getLocalizedMessage(), ex);
            }

            // Continue only if the File was successfully created
            if (mImageFile != null) {
//                intent.putExtra(MediaStore.EXTRA_OUTPUT,
//                        Uri.fromFile(mImageFile));
                startActivityForResult(intent, IMAGE_CAPTURE_REQ_CODE);
            } else {
                Toast.makeText(this, R.string.error_capture_image, Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, R.string.no_cam_app, Toast.LENGTH_SHORT).show();
        }
    }

    private File createImageFile() throws IOException {
        // TODO - move picture path to internal
        File filepath = Environment.getExternalStorageDirectory();
        // Create a new folder in SD Card
        File dir = new File(filepath.getAbsolutePath() + "/Matey/");
        if (!dir.exists()) {
            dir.mkdir();
        }
        // Create a file for the image
        String mImageName = "profilePic_" + (new SimpleDateFormat("yyyyMMdd", Locale.US).format(new Date())) + ".jpg";
        File image = File.createTempFile(
                mImageName,  /* prefix */
                ".jpg",         /* suffix */
                dir      /* directory */
        );

        // Save a file path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();

        return image;
    }
}
