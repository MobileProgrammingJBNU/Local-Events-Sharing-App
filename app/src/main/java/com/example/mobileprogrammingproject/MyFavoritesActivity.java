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

public class MyFavoritesActivity extends AppCompatActivity {
    Toolbar toolbar;
    private RecyclerView recyclerView;
    private PostAdapter adapter;

    private List<PostPreview> postPreviews;

    FirebaseFirestore db;

    String user_id;
    String Title, postId, nickname;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_favorites);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true); // 툴바 왼쪽에, 뒤로가기 버튼 추가.

        db = FirebaseFirestore.getInstance();

        user_id = FirebaseAuth.getInstance().getUid();

        //recyclerView 초기화
        recyclerView = findViewById(R.id.favorites_rv);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        db.collection("favorites").whereEqualTo("UserID", user_id).get() // 사용자가 작성한 post 가져오기

                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        Log.d("OnComplre 안입니다.","");
                        QuerySnapshot querySnapshot = task.getResult();
                        postPreviews = new ArrayList<>();

                        for(QueryDocumentSnapshot doc : querySnapshot){
                            String userId = doc.getString("UserID");
                            //Title = doc.getString("Title");
                            postId = doc.getString("PostID");
                            getUserNicknameAndTitle(userId, postId); // userNickname 및 title 가져오기
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(MyFavoritesActivity.this, "user_id와 동일한 post를 가져오는데 실패했습니다.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void getUserNicknameAndTitle(String UserID, String PostID) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("user").document(UserID).get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        DocumentSnapshot result = documentSnapshot;
                        if(result.exists()){
                            nickname = result.getString("nickname");
                            getPostTitle(PostID);
                        }
                    }
                });
    }

    private void getPostTitle(String PostID) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("posts").document(PostID).get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        DocumentSnapshot result = documentSnapshot;
                        if(result.exists()){
                            Title = result.getString("Title");

                            postPreviews.add(new PostPreview(Title, nickname, PostID));

                            if(adapter == null){
                                adapter = new PostAdapter(postPreviews);
                                recyclerView.setAdapter(adapter);
                            }
                            else {
                                adapter.notifyDataSetChanged();
                            }
                        }
                    }
                });
    }
}