package com.example.photocut;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.Manifest;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
import java.io.File;


public class MainActivity extends AppCompatActivity {

    private Button choiceFromAlbumButton = null;
    private ImageView pictureImageView = null;
    private static final int WRITE_SDCARD_PERMISSION_REQUEST_CODE = 1; // 讀儲存卡內容的權限處理返回代碼
    private static final int CHOICE_FROM_ALBUM_REQUEST_CODE = 4; // 相簿選取返回的 requestCode
    private static final int CROP_PHOTO_REQUEST_CODE = 5; // 裁剪圖片返回的 requestCode


    private Uri photoOutputUri = null; // 圖片最後輸出文件的 Uri

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        choiceFromAlbumButton = findViewById(R.id.choiceFromAlbumButton);
        choiceFromAlbumButton.setOnClickListener(clickListener);
        pictureImageView =  findViewById(R.id.pictureImage);

        if(ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            // 申請讀寫內存卡權限
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, WRITE_SDCARD_PERMISSION_REQUEST_CODE);
        }
    }

    private View.OnClickListener clickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
                if(v == choiceFromAlbumButton) {
                choiceFromAlbum();
            }
        }
    };
    // 從相簿中選取
    private void choiceFromAlbum() {
        // 打開圖檔系統 Action，等同於: "android.intent.action.GET_CONTENT"
        Intent choiceFromAlbumIntent = new Intent(Intent.ACTION_GET_CONTENT);
        // 設置數據類型為圖片類
        choiceFromAlbumIntent.setType("image/*");
        startActivityForResult(choiceFromAlbumIntent, CHOICE_FROM_ALBUM_REQUEST_CODE);
    }

    //裁剪圖片
    private void cropPhoto(Uri inputUri) {
        // 調用系統剪裁的動作 Action
        Intent cropPhotoIntent = new Intent("com.android.camera.action.CROP");
        // 設置數據及格式
        cropPhotoIntent.setDataAndType(inputUri, "image/*");
        // 授權應讀取Uri,否則程序會崩潰
        cropPhotoIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        // 設置圖片的最後輸出目的
        cropPhotoIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                photoOutputUri = Uri.parse("file:////sdcard/image_output.jpg"));
        startActivityForResult(cropPhotoIntent, CROP_PHOTO_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode == RESULT_OK) {
            // 通過返回程式碼判斷為何種動作
            switch (requestCode) {
                // 相簿選取
                case CHOICE_FROM_ALBUM_REQUEST_CODE:
                    cropPhoto(data.getData());
                    break;
                // 裁剪圖片
                case CROP_PHOTO_REQUEST_CODE:
                    File file = new File(photoOutputUri.getPath());
                    if(file.exists()) {
                        Bitmap bitmap = BitmapFactory.decodeFile(photoOutputUri.getPath());
                        pictureImageView.setImageBitmap(bitmap);
                    } else {
                        Toast.makeText(this, "找不到照片", Toast.LENGTH_SHORT).show();
                    }
                    break;
            }
        }
    }
}
