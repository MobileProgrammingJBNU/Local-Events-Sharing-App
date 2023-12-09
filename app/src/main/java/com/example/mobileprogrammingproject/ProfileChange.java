package com.example.mobileprogrammingproject;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class ProfileChange extends AppCompatActivity {

    Toolbar toolbar;
    TextView email_tv;
    EditText info_et, nickname_et;

    String email, info, nickname, user_id;
    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_change);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true); // 툴바 왼쪽에, 뒤로가기 버튼 추가.

        email_tv = findViewById(R.id.email_tv);
        info_et = findViewById(R.id.info_et);
        nickname_et = findViewById(R.id.nickname_et);

        user_id = FirebaseAuth.getInstance().getUid();

        db = FirebaseFirestore.getInstance(); // 현재 파이어스토어의 인스턴스 불러오기.

        db.collection("user").document(user_id).get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if(documentSnapshot.exists()){
                            email = documentSnapshot.getString("email");
                            nickname = documentSnapshot.getString("nickname");
                            info = documentSnapshot.getString("info");

                            if(email != null)
                                email_tv.setText(email);
                            if(nickname != null)
                                nickname_et.setText(nickname);
                            if(info != null)
                                info_et.setText(info);
                        }
                    }
                });
    }

    /**
     * 툴바 오른쪽에 '완료' 버튼 추가.
     * @param menu The options menu in which you place your items.
     * @return true
     */
    public boolean onCreateOptionsMenu(Menu menu) { // 액티비티가 생성될 때 자동으로 호출.
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.complete_btn, menu); // 완료 버튼 추가

        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item){ // 액션바 항목 선택시 실행되는 메소드.
        if(item.getItemId() == R.id.toolbar_complete_btn){ // 완료 버튼 눌렀을 시 동작. 잠시 toast로 대체.
            Map<String, Object> updateData = new HashMap<>();
            updateData.put("nickname", nickname_et.getText().toString());
            updateData.put("info", info_et.getText().toString());

            db.collection("user").document(user_id).update(updateData)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            Toast.makeText(ProfileChange.this, "사용자 정보가 성공적으로 업데이트 되었습니다.", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(ProfileChange.this, "사용자 정보가 업데이트가 실패하였습니다.", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
        return super.onOptionsItemSelected(item);
    }
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(ProfileChange.this, Mypage.class);
        startActivity(intent);
        finish();
    }
}