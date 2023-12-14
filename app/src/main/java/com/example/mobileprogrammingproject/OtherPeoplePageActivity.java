package com.example.mobileprogrammingproject;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.animation.ValueAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
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

    Button good_btn,bad_btn;
    //현재 temp값을 저장하는 변수
    int currentTemp;

    Toolbar toolbar;
    TableRow view_post_tr, favorites_tr;
    TextView nickname_tv, selfintro_tv,temp;
    String nickname, selfintro;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_other_people_page);

        //좋아요 버튼
        good_btn = findViewById(R.id.good_btn);
        //싫어요 버튼
        bad_btn = findViewById(R.id.bad_btn);
        temp = findViewById(R.id.temp);
        good_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateTempValue(1); // 좋아요 버튼 클릭 시 temp를 +1
                refreshPageContent(); // 페이지 내용 새로고침
            }
        });

        bad_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateTempValue(-1); // 싫어요 버튼 클릭 시 temp를 -1
                refreshPageContent(); // 페이지 내용 새로고침
            }
        });

        view_post_tr = findViewById(R.id.view_post_tr); // 작성한 소식
        favorites_tr = findViewById(R.id.favorites_tr); // 즐겨찾기 테이블
        nickname_tv = findViewById(R.id.nickname_tv);
        selfintro_tv = findViewById(R.id.selfintro_tv);

        intent = getIntent();
        other_user_id = intent.getStringExtra("other_user_id");

        progressBar = findViewById(R.id.progressBar);

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
                        if(documentSnapshot.exists()){
                            nickname = documentSnapshot.getString("nickname");
                            selfintro = documentSnapshot.getString("info");

                            // temp 값을 불러와서 저장하고 ProgressBar 및 TextView 업데이트
                            Number tempNumber = documentSnapshot.getLong("temp");
                            if (tempNumber != null) {
                                currentTemp = tempNumber.intValue();
                                animateProgressBar(currentTemp);

                                // TextView에 temp 값을 표시 (℃ 기호 추가)
                                temp.setText(currentTemp + "℃");
                            } else {
                                // temp 필드가 없는 경우, 기본값 설정 ("0℃")
                                temp.setText("0℃");
                            }

                            nickname_tv.setText(nickname);
                            selfintro_tv.setText(selfintro);
                        }
                    }
                });
    }

    private void refreshPageContent() {
        getUserData(other_user_id); // 사용자 데이터를 다시 로드하여 UI 업데이트
    }


    private void updateTempValue(int delta) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("user").document(other_user_id)
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if(documentSnapshot.exists()){
                            Number temp = documentSnapshot.getLong("temp");
                            if (temp != null) {
                                currentTemp = temp.intValue() + delta;
                                db.collection("user").document(other_user_id)
                                        .update("temp", currentTemp)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                animateProgressBar(currentTemp);
                                            }
                                        });
                            }
                        }
                    }
                });
    }
}