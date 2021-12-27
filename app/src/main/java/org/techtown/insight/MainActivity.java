package org.techtown.insight;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button button = findViewById(R.id.cameraButton);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), CameraActivity.class);
                startActivity(intent);
                Toast.makeText(getApplicationContext(), "촬영하기 버튼", Toast.LENGTH_SHORT).show();
            }
        });

        Button button2 = findViewById(R.id.fileButton);
        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), AlbumActivity.class);
                startActivity(intent);
                Toast.makeText(getApplicationContext(), "내 파일 확인 버튼", Toast.LENGTH_SHORT).show();
            }
        });

        Button button3 = findViewById(R.id.voiceButton);
        button3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), VoiceActivity.class);
                startActivity(intent);
                Toast.makeText(getApplicationContext(), "음성재생 버튼", Toast.LENGTH_SHORT).show();
            }
        });

        Button button4 = findViewById(R.id.button4);
        button4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), "나를 지키는 혜안 캠 입니다", Toast.LENGTH_SHORT).show();
            }
        });

    }
}