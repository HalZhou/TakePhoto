package com.robooot.myapplication;

import android.Manifest;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.support.annotation.Nullable;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.ImageView;
import com.robooot.myapplication.view.CustomDialog;
import com.robooot.myapplication.photo.OnTakePhotoListener;
import com.robooot.myapplication.photo.TakePhoteManager;

public class MainActivity extends AppCompatActivity implements OnTakePhotoListener {

    private String[] permissions = new String[]{
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.READ_PHONE_STATE
    };
    private TakePhoteManager takePhoteManager;
    private ImageView ivProfile;

    private int width;
    private int height;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        takePhoteManager = new TakePhoteManager(this, this);
        ivProfile = findViewById(R.id.ivProfile);

        DisplayMetrics dm = getResources().getDisplayMetrics();
        width = dm.widthPixels;
        height = dm.heightPixels;
    }

    public void uploadAvatar(View view) {
        new CustomDialog(this)
                .setTitles(new String[]{"拍照", "从手机相册选择"})
                .setColors(new int[]{R.color.colorAccent,R.color.colorPrimary,R.color.colorPrimaryDark})
                .setOnItemClickListener(new CustomDialog.OnItemClickListener() {
                    @Override
                    public void onItemClick(int id) {
                        switch (id) {
                            case CustomDialog.TAKE_PHOTO:
                                //拍照
                                takePhoteManager.setTakePhotoValue(width,height,1);
                                takePhoteManager.requestTakePhoto();
                                break;
                            case CustomDialog.PICK_PHOTO:
                                //本地图片
                                takePhoteManager.setTakePhotoValue(width,height,0);
                                takePhoteManager.requestPickPhoto();
                                break;
                        }
                    }
                })
                .show();
    }

    @Override
    public void onTakePath(String path) {
        System.out.println(path);

        ivProfile.setImageBitmap(BitmapFactory.decodeFile(path));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        takePhoteManager.onActivityResult(this, requestCode, resultCode, data);
    }
}
