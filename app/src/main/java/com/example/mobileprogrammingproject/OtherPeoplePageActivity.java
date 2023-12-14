package com.example.mobileprogrammingproject;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.animation.ValueAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TableRow;
import android.widget.TextView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class OtherPeoplePageActivity extends AppCompatActivity {

    Intent intent;
    String other_user_id;
    ProgressBar progressBar;

    Toolbar toolbar;
    TableRow view_post_tr, favorites_tr;
    TextView nickname_tv, selfintro_tv;
    String nickname, selfintro;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_other_people_page);

        view_post_tr = findViewById(R.id.view_post_tr); // 작성한 소식
        favorites_tr = findViewById(R.id.favorites_tr); // 즐겨찾기 테이블
        nickname_tv = findViewById(R.id.nickname_tv);
        selfintro_tv = findViewById(R.id.selfintro_tv);

        intent = getIntent();
        other_user_id = intent.getStringExtra("other_user_id");

        progressBar = findViewById(R.id.progressBar);
        animateProgressBar(36); // 원하는 프로그래스 값으로 애니메이션 실행

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true); // 툴바 왼쪽에, 뒤로가기 버튼 추가.


        getUserData(other_user_id);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        view_post_tr.setOnClickListener(new View.OnClickListener() { // 작성한 소식 클릭시, 액티비티 이동
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(OtherPeoplePageActivity.this, ViewPostActivity.class);
                intent.putExtra("other_user_id", other_user_id);
                startActivity(intent);
            }
        });


        favorites_tr.setOnClickListener(new View.OnClickListener() { //  관심목록 클릭시, 액티비티 이동
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(OtherPeoplePageActivity.this, MyFavoritesActivity.class);
                intent.putExtra("other_user_id", other_user_id);
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

    private void getUserData(String UserID) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("user").document(UserID).get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        DocumentSnapshot result = documentSnapshot;
                        if(result.exists()){
                            nickname = result.getString("nickname");
                            selfintro = result.getString("info");

                            nickname_tv.setText(nickname);
                            selfintro_tv.setText(selfintro);
                        }
                    }
                });
    }
}