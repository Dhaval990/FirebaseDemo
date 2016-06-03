package com.example.dhaval.newfirebasedemo.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.edmodo.cropper.CropImageView;
import com.example.dhaval.newfirebasedemo.Constants;
import com.example.dhaval.newfirebasedemo.R;
import com.example.dhaval.newfirebasedemo.fragment.FragmentUser;
import com.example.dhaval.newfirebasedemo.helper.Util;

import static com.example.dhaval.newfirebasedemo.R.id.btn_Done;
import static com.example.dhaval.newfirebasedemo.helper.Util.saveImage;

public class CropActivity extends AppCompatActivity {

    private CropImageView cropImageView;
    private String fileUrl;
    private String pageName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crop);

        cropImageView = (CropImageView) findViewById(R.id.CropImageView);


        findViewById(btn_Done).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String uri = saveImage(fileUrl, cropImageView.getCroppedImage());
                Intent i = new Intent();
                i.putExtra("uri", uri);
                setResult(Constants.CROP_IMAGE, i);
                finish();
            }
        });


        if (getIntent().getExtras() != null) {
            fileUrl = getIntent().getStringExtra("FilePath");
            cropImageView.setImageBitmap(Util.getUriToBitmap(fileUrl));
            pageName = getIntent().getStringExtra("PageName");
        }

        cropImageView.setGuidelines(1);
        cropImageView.setFixedAspectRatio(true);
        if (pageName.equals(FragmentUser.mTag)) {
            cropImageView.setAspectRatio(1, 1);
        } else {
            cropImageView.setAspectRatio(2, 1);
        }


    }

}
