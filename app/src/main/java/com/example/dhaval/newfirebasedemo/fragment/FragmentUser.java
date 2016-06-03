package com.example.dhaval.newfirebasedemo.fragment;


import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.dhaval.newfirebasedemo.Constants;
import com.example.dhaval.newfirebasedemo.R;
import com.example.dhaval.newfirebasedemo.activity.CropActivity;
import com.example.dhaval.newfirebasedemo.helper.CircleTransform;
import com.example.dhaval.newfirebasedemo.helper.Util;
import com.example.dhaval.newfirebasedemo.model.UserData;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import static com.google.android.gms.internal.zzs.TAG;


/**
 * A simple {@link Fragment} subclass.
 */
public class FragmentUser extends Fragment implements View.OnClickListener {

    public static final String mTag = FragmentUser.class.getSimpleName();
    public Bitmap bitmap;
    boolean isImageUpdate = false;
    private ImageView img_user;
    private EditText et_firstName;
    private EditText et_lastName;
    private Context context;
    private Uri path;
    private ProgressDialog mProgressDialog;
    private Uri mDownloadUrl = null;
    private StorageReference mStorageRef = FirebaseStorage.getInstance().getReference();
    // Write a message to the database
    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference myRef = database.getReference("User");
    private String imgUrl = "";

    public FragmentUser() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_fragment_user, container, false);
        findViews(view);
        postInitView();
        return view;
    }

    private void postInitView() {
        img_user.setOnClickListener(this);
        getLatestDataFromTheStorage();
    }

    private void getLatestDataFromTheStorage() {
        showProgressDialog();
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                hideProgressDialog();
                UserData userData = dataSnapshot.getValue(UserData.class);
                fillData(userData);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                hideProgressDialog();
            }
        });
    }

    private void fillData(UserData userData) {
        if (userData != null) {
            Glide.with(context.getApplicationContext()).load(userData.getImg()).transform(new CircleTransform(context.getApplicationContext())).placeholder(ContextCompat.getDrawable(context, R.mipmap.ic_launcher)).error(ContextCompat.getDrawable(context, R.mipmap.ic_launcher)).into(img_user);
            imgUrl = userData.getImg();
            et_firstName.setText(userData.getFirstName());
            et_lastName.setText(userData.getLastName());

        } else {
            Toast.makeText(context, "Null Data", Toast.LENGTH_SHORT).show();
        }
    }

    private void findViews(View view) {
        img_user = (ImageView) view.findViewById(R.id.img_user);
        et_firstName = (EditText) view.findViewById(R.id.et_firstName);
        et_lastName = (EditText) view.findViewById(R.id.et_lastName);
        view.findViewById(R.id.btn_Submit).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.img_user:
                onGallaryOptionClicked();
                break;
            case R.id.btn_Submit:
                updateData();
                break;

        }
    }

    private void updateData() {
        if (isImageUpdate) {
            upLoadImage(path);
        } else {
            addDataToStorage();
        }
    }

    private void upLoadImage(Uri fileUri) {
// [START upload_from_uri]
        Log.d(TAG, "uploadFromUri:src:" + fileUri.toString());

        // [START get_child_ref]
        // Get a reference to store file at photos/<FILENAME>.jpg
        StorageReference photoRef = mStorageRef.child("photos").child(fileUri.getLastPathSegment());

        // [END get_child_ref]

        // Upload file to Firebase Storage
        // [START_EXCLUDE]
        showProgressDialog();
        // [END_EXCLUDE]
        Log.d(TAG, "uploadFromUri:dst:" + photoRef.getPath());
        photoRef.putFile(fileUri)
                .addOnSuccessListener(getActivity(), new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        // Upload succeeded
                        Log.d(TAG, "uploadFromUri:onSuccess");

                        // Get the public download URL
                        mDownloadUrl = taskSnapshot.getMetadata().getDownloadUrl();
                        addDataToStorage();
                        // [START_EXCLUDE]

                        // [END_EXCLUDE]
                    }
                })
                .addOnFailureListener(getActivity(), new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        // Upload failed
                        Log.w(TAG, "uploadFromUri:onFailure", exception);

                        mDownloadUrl = null;
                        isImageUpdate = false;
                        // [START_EXCLUDE]
                        hideProgressDialog();
                        Toast.makeText(context, "Error: upload failed", Toast.LENGTH_SHORT).show();

                        // [END_EXCLUDE]
                    }
                });

        // [END upload_from_uri]
    }

    private void addDataToStorage() {

        if (!et_firstName.getText().toString().trim().isEmpty() && !et_lastName.getText().toString().trim().isEmpty()) {
            showProgressDialog();

            UserData userData = new UserData();
            userData.setFirstName(et_firstName.getText().toString().trim());
            userData.setLastName(et_lastName.getText().toString().trim());
            if (isImageUpdate) {
                userData.setImg(mDownloadUrl.toString());
            } else {
                userData.setImg(imgUrl);

            }

            myRef.setValue(userData).addOnSuccessListener(getActivity(), new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    hideProgressDialog();
                    Toast.makeText(context, "Successfull added", Toast.LENGTH_SHORT).show();
                }
            }).addOnFailureListener(getActivity(), new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    hideProgressDialog();
                    Toast.makeText(context, "Fail", Toast.LENGTH_SHORT).show();
                }
            });


        } else {
            Toast.makeText(context, "Please Cheak Data", Toast.LENGTH_SHORT).show();
        }
    }

    private void showProgressDialog() {

        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(context);
            mProgressDialog.setMessage("Loading...");
            mProgressDialog.setIndeterminate(true);
        }

        mProgressDialog.show();
    }

    private void hideProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
    }

    private void onGallaryOptionClicked() {
        startActivityForResult(new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI), Constants.CAPTURE_GALLERY_IMAGE_ACTIVITY_REQUEST_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Constants.CAPTURE_GALLERY_IMAGE_ACTIVITY_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK && (data.getData() != null)) {
                Uri fileUri = data.getData();
                String PathGallery = Util.getRealPathFromURI(fileUri, getActivity());
                Log.i("", "Gallery Path: " + PathGallery);
                if (!TextUtils.isEmpty(PathGallery)) {
                    bitmap = Util.getBitmapDefault(PathGallery, 1000, 1000);
                    if (bitmap != null) {
                        Util.saveImage(PathGallery, bitmap);
                        navigateToCropActivity(PathGallery);
                    }
                }
            }
        } else if (requestCode == Constants.CROP_IMAGE) {
            if (data != null) {
                isImageUpdate = true;
                //file:///storage/emulated/0/a1681a38-cedf-4dcf-b12f-d4046cfca472.jpg
                path = Uri.parse("file://" + data.getStringExtra("uri"));


                Glide.with(context.getApplicationContext()).load(path).transform(new CircleTransform(context.getApplicationContext())).placeholder(ContextCompat.getDrawable(context, R.mipmap.ic_launcher)).error(ContextCompat.getDrawable(context, R.mipmap.ic_launcher)).into(img_user);
                //img_user.setImageBitmap(Util.getUriToBitmap(data.getStringExtra("uri")));
            }
        }
    }

    private void navigateToCropActivity(String path) {
        // create new Intent
        Intent intent = new Intent(getActivity(), CropActivity.class);
        // create a file to save image
        intent.putExtra("FilePath", path);
        intent.putExtra("PageName", mTag);

        // start the image Capture Intent
        startActivityForResult(intent, Constants.CROP_IMAGE);
    }
}
