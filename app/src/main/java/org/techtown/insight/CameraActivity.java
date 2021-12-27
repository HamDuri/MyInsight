package org.techtown.insight;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.yanzhenjie.permission.Action;
import com.yanzhenjie.permission.AndPermission;
import com.yanzhenjie.permission.runtime.Permission;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CameraActivity extends AppCompatActivity {
    CameraSurfaceView cameraView;
    private Camera camera = null;

    MediaPlayer player;
    MediaRecorder recorder;

    //저장파일지정
    File file;
    String filename;
    //서피스뷰 : 미리보기 화면지정
    SurfaceHolder holder;
    private String SaveFolderPath;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        FrameLayout previewFrame = findViewById(R.id.previewFrame);
        cameraView = new CameraSurfaceView(this);
        previewFrame.addView(cameraView);

        SurfaceView surface = new SurfaceView(this);
        holder = surface.getHolder();
        previewFrame.addView(surface);

        //캡처버튼
        Button button = findViewById(R.id.capButton);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                takePicture();
            }
        });

        //녹화버튼
        Button button2 = findViewById(R.id.shootButton);
        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startRecording();
            }
        });

        //녹화중지버튼
        Button button3 = findViewById(R.id.stopButton);
        button3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopRecording();
            }
        });


        file = getOutputFile();
        if (file != null){
            filename = file.getAbsolutePath();

        }

        //위험권한
        AndPermission.with(this)
                .runtime()
                .permission(
                        Permission.CAMERA,
                        Permission.RECORD_AUDIO,
                        Permission.READ_EXTERNAL_STORAGE,
                        Permission.WRITE_EXTERNAL_STORAGE)
                .onGranted(new Action<List<String>>() {
                    @Override
                    public void onAction(List<String> permissions) {
                        showToast("허용된 권한 갯수 : " + permissions.size());
                    }
                })
                .onDenied(new Action<List<String>>() {
                    @Override
                    public void onAction(List<String> permissions) {
                        showToast("거부된 권한 갯수 : " + permissions.size());
                    }
                })
                .start();

    }


    public void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    public void takePicture() {
        cameraView.capture(new Camera.PictureCallback(){

            //사진찍으면 자동으로 호출되는 onPictureTaken 메서드로 캡처한 이미지 데이터가 전달된다.
            @Override
            public void onPictureTaken(byte[] data, Camera camera) {
                try {
                    //전달받은 바이트 배열을 Bitmap객체로 만들기 (이미지 데이터를 비트맵으로 만들기위해 decodeByteArray 메서드 이용)
                    Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
                    String outUriStr = MediaStore.Images.Media.insertImage(getContentResolver(), bitmap,"MyInsight Image", "Captured Image using Camera.");  //비트맵이미지의 제목, 내용
                    if (outUriStr == null){
                        Log.d("SampleCapture", "Image insert failed.");
                        return;
                    }else {
                        Uri outUri = Uri.parse(outUriStr);
                        sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, outUri));
                    }
                    camera.startPreview();
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        });
        Toast.makeText(this, "사진이 저장되었습니다.", Toast.LENGTH_SHORT).show();
    }

    public File getOutputFile() {  //외부에 파일을 저장하겠다.
        File mediaFile = null;
        try {
            File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_PICTURES), "MyInsight");
            if (!mediaStorageDir.exists()){
                if (!mediaStorageDir.mkdirs()){
                    Log.d("MyCameraApp", "failed to create directory");
                    return null;
                }
            }
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HHmmssSSS", Locale.US);
            mediaFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath().toString()
                    + "/MyInsight/RECORDING_"
                    + dateFormat.format(new Date())
                    + ".mp4");
        } catch(Exception e) {
            e.printStackTrace();
        }
        return mediaFile;

    }

    //녹화시작버튼
    public void startRecording() {
        if (recorder == null){
            recorder = new MediaRecorder();
        }
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);  //비디오 입력정보
        recorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH));       //화질좋게
        //recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        //recorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);
        //recorder.setVideoEncoder(MediaRecorder.VideoEncoder.DEFAULT);//인코더 정보설정
        recorder.setOutputFile(filename);
        //DisplayMetrics displayMetrics = Resources.getSystem().getDisplayMetrics();      //화질좋게
        //recorder.setVideoSize(displayMetrics.widthPixels, displayMetrics.heightPixels); //화질좋게
        recorder.setVideoFrameRate(24);
        recorder.setVideoSize(3840, 2160);
        recorder.setVideoEncodingBitRate(3000000);
        recorder.setAudioEncodingBitRate(8000);

        //MediaRecorder에 미리보기 화면을 보여줄 객체 설정하기
        recorder.setPreviewDisplay(holder.getSurface());

        try{
            recorder.prepare();
            recorder.start();
        }catch (Exception e){
            e.printStackTrace();

            recorder.release();
            recorder = null;
        }

        Toast.makeText(getApplicationContext(), "녹화가 시작되었습니다.", Toast.LENGTH_SHORT).show();
    }


    //녹화중지버튼
    public void stopRecording() {
        if (recorder == null) {
            return;
        }

        recorder.stop();
        recorder.reset();
        recorder.release();
        recorder = null;

        Toast.makeText(getApplicationContext(), "녹화가 저장되었습니다.", Toast.LENGTH_SHORT).show();

        //미디어 앨범에 녹화된 동영상을 등록하기 위해서 내용제공자인 ContentValues 객체를 사용한다.
        ContentValues values = new ContentValues(10);

        //put 메소드에 입력한 후 insert 를 이용하여 추가하면서 내용제공자의 접근통로를 지정한다.
        values.put(MediaStore.MediaColumns.TITLE, "RecordedVideo");
        values.put(MediaStore.Audio.Media.ALBUM, "Video Album");
        values.put(MediaStore.Audio.Media.ARTIST, "Mike");
        values.put(MediaStore.Audio.Media.DISPLAY_NAME, "Recorded Video");
        values.put(MediaStore.MediaColumns.DATE_ADDED, System.currentTimeMillis() / 1000);
        values.put(MediaStore.MediaColumns.MIME_TYPE, "video/mp4");
        values.put(MediaStore.Audio.Media.DATA, filename);

        //ContentResolver의 insert 메서드 호출하여 저장하기(getContentResolve 는 내용제공자의 접근통로를 찾아서 videoUri에 대입한다.)
        Uri videoUri = getContentResolver().insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values);
        if (videoUri == null) {
            Log.d("SampleVideoRecorder", "Video insert failed.");
            return;
        }

        //미디어 앨범에 저장되었다는 정보를 다른앱에도 알려주고싶다면 URI객체를 브로드캐스팅하면 된다.
        sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, videoUri));

    }



    

    //SurfaceView 클래스를 상속하고 Callback 인터페이스를 구현하는 새로운 CameraSurfaceView 클래스 정의
    class CameraSurfaceView extends SurfaceView implements SurfaceHolder.Callback {
        private SurfaceHolder mHolder;
        private Camera camera = null;

        public CameraSurfaceView(Context context) {
            super(context);

            //생성자에서 서피스홀더 객체 참조 후 설정
            mHolder = getHolder();
            mHolder.addCallback(this);
        }

        //서피스뷰가 만들어질 때
        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            camera = Camera.open(); //카메라 객체를 참조 한 후

            //화면이 가로로 돌아가있어서 세로로 보이게 추가
            setCameraOrientation();

            try {
                camera.setPreviewDisplay(mHolder);  //미리보기화면으로 홀더객체 생성
            } catch (Exception e){
                e.printStackTrace();
            }
        }

        //화면 세로로 보이게 코드추가
        public void setCameraOrientation() {
            if (camera == null) {
                return;
            }
            Camera.CameraInfo info = new Camera.CameraInfo();
            Camera.getCameraInfo(0, info);

            WindowManager manager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
            int rotation = manager.getDefaultDisplay().getRotation();   //회전에 대한 정보확인하기

            int degrees = 0;
            switch (rotation){
                case Surface.ROTATION_0 : degrees = 0; break;
                case Surface.ROTATION_90 : degrees = 90; break;
                case Surface.ROTATION_180 : degrees = 180; break;
                case Surface.ROTATION_270 : degrees = 270; break;
            }
            int result;
            if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT){
                result = (info.orientation + degrees) % 360;
                result = (360 - result) % 360;
            }else {
                result = (info.orientation - degrees + 360) % 360;
            }

            camera.setDisplayOrientation(result);       //카메라 객체의 setDisplayOrientation 메서드 호출하기
        }

        //서피스뷰의 화면 크기가 바뀌는 등의 변경 시점에 미리보기 시정
        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            camera.startPreview();
        }

        //서피스뷰가 없어질 때 미리보기 중지
        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            camera.stopPreview();
            camera.release();
            camera = null;
        }

        //카메라 객체의 takePicture메서드를 호출하여 사진촬영
        public boolean capture(Camera.PictureCallback handler){
            if (camera != null){
                camera.takePicture(null, null, handler);
                return true;
            }else {
                return false;
            }
        }
    }
}