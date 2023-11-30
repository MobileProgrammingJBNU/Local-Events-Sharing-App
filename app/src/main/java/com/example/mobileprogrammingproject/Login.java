package com.example.mobileprogrammingproject;

import android.annotation.SuppressLint;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
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

import com.google.android.gms.common.SignInButton;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;


public class Login extends AppCompatActivity {
     TextView sign;
     Button login_btn;

    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private SharedPreferences appData;
    private String id;
    private boolean saveLoginData;

    EditText email_et, pw_et;
    String email, pw;

     protected void onCreate(Bundle savedInstanceState){
           super.onCreate(savedInstanceState);

           setContentView(R.layout.activity_login);
            //회원가입 버튼
           sign = findViewById(R.id.signin);

            //회원가입 누르면 페이지 이동
           sign.setOnClickListener(v -> {
                Intent intent = new Intent(this, Signup.class);
                startActivity(intent);
           });
           // 로그인 버튼
         login_btn = findViewById(R.id.loginbutton);

         appData = getSharedPreferences("appData", MODE_PRIVATE);
         load();

         login_btn.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 Intent intent = new Intent(Login.this, MainPageActivity.class);
                 startActivity(intent);

                 email_et = findViewById(R.id.email_et);
                 pw_et = findViewById(R.id.pw_et);

                 email = email_et.getText().toString();
                 pw = pw_et.getText().toString();

                 mAuth.signInWithEmailAndPassword(email, pw)
                         .addOnCompleteListener(Login.this, new OnCompleteListener<AuthResult>() {
                             @Override
                             public void onComplete(@NonNull Task<AuthResult> task) {
                                 if (task.isSuccessful()) {//성공했을때
                                     Intent intent = new Intent(Login.this, MainPageActivity.class);
                                     startActivity(intent);
                                 } else {//실패했을때
                                     Toast.makeText(Login.this, "로그인 오류", Toast.LENGTH_SHORT).show();
                                 }
                             }
                         });
                 save();
                 //Intent intent = new Intent(LoginActivity.this, PostActivity.class);
                 // startActivity(intent);
                 //Toast.makeText(LoginActivty.this, "click", Toast.LENGTH_SHORT).show(); // 로그인 버튼 눌렀을 때 동작. 잠시 toast로 대체

             }
         });
     }
    private void save() {
        // SharedPreferences 객체만으론 저장 불가능 Editor 사용
        SharedPreferences.Editor editor = appData.edit();

        // 에디터객체.put타입( 저장시킬 이름, 저장시킬 값 )
        // 저장시킬 이름이 이미 존재하면 덮어씌움
        editor.putBoolean("SAVE_LOGIN_DATA", false);
        editor.putString("ID", email);

        // apply, commit 을 안하면 변경된 내용이 저장되지 않음
        editor.apply();
    }

    // 설정값을 불러오는 함수
    private void load() {
        // SharedPreferences 객체.get타입( 저장된 이름, 기본값 )
        // 저장된 이름이 존재하지 않을 시 기본값
        saveLoginData = appData.getBoolean("SAVE_LOGIN_DATA", false);
        id = appData.getString("ID", "");
    }
 }