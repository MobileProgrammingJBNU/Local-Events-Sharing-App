package com.example.mobileprogrammingproject;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.example.mobileprogrammingproject.postlist.PostAdapter;
import com.example.mobileprogrammingproject.postlist.PostPreview;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class ViewPostActivity extends AppCompatActivity {
    Toolbar toolbar;

    private RecyclerView recyclerView;
    private PostAdapter adapter;

    private List<PostPreview> postPreviews;

    FirebaseFirestore db;

    String user_id;
    String Title, PostID;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_post);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true); // 툴바 왼쪽에, 뒤로가기 버튼 추가.

        db = FirebaseFirestore.getInstance();

        user_id = FirebaseAuth.getInstance().getUid();

        //recyclerView 초기화
        recyclerView = findViewById(R.id.myPost_rv);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));



        db.collection("posts").whereEqualTo("UserID", user_id).get() // 사용자가 작성한 post 가져오기

                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        Log.d("OnComplre 안입니다.","");
                        QuerySnapshot querySnapshot = task.getResult();
                        postPreviews = new ArrayList<>();

                        for(QueryDocumentSnapshot doc : querySnapshot){
                            String UserID = doc.getString("UserID");
                            Title = doc.getString("Title");
                            PostID = doc.getString("PostID");
                            getUserNickname(UserID);
                        }
                        adapter = new PostAdapter(postPreviews);
                        recyclerView.setAdapter(adapter);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(ViewPostActivity.this, "user_id와 동일한 post를 가져오는데 실패했습니다.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void getUserNickname(String userId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("user").document(userId).get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                // 여기서 user collection의 nickname을 가져옴
                                String nickname = document.getString("nickname");

                                postPreviews.add(new PostPreview(Title, nickname, PostID));
                                adapter.notifyDataSetChanged();
                                Log.d("OnComplete", "안입니다.");
                            }
                        } else {
                            Log.e("FirestoreData", "get failed with ", task.getException());
                        }
                    }
                });
    }
}