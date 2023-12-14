package com.example.mobileprogrammingproject;

import android.annotation.SuppressLint;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowInsets;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.mobileprogrammingproject.utility.FirebaseID;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;

public class Signup extends AppCompatActivity {
    EditText name_et, pw_et, email_et, pw2_et, nickname_et;
    Button pwcheck, submit;
    String email, pw, nickname;

    private FirebaseFirestore mStore;
    private FirebaseAuth mAuth;

    FirebaseAuth auth = FirebaseAuth.getInstance();

    //회원가입시 닉네임 중복 체크 함수
    private void checkNicknameExists(String nickname, final OnCompleteListener<QuerySnapshot> onCompleteListener) {
        mStore.collection(FirebaseID.user)
                .whereEqualTo(FirebaseID.nickname, nickname)
                .get()
                .addOnCompleteListener(onCompleteListener);
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });


        //작성 항목

        mStore = FirebaseFirestore.getInstance(); // 파이어베이스 스토어
        mAuth = FirebaseAuth.getInstance(); // 파이어베이스 인스턴스 설정

        //비번 확인
        pwcheck = findViewById(R.id.pwcheckbutton);
        pw_et = findViewById(R.id.signPW);
        pw2_et = findViewById(R.id.signPW2);

        pwcheck.setOnClickListener(v -> {
            if(pw_et.getText().toString().equals(pw2_et.getText().toString())){
                pwcheck.setText("일치");
            }else{
                Toast.makeText(Signup.this, "비밀번호가 다릅니다.", Toast.LENGTH_LONG).show();
            }
        });

        //회원가입 완료 버튼
        submit = findViewById(R.id.signupbutton);

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                email_et = findViewById(R.id.signEmail);
                pw_et = findViewById(R.id.signPW);
                pw2_et = findViewById(R.id.signPW2); //비밀번호 확인 edit text
                nickname_et = findViewById(R.id.signName);

                email = email_et.getText().toString();
                pw = pw_et.getText().toString();
                String pw2 = pw2_et.getText().toString(); // 두 번째 비밀번호 입력값
                nickname = nickname_et.getText().toString();

                if(!pw.equals(pw2)) {
                    Toast.makeText(Signup.this, "비밀번호가 다릅니다.", Toast.LENGTH_LONG).show();
                    return; // 비밀번호가 다를 경우 함수 종료
                }
                    if(!email.isEmpty() && !pw.isEmpty() && !nickname.isEmpty()) {
                if ((email != null) && !email.isEmpty() && (pw != null) && !pw.isEmpty() && (nickname != null) && !nickname.isEmpty()) {
                    //닉네임 중복체크 함수를 블러와서 확인
                    checkNicknameExists(nickname, new OnCompleteListener<QuerySnapshot>() {
                        @Override // 파이어베이스에서 닉네임 중복 체크
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                if (task.getResult() != null && !task.getResult().isEmpty()) {
                                    Toast.makeText(Signup.this, "닉네임이 이미 존재합니다.", Toast.LENGTH_SHORT).show();
                                } else {
                                    mAuth.createUserWithEmailAndPassword(email, pw)
                                            .addOnCompleteListener(Signup.this, new OnCompleteListener<AuthResult>() {
                                                @Override
                                                public void onComplete(@NonNull Task<AuthResult> task) {

                                                    if (task.isSuccessful()) {
                                                        Toast.makeText(Signup.this, "회원가입 성공",
                                                                Toast.LENGTH_SHORT).show();

                                                        // Sign in success, update UI with the signed-in user's information
                                                        FirebaseUser user = mAuth.getCurrentUser();

                                                        Map<String, Object> userMap = new HashMap<>();
                                                        userMap.put(FirebaseID.user_id, user.getUid());
                                                        userMap.put(FirebaseID.email, email);
                                                        userMap.put(FirebaseID.password, pw);
                                                        userMap.put(FirebaseID.nickname, nickname);
                                                        userMap.put(FirebaseID.info, null); // 처음 자기소개는 null
                                                        //현재 유저의 Uid를 이름으로 한 document 생성. 이게 없으면 사용자 컨텐츠의 이륾과 사용자id이름이 달라 사용하기 힘듬
                                                        mStore.collection(FirebaseID.user).document(user.getUid()).set(userMap, SetOptions.merge());

                                                        //회원가입 성공시 로그인 액티비티로 화면 전환
                                                        Intent intent = new Intent(Signup.this, Login.class);

                                                        startActivity(intent);
                                                        finish();
                                                    } else {
                                                        Exception exception = task.getException();
                                                        if (exception != null) {
                                                            Toast.makeText(Signup.this, "회원가입 실패",
                                                                    Toast.LENGTH_SHORT).show();
                                                        } else {
                                                            Toast.makeText(Signup.this, "회원가입 실패", Toast.LENGTH_SHORT).show();
                                                        }
                                                    }
                                                }
                                            });
                                }
                            } else {
                                Toast.makeText(Signup.this, "닉네임 체크 중 오류가 발생했습니다.", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }
            }
        });
    }
}