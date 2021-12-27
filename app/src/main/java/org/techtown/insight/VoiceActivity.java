package org.techtown.insight;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.yanzhenjie.permission.Action;
import com.yanzhenjie.permission.AndPermission;
import com.yanzhenjie.permission.runtime.Permission;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class VoiceActivity extends AppCompatActivity {
    String path;
    File directory;
    File[] files;
    List<String> filesNameList = new ArrayList<String>();
    RecordAdapter adapter;
    RecyclerView recyclerView;
    MediaPlayer player;
    File mediaStorageDir;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voice);

        recyclerView = findViewById(R.id.recyclerView);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(layoutManager);
        adapter = new RecordAdapter();

        mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_RINGTONES), "MyInsight");
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.d("VoiceActivity", "디렉토리 생성 실패");
            }
        }

        getFileNames();

        Button button = findViewById(R.id.recButton);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), RecordActivity.class);
                startActivityForResult(intent, 101);
            }
        });

        //외부 파일을 선택했을 때 그 파일의 위치를 저장해서 여기에 출력하는 방법을 찾지 못함.
        //앱을 재실행할 때 외부 파일이 리스트에 그대로 남아있게 하는 방법을 찾지 못함.
        //일단 녹음까지만 제출하고 다시 찾아볼 예정... 엉엉
        Button button2 = findViewById(R.id.addButton);
        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //searchFile();
                showToast("아직 준비 중입니다.");
            }
        });

        recyclerView.setAdapter(adapter);

        //카드뷰 하나를 눌렀을 때 파일이 재생되게 하기
        adapter.setOnItemClickListener(new OnRecordItemClickListener() {
            @Override
            public void onItemClick(RecordAdapter.ViewHolder holder, View view, int position) {
                //여기부터 RecordActivity의 getOutputFile()의 일부를 가져옴.
                File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                        Environment.DIRECTORY_RINGTONES), "MyInsight");
                String item = adapter.getItem(position).getFilename();  //얘는 아니고
                File mediaFile = new File(mediaStorageDir.getPath()+File.separator+item);
                //여기까지
                player = new MediaPlayer();
                try {
                    player.setDataSource(String.valueOf(mediaFile));
                    player.prepare();
                    player.start();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void showToast(String data) {
        Toast.makeText(this, data, Toast.LENGTH_LONG).show();
    }

    public void getFileNames() {
        path = mediaStorageDir.toString();
        directory = new File(path);
        files = directory.listFiles();
        if(files != null) {
            for (int i = 0; i < files.length; i++) {
                filesNameList.add(files[i].getName());
                adapter.addItem(new Record(filesNameList.get(i)));
            }
        }
    }

    //외부 파일 가져오기 위한 함수
    //여기까지는 문제가 없을 텐데 그 다음이 문제라 일단 위에서 주석 처리 해놓음.
    public void searchFile() {
        Intent intent = new Intent();
        intent.setType("audio/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);

        startActivityForResult(intent, 102);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == 101) {
            if(resultCode == RESULT_OK) {
                String file = data.getStringExtra("file");
                adapter.addItem(new Record(file));
            }
            recyclerView.setAdapter(adapter);
        } else if(requestCode == 102) {
            //102는 외부 파일 가져왔을 때 쓰려고 배정한 코드
            //근데 지금 와서 생각해보니 여기다가 적을 필요가 없을 것 같기도...?
            //일단 여기서 막혀서 주석 처리 해놓음.
            if(resultCode == RESULT_OK) {
                /*ContentResolver resolver = getContentResolver();

                Uri recordUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                String id = recordUri.getLastPathSegment();
                getFileName(id);
                 */
            }
        }
    }

    //외부 파일의 이름 가져오는 함수
    //얘도 일단 위에서 주석처리 해놓음.
    public void getFileName(String id) {
        Cursor cursor = null;
        String name = "";

        try {
            cursor = getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    null,
                    MediaStore.Audio.Media._ID+"= ?",
                    new String[] {id},
                    null);

            if(cursor.moveToFirst()) {
                name = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));
                adapter.addItem(new Record(name));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}