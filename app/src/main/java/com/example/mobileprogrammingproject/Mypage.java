package com.example.mobileprogrammingproject;

import androidx.appcompat.app.AppCompatActivity;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;

public class Mypage extends AppCompatActivity {

    ProgressBar progressBar;
    Button changeButton;
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mypage);
        progressBar = findViewById(R.id.progressBar);
        animateProgressBar(36); // 원하는 프로그래스 값으로 애니메이션 실행

        changeButton = findViewById(R.id.profileChange_btn); // 프로필 수정 버튼

        changeButton.setOnClickListener(new View.OnClickListener(){  // 프로필 수정 버튼 클릭 시, 프로필 변경 액티비티로 이동
            public void onClick(View v){
                Intent intent = new Intent(getApplicationContext(), ProfileChange.class);
                startActivity(intent);
            }
        });
    }
    private void animateProgressBar(int targetProgress) {
        ValueAnimator animator = ValueAnimator.ofInt(0, targetProgress);
        animator.setDuration(500); // 애니메이션 기간 (1초로 설정, 필요에 따라 조절)

        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int progress = (int) animation.getAnimatedValue();
                progressBar.setProgress(progress);
            }
        });

        animator.start();
    }
}