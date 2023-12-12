package com.example.mobileprogrammingproject;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;

import com.example.mobileprogrammingproject.postlist.PostAdapter;
import com.example.mobileprogrammingproject.postlist.PostPreview;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class ViewCommentActivity extends AppCompatActivity {
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
        setContentView(R.layout.activity_view_comment);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true); // 툴바 왼쪽에, 뒤로가기 버튼 추가.
    }
}