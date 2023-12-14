package com.example.mobileprogrammingproject;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class Login extends AppCompatActivity {
    TextView sign;
    Button login_btn;
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    EditText email_et, pw_et;
    CheckBox id_save;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        sign = findViewById(R.id.signin);
        login_btn = findViewById(R.id.loginbutton);
        email_et = findViewById(R.id.email_et);
        pw_et = findViewById(R.id.pw_et);
        id_save = findViewById(R.id.id_save);

        sharedPreferences = getSharedPreferences("LoginPrefs", MODE_PRIVATE);
        loadLoginData();

        sign.setOnClickListener(v -> {
            Intent intent = new Intent(this, Signup.class);
            startActivity(intent);
        });

        login_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = email_et.getText().toString();
                String pw = pw_et.getText().toString();

                mAuth.signInWithEmailAndPassword(email, pw)
                        .addOnCompleteListener(Login.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    saveLoginData();
                                    Intent intent = new Intent(Login.this, MainPageActivity.class);
                                    startActivity(intent);
                                } else {
                                    Toast.makeText(Login.this, "로그인 오류", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        });
    }

    private void saveLoginData() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("SaveLogin", id_save.isChecked());
        if (id_save.isChecked()) {
            editor.putString("Email", email_et.getText().toString());
            editor.putString("Password", pw_et.getText().toString());
        } else {
            editor.remove("Email");
            editor.remove("Password");
        }
        editor.apply();
    }

    private void loadLoginData() {
        boolean saveLogin = sharedPreferences.getBoolean("SaveLogin", false);
        id_save.setChecked(saveLogin);

        if (saveLogin) {
            email_et.setText(sharedPreferences.getString("Email", ""));
            pw_et.setText(sharedPreferences.getString("Password", ""));
        }
    }

}
