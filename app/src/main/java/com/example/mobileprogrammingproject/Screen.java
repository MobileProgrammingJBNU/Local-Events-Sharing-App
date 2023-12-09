package com.example.mobileprogrammingproject;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.animation.ValueAnimator;
import android.os.Bundle;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.checkerframework.checker.units.qual.C;


public class Screen extends AppCompatActivity {
    ProgressBar progressBar;
    Toolbar toolbar;

    DatabaseReference databaseReference;
    public static class Comment {
        private String message;
        private String userId;
        private long timestamp;

        // Default constructor required for calls to DataSnapshot.getValue(Comment.class)
        public Comment() {
        }

        public Comment(String message, long timestamp, String userId) {
            this.message = message;
            this.timestamp = timestamp;
            this.userId = userId;
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_screen);
        progressBar = findViewById(R.id.progressBar);
        animateProgressBar(36); // 원하는 프로그래스 값으로 애니메이션 실행
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true); // 툴바 왼쪽에, 뒤로가기 버튼 추가.


        FirebaseDatabase firebaseDatabase=FirebaseDatabase.getInstance();
        databaseReference-firebaseDatabase.getReference("comments");
        Button send = findViewById(R.id.comment_btn_send);
        EditText comment_text = findViewById(R.id.comment_edit_message);
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = comment_text.getText().toString();
                long timestamp = System.currentTimeMillis(); // 밀리초 단위 현재 시간
                String userId = //bd에서 유저id가져오기

                        saveCommentToFirebase(userId, timestamp, message);
            }
        });
    }
    private void saveCommentToFirebase(String userId, long timestamp, String message) {
        String commentId = databaseReference.push().getKey();
        Comment comment = new Comment(userId, timestamp, message);
        databaseReference.child(commentId).setValue(comment);
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
    public void onBackPressed() {
        super.onBackPressed();
        // 여기에 원하는 동작을 추가하세요

        // 원래의 뒤로가기 동작을 유지하고 싶다면 super.onBackPressed()를 호출하세요.
        Intent intent = new Intent(Screen.this, MainPageActivity.class);
        startActivity(intent);
        finish();
    }
}