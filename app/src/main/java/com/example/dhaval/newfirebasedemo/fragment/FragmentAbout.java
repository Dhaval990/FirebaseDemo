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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.dhaval.newfirebasedemo.Constants;
import com.example.dhaval.newfirebasedemo.R;
import com.example.dhaval.newfirebasedemo.activity.CropActivity;
import com.example.dhaval.newfirebasedemo.helper.Util;
import com.example.dhaval.newfirebasedemo.model.CompanyDetail;
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

import static com.bumptech.glide.gifdecoder.GifHeaderParser.TAG;

/**
 * A simple {@link Fragment} subclass.
 */
public class FragmentAbout extends Fragment implements View.OnClickListener {


    public static final String mTag = FragmentAbout.class.getSimpleName();
    public Bitmap bitmap;
    private boolean isImageUpdate = false;
    private Context context;
    private EditText etAboutUs;
    private EditText etContactUs;
    private ImageView imgAd;
    private Button btnDone;
    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference myRef = database.getReference("Company");
    private StorageReference mStorageRef = FirebaseStorage.getInstance().getReference();
    private Uri path;
    private ProgressDialog mProgressDialog;
    private Uri mDownloadUrl;
    private String imgUrl = "";

    public FragmentAbout() {
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
        View view = inflater.inflate(R.layout.fragment_fragment_about, container, false);
        findViews(view);
        initData();
        return view;
    }

    private void initData() {
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                CompanyDetail companyDetail = dataSnapshot.getValue(CompanyDetail.class);
                if (companyDetail != null) {
                    etAboutUs.setText(companyDetail.getAboutUs());
                    etContactUs.setText(companyDetail.getContactUs());
                    imgUrl = companyDetail.getImg();
                    Glide.with(context.getApplicationContext()).load(companyDetail.getImg()).placeholder(ContextCompat.getDrawable(context, R.mipmap.ic_launcher)).error(ContextCompat.getDrawable(context, R.mipmap.ic_launcher)).into(imgAd);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    private void findViews(View view) {
        etAboutUs = (EditText) view.findViewById(R.id.et_aboutUs);
        etContactUs = (EditText) view.findViewById(R.id.et_ContactUs);
        imgAd = (ImageView) view.findViewById(R.id.img_ad);
        btnDone = (Button) view.findViewById(R.id.btn_Done);

        btnDone.setOnClickListener(this);
        imgAd.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v == btnDone) {
            /*if (!etAboutUs.getText().toString().trim().isEmpty() && !etContactUs.getText().toString().trim().isEmpty()) {
                CompanyDetail companyDetail = new CompanyDetail();
                companyDetail.setAboutUs(etAboutUs.getText().toString().trim());
                companyDetail.setContactUs(etContactUs.getText().toString().trim());
                if (isImageUpdate) {
                    companyDetail.setImg(mDownloadUrl.toString());
                }
                myRef.setValue(companyDetail);
            } else {
                Toast.makeText(context, "Please check input", Toast.LENGTH_SHORT).show();
            }*/


            if (isImageUpdate) {
                upLoadImage(path);
            } else {
                addDataToStorage();
            }
        } else if (v == imgAd) {
            onGallaryOptionClicked();
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
                path = Uri.parse("file:///" + data.getStringExtra("uri"));


                Glide.with(context.getApplicationContext()).load(path).placeholder(ContextCompat.getDrawable(context, R.mipmap.ic_launcher)).error(ContextCompat.getDrawable(context, R.mipmap.ic_launcher)).into(imgAd);
                //img_user.setImageBitmap(Util.getUriToBitmap(data.getStringExtra("uri")));
            }
        }
    }

    private void addDataToStorage() {

        if (!etAboutUs.getText().toString().trim().isEmpty() && !etContactUs.getText().toString().trim().isEmpty()) {
            showProgressDialog();

            CompanyDetail userData = new CompanyDetail();
            userData.setContactUs(etContactUs.getText().toString().trim());
            userData.setAboutUs(etAboutUs.getText().toString().trim());
            if (isImageUpdate) {
                userData.setImg(mDownloadUrl.toString());
            } else {
                userData.setImg(imgUrl);
            }

            myRef.setValue(userData).addOnSuccessListener(getActivity(), new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    hideProgressDialog();
                    Toast.makeText(context, "Successfully added", Toast.LENGTH_SHORT).show();
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
