package com.clipview.git.clipview;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

public class MainActivity extends AppCompatActivity {

    private GetImg getImg;
    private ImageView iv_photo;
    private RegisterReceiver receiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        IntentFilter filter = new IntentFilter("com.clipview.git.clipview.activity.acpreview.picture");
        receiver = new RegisterReceiver();
        registerReceiver(receiver, filter);

        getImg = new GetImg(this);

        iv_photo = (ImageView)findViewById(R.id.iv_photo);
         ((Button)findViewById(R.id.bt)).setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 takephoto_type = 1;
                 takePhoto();
             }
         });
         ((Button)findViewById(R.id.bt_system)).setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 takePhoto();
                 takephoto_type = 0;
             }
         });
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(receiver);
        super.onDestroy();
    }

    private void takePhoto() {
        new PWTakePhoto(this).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case PWTakePhoto.R_ID_BTN_0:
                        getImg.goToCamera();
                        break;
                    case PWTakePhoto.R_ID_BTN_1:
                        getImg.goToGallery();
                        break;
                    default:
                        break;
                }
            }
        }).showBottom(getWindow().getDecorView());
    }

    private int takephoto_type = 1;
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                // 如果是直接从相册获取
                case GetImg.PHOTO_IMG:
                    Uri uri = data.getData();
                    if(takephoto_type==0){
                        if (uri != null) {
                            getImg.gotoCutImage(uri);
                        }
                    }else if(takephoto_type==1) {
                        Intent intent = new Intent(this, ClipPictureActivity.class);
                        intent.setData(uri);
                        startActivity(intent);
                    }
                    break;
                // 如果是调用相机拍照时
                case GetImg.CAMERA_IMG:
                    Uri uri2 = Uri.fromFile(getImg.file_camera);
                    if(takephoto_type==0) {
                        if (uri2 != null) {
                            getImg.gotoCutImage(uri2);
                        }
                    }else if(takephoto_type==1) {
                        Intent intent2 = new Intent(this, ClipPictureActivity.class);
                        intent2.setData(uri2);
                        startActivity(intent2);
                    }
                    break;
                // 取得裁剪后的图片
                case GetImg.CUT_IMG:
                    if (data != null) {
                        Bitmap cut_bitmap = BitmapFactory.decodeFile(GetImg.file_cut.getAbsolutePath());
                        iv_photo.setImageBitmap(cut_bitmap);
                    }
                    break;
            }
        }
    }

    public class RegisterReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(final Context context, final Intent in) {
            if (in != null) {
                byte[] bis = in.getByteArrayExtra("bitmap");
                Bitmap bitmap = BitmapFactory.decodeByteArray(bis, 0, bis.length);
                iv_photo.setImageBitmap(bitmap);
            }
        }
    }

}
