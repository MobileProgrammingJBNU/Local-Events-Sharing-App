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

    //닉네임 중복 체크 함수
    private void checkNicknameExists(String nickname, OnSuccessListener<Void> onSuccessListener, OnFailureListener onFailureListener) {
        db.collection("user")
                .whereEqualTo("nickname", nickname)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    boolean isDuplicate = false;
                    for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                        if (!document.getId().equals(user_id)) {
                            isDuplicate = true;
                            break;
                        }
                    }
                    if (isDuplicate) {
                        onSuccessListener.onSuccess(null); // 닉네임 중복
                    } else {
                        onFailureListener.onFailure(null); // 닉네임 중복 아님
                    }
                });
    }


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

    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.toolbar_complete_btn) {
            String newNickname = nickname_et.getText().toString();

            checkNicknameExists(newNickname, unused -> {
                Toast.makeText(ProfileChange.this, "중복된 닉네임입니다.", Toast.LENGTH_SHORT).show();
            }, unused -> {
                Map<String, Object> updateData = new HashMap<>();
                updateData.put("nickname", newNickname);
                updateData.put("info", info_et.getText().toString());

                db.collection("user").document(user_id).update(updateData)
                        .addOnSuccessListener(aVoid -> Toast.makeText(ProfileChange.this, "사용자 정보가 성공적으로 업데이트 되었습니다.", Toast.LENGTH_SHORT).show())
                        .addOnFailureListener(e -> Toast.makeText(ProfileChange.this, "사용자 정보 업데이트가 실패하였습니다.", Toast.LENGTH_SHORT).show());
            });

            return true;
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