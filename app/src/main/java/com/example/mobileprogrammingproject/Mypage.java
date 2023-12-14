package com.example.mobileprogrammingproject;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class Mypage extends AppCompatActivity {

    ProgressBar progressBar;
    Button changeButton;
    TableRow view_post_tr, view_comment_tr, favorites_tr, logout_tr;
    FirebaseFirestore db;

    TextView nickname_tv,personal_temp;
    String user_id, nickname;
    Toolbar toolbar;
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mypage);
        progressBar = findViewById(R.id.progressBar);

        personal_temp = findViewById(R.id.personal_temp);

        changeButton = findViewById(R.id.profileChange_btn); // 프로필 수정 버튼

        view_post_tr = findViewById(R.id.view_post_tr); // 내가 작성한 소식 테이블
        view_comment_tr = findViewById(R.id.view_comment_tr); // 내가 남긴 댓글 테이블
        favorites_tr = findViewById(R.id.favorites_tr); // 즐겨찾기 테이블
        logout_tr = findViewById(R.id.logout_tr); // 로그아웃 테이블
        nickname_tv = findViewById(R.id.nickname);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true); // 툴바 왼쪽에, 뒤로가기 버튼 추가.

        user_id = FirebaseAuth.getInstance().getUid(); // user_id 가져오기

        db = FirebaseFirestore.getInstance(); // 현재 파이어스토어의 인스턴스 불러오기.


        db.collection("user").document(user_id).get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if(documentSnapshot.exists()){
                            nickname = documentSnapshot.getString("nickname");
                            nickname_tv.setText(nickname);

                            // temp 값을 불러와서 TextView에 표시
                            Number tempNumber = documentSnapshot.getLong("temp");
                            if (tempNumber != null) {
                                personal_temp.setText(tempNumber.intValue() + "℃");
                                int currentTemp = tempNumber.intValue();
                                animateProgressBar(currentTemp);
                            } else {
                                // temp 필드가 없는 경우, 기본값 설정 ("0℃")
                                personal_temp.setText("0℃");
                            }
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(Mypage.this, "유저 데이터 불러오기가 실패하였습니다.", Toast.LENGTH_SHORT).show();
                    }
                });

        changeButton.setOnClickListener(new View.OnClickListener(){  // 프로필 수정 버튼 클릭 시, 액티비티 이동
            public void onClick(View v){
                Intent intent = new Intent(getApplicationContext(), ProfileChange.class);
                startActivity(intent);
            }
        });

        view_post_tr.setOnClickListener(new View.OnClickListener() { // 내가 작성한 소식 클릭시, 액티비티 이동
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), ViewPostActivity.class);
                startActivity(intent);
            }
        });

        view_comment_tr.setOnClickListener(new View.OnClickListener() { // 내가 남긴 댓글 클릭시, 액티비티 이동
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), ViewCommentActivity.class);
                startActivity(intent);
            }
        });

        favorites_tr.setOnClickListener(new View.OnClickListener() { //  즐겨찾기 클릭시, 액티비티 이동
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), MyFavoritesActivity.class);
                startActivity(intent);
            }
        });

        logout_tr.setOnClickListener(new View.OnClickListener() { // 로그아웃 클릭시, 
            @Override
            public void onClick(View v) { // 파이어베이스 로그아웃 후, 로그인 화면으로 이동
                FirebaseAuth.getInstance().signOut();
                //finish();
//
//                Intent intent = new Intent(Mypage.this, Login.class);
//                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
//                startActivity(intent);
                Intent intent = new Intent(Mypage.this, Login.class);
                startActivity(intent);
                finish();
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

    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(Mypage.this, MainPageActivity.class);
        startActivity(intent);
        finish();
    }
}