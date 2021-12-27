package org.techtown.insight;

import android.content.ContentValues;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.yanzhenjie.permission.Action;
import com.yanzhenjie.permission.AndPermission;
import com.yanzhenjie.permission.runtime.Permission;

import java.io.File;
import java.lang.reflect.Array;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class RecordActivity extends AppCompatActivity {
    MediaRecorder recorder;
    MediaPlayer player;

    File file;
    String filename;
    String time;    //파일 이름을 위한 변수

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record);

        Button button = findViewById(R.id.recordbutton);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startRecording();
            }
        });

        Button button2 = findViewById(R.id.stprecbutton);
        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopRecording();
            }
        });

        Button button3 = findViewById(R.id.playbutton);
        button3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startPlay();
            }
        });

        Button button4 = findViewById(R.id.pausebutton);
        button4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopPlay();
            }
        });

        Button button5 = findViewById(R.id.saverecbutton);
        button5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveRecord();
            }
        });

        file = getOutputFile();
        if(file != null) {
            filename = file.getAbsolutePath();
        }

        AndPermission.with(this)
                .runtime()
                .permission(
                        Permission.RECORD_AUDIO,
                        Permission.READ_EXTERNAL_STORAGE,
                        Permission.WRITE_EXTERNAL_STORAGE
                )
                .onGranted(new Action<List<String>>() {
                    @Override
                    public void onAction(List<String> data) {
                        Log.d("RecordActivity", data.size()+"개의 권한 허용");
                    }
                })
                .onDenied(new Action<List<String>>() {
                    @Override
                    public void onAction(List<String> data) {
                        Log.d("RecordActivity", data.size()+"개의 권한 거부");
                    }
                })
                .start();
    }

    public File getOutputFile() {
        File mediaFile = null;
        try {
            File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_RINGTONES), "MyInsight");
            if(!mediaStorageDir.exists()) {
                if(!mediaStorageDir.mkdirs()) {
                    Log.d("RecordActivity", "디렉토리 생성 실패");
                    return null;
                }
            }

            time = getTime();   //파일 이름을 현재 시간으로 한다.
            mediaFile = new File(mediaStorageDir.getPath()+File.separator+time+".mp3");
        } catch (Exception e) {
            e.printStackTrace();
        }

        return mediaFile;
    }

    private void startRecording() {
        if(recorder == null) {
            recorder = new MediaRecorder();
        }

        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);
        recorder.setOutputFile(filename);

        try {
            recorder.prepare();
            recorder.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void stopRecording() {
        if(recorder == null) {
            return;
        }

        recorder.stop();
        recorder.release();
        recorder = null;

        ContentValues values = new ContentValues(10);

        values.put(MediaStore.MediaColumns.TITLE, "Recorded");
        values.put(MediaStore.Audio.Media.ALBUM, "Audio Album");
        values.put(MediaStore.Audio.Media.ARTIST, "MyInsight");
        values.put(MediaStore.Audio.Media.DISPLAY_NAME, "Recorded Audio");
        values.put(MediaStore.Audio.Media.IS_RINGTONE, 1);
        values.put(MediaStore.Audio.Media.IS_MUSIC, 1);
        values.put(MediaStore.MediaColumns.DATE_ADDED, System.currentTimeMillis()/1000);
        values.put(MediaStore.MediaColumns.MIME_TYPE, "audio/mp3");
        values.put(MediaStore.Audio.Media.DATA, filename);

        Uri audioUri = getContentResolver().insert(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, values);
        if(audioUri == null) {
            Log.d("RecordActivity", "오디오 생성 실패");
            return;
        }
    }

    private void startPlay() {
        killMediaPlayer();

        try {
            player = new MediaPlayer();
            player.setDataSource(filename);
            player.prepare();
            player.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void stopPlay() {
        if(player != null) {
            player.stop();
        }
    }

    private void killMediaPlayer() {
        if(player != null) {
            try {
                player.release();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    //saveRecord : '저장하기' 버튼을 눌렀을 경우 추가된 녹음 파일의 이름을 인텐트로 보내
    //리사이클러뷰에 추가할 수 있게 한다.
    private void saveRecord() {
        Intent backIntent = new Intent();
        backIntent.putExtra("file", time+".mp3");
        setResult(RESULT_OK, backIntent);
        finish();
    }

    //onBackPressed : '저장히기' 버튼을 누르지 않고 그냥 뒤로 가기를 했을 경우,
    //기존에 저장되어 있던 녹음 파일을 다시 지운다.
    @Override
    public void onBackPressed() {
        if(filename != null) {
            String where = MediaStore.Audio.Media.DATA+"="+filename;
            int delResult = 0;

            delResult += getContentResolver().delete(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, where, null);


            if(delResult != 0) {
                Log.d("RecordActivity", "오류 : 녹음 취소 과정에서 문제가 생겼습니다.");
            } else {
                Log.d("RecordActivity", "삭제 결과 : "+delResult);
            }
        }

        super.onBackPressed();
    }

    //현재 시간을 String으로 출력하는 함수
    //파일 이름을 녹음 시작 시간으로 하기 위해서 만들었다.
    private String getTime() {
        long time = System.currentTimeMillis();
        Date date = new Date(time);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_hhmmss");

        return sdf.format(date);
    }
}
