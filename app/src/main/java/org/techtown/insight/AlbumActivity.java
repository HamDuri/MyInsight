package org.techtown.insight;

import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.VideoView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.yanzhenjie.permission.Action;
import com.yanzhenjie.permission.AndPermission;
import com.yanzhenjie.permission.runtime.Permission;

import java.io.InputStream;
import java.util.List;

public class AlbumActivity extends AppCompatActivity {
    ImageView imageView;
    VideoView videoView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_album);

        imageView = findViewById(R.id.imageView);
        videoView = findViewById(R.id.videoView);
        Button button = findViewById(R.id.galleryButton);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openGallery();
            }
        });

        AndPermission.with(this)
                .runtime()
                .permission(
                        Permission.READ_EXTERNAL_STORAGE,
                        Permission.WRITE_EXTERNAL_STORAGE
                )
                .onGranted(new Action<List<String>>() {
                    @Override
                    public void onAction(List<String> data) {
                        Log.d("AlbumActivity", "권한 허용");
                    }
                })
                .onDenied(new Action<List<String>>() {
                    @Override
                    public void onAction(List<String> data) {
                        Log.d("AlbumActivity", "권한 거부");
                    }
                })
                .start();
    }

    public void openGallery() {
        Intent intent = new Intent();
        intent.setType("image/* video/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);

        startActivityForResult(intent, 101);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Uri fileUri = data.getData();

        if(requestCode == 101) {
            if(resultCode == RESULT_OK) {
                if(fileUri.toString().contains("image")) {
                    imageView.setVisibility(View.VISIBLE);
                    videoView.setVisibility(View.INVISIBLE);

                    ContentResolver resolver = getContentResolver();

                    try {
                        InputStream instream = resolver.openInputStream(fileUri);
                        Bitmap imgBitmap = BitmapFactory.decodeStream(instream);

                        imageView.setImageBitmap(imgBitmap);
                        instream.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else if(fileUri.toString().contains("video")) {
                    videoView.setVisibility(View.VISIBLE);
                    imageView.setVisibility(View.INVISIBLE);

                    MediaController mc = new MediaController(this);
                    videoView.setMediaController(mc);
                    videoView.setVideoURI(fileUri);
                    videoView.requestFocus();
                    videoView.start();
                }
            }
        }
    }
}
