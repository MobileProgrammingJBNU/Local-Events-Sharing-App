package com.example.mobileprogrammingproject;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.animation.ValueAnimator;
import android.os.Bundle;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

import org.checkerframework.checker.units.qual.C;
import org.w3c.dom.Comment;

import java.util.HashMap;
import java.util.Map;


public class Screen extends AppCompatActivity {
    Toolbar toolbar;

    DatabaseReference databaseReference;
    String user_id, nickname, post_id;
    String Title, Content, StartTime, StartDate, EndTime, EndDate, Location, img;

    String StartDateTime, EndDateTime;
    TextView title_tv, contents_tv, nickname_tv, StartDateTime_tv, EndDateTime_tv, location_tv;
    EditText comment_et;
    FirebaseFirestore db;
    Intent intent;
    ImageView send_iv, img_iv;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_screen);

        title_tv = findViewById(R.id.title_tv);
        contents_tv = findViewById(R.id.contents_tv);
        nickname_tv = findViewById(R.id.nickname_tv);
        StartDateTime_tv = findViewById(R.id.startDateTime_tv);
        EndDateTime_tv = findViewById(R.id.endDateTime_tv);
        location_tv = findViewById(R.id.location_tv);
        send_iv = findViewById(R.id.send_iv);
        comment_et = findViewById(R.id.comment_et);
        img_iv = findViewById(R.id.img_iv);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true); // 툴바 왼쪽에, 뒤로가기 버튼 추가.

        intent = getIntent();

        post_id = intent.getStringExtra("post_id"); // MainPage에서 넘겨준 마커의 post_id를 가져오기.

        db = FirebaseFirestore.getInstance();

        db.collection("posts").document(post_id).get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if(documentSnapshot.exists()){
                            user_id = documentSnapshot.getString("UserID");
                            Title = documentSnapshot.getString("Title");
                            Content = documentSnapshot.getString("Content");
                            StartDate = documentSnapshot.getString("StartDate");
                            StartTime = documentSnapshot.getString("StartTime");
                            EndDate = documentSnapshot.getString("EndDate");
                            EndTime = documentSnapshot.getString("EndTime");
                            Location = documentSnapshot.getString("Location");
                            img = documentSnapshot.getString("ImageURL");

                            // 게시글 정보 불러오고, user_id 바탕으로 nickname 가져오기.
                            db.collection("user").document(user_id).get()
                                    .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                        @Override
                                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                                            nickname = documentSnapshot.getString("nickname");
                                            StartDateTime = StartDate + "  " +StartTime;
                                            EndDateTime = EndDate + "  " + EndTime;

                                            // nickname 가져 왔으면, 화면에 반영
                                            nickname_tv.setText(nickname);
                                            title_tv.setText(Title);
                                            location_tv.setText(Location);
                                            StartDateTime_tv.setText(StartDateTime);
                                            EndDateTime_tv.setText(EndDateTime);


                                            Picasso.get()
                                                    .load(img)
                                                    .into(img_iv);
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Toast.makeText(Screen.this, "유저 데이터 불러오기가 실패하였습니다.", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(Screen.this, "게시글 데이터 불러오기가 실패하였습니다.", Toast.LENGTH_SHORT).show();
                    }
                });
        send_iv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(comment_et.getText().length() != 0) { // 댓글이 비어있지 않다면, db에 삽입
                    String user_id = FirebaseAuth.getInstance().getUid();

                    db.collection("user").document(user_id).get()
                            .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                @Override
                                public void onSuccess(DocumentSnapshot documentSnapshot) {
                                    if(documentSnapshot.exists()){
                                        String nickname = documentSnapshot.getString("nickname"); // 닉네임을 가져온 후, 댓글을 db에 insert 할 것임.
                                        String comment = comment_et.getText().toString();


                                        Map<String, Object> comments = new HashMap<>();
                                        comments.put("userID", user_id);
                                        comments.put("postID", post_id);
                                        comments.put("comment", comment);
                                        comments.put("nickname", nickname);

                                        db.collection("comments")
                                                .add(comments)
                                                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                                    @Override
                                                    public void onSuccess(DocumentReference documentReference) {
                                                        Intent intent = new Intent(Screen.this, Screen.class);
                                                        intent.putExtra("post_id", post_id);
                                                        startActivity(intent);
                                                        finish();
                                                    }
                                                })
                                                .addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        Toast.makeText(Screen.this, "댓글 작성이 실패하였습니다.", Toast.LENGTH_SHORT).show();
                                                    }
                                                });
                                    }
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(Screen.this, "유저 데이터 불러오기가 실패하였습니다.", Toast.LENGTH_SHORT).show();
                                }
                            });
                } else {
                    Toast.makeText(Screen.this, "댓글은 비어있을 수 없습니다.", Toast.LENGTH_SHORT).show();
                }
            }
        });
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

