package com.example.mobileprogrammingproject;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
    FirebaseAuth auth;
    Intent intent;
    ImageView send_iv, img_iv;
    ProgressBar progressBar;

    private ImageView starImageView;
    private boolean isFavorite = false;
    private RecyclerView recyclerView;
    private CommentAdapter commentAdapter;
    private List<Comment> commentList;

    public static class Comment {
        private String commenterName;
        private String commentText;

        public String getCommenterName() {
            return commenterName;
        }

        public void setCommenterName(String commenterName) {
            this.commenterName = commenterName;
        }

        public String getCommentText() {
            return commentText;
        }

        public void setCommentText(String commentText) {
            this.commentText = commentText;
        }
    }
    public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.CommentViewHolder> {
        private List<Comment> commentList;

        public CommentAdapter(List<Comment> commentList) {
            this.commentList = commentList;
        }

        @NonNull
        @Override
        public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.comment_view, parent, false);
            return new CommentViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull CommentViewHolder holder, int position) {
            Comment comment = commentList.get(position);
            holder.commenterNameTextView.setText(comment.getCommenterName());
            holder.commentTextView.setText(comment.getCommentText());
        }

        @Override
        public int getItemCount() {
            return commentList.size();
        }

        public class CommentViewHolder extends RecyclerView.ViewHolder {
            public TextView commenterNameTextView;
            public TextView commentTextView;

            public CommentViewHolder(@NonNull View itemView) {
                super(itemView);
                commenterNameTextView = itemView.findViewById(R.id.txtUser_id);
                commentTextView = itemView.findViewById(R.id.txtUser_content);
            }
        }
    }
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
        progressBar = findViewById(R.id.progress_view);
        starImageView = findViewById(R.id.star);

        progressBar.setVisibility(View.VISIBLE); // 로딩시 progressbar VISIBLE 으로 설정.
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true); // 툴바 왼쪽에, 뒤로가기 버튼 추가.

        intent = getIntent();
        post_id = intent.getStringExtra("post_id"); // MainPage에서 넘겨준 마커의 post_id를 가져오기.

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        checkFavoriteStatus();

        recyclerView = findViewById(R.id.recyclerViewComments);
        commentList = new ArrayList<>();
        commentAdapter = new CommentAdapter(commentList);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(commentAdapter);

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
                                            progressBar.setVisibility(View.GONE);
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
                                        comments.put("UserID", user_id);
                                        comments.put("PostID", post_id);
                                        comments.put("comment", comment);
                                        comments.put("nickname", nickname);

                                        db.collection("comments")
                                                .add(comments)
                                                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                                    @Override
                                                    public void onSuccess(DocumentReference documentReference) {

                                                        Comment newComment = new Comment();
                                                        newComment.setCommenterName(nickname);
                                                        newComment.setCommentText(comment);

                                                        commentList.add(newComment);
                                                        commentAdapter.notifyDataSetChanged();
                                                        comment_et.setText("");

                                                        Toast.makeText(Screen.this, "댓글 작성을 완료했습니다.", Toast.LENGTH_SHORT).show();
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

        starImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onStarClick(v);
            }
        });

    }

    public void onStarClick(View view) {
        // 즐겨찾기 상태 토글
        isFavorite = !isFavorite;

        // UI 업데이트
        updateFavoriteUI();
    }
    private void updateFavoriteUI() {
        // UI를 업데이트하는 코드
        if (isFavorite) {
            starImageView.setImageResource(R.drawable.star);
            // 파이어베이스에 즐겨찾기 정보 추가
            addToFavorites();
        } else {
            starImageView.setImageResource(R.drawable.star_blank);
            // 파이어베이스에서 즐겨찾기 정보 제거
            removeFromFavorites();
        }
    }
    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
    private void addToFavorites() {

        if (user_id != null) {
            // 현재 사용자가 로그인되어 있을 때만 실행
            String PostID = post_id; // 게시글의 고유 아이디

            // "favorites" 컬렉션에 즐겨찾기 정보 추가
            CollectionReference favoritesCollection = db.collection("favorites");
            DocumentReference documentReference = favoritesCollection.document(user_id + "_" + post_id);

            // 즐겨찾기 정보 추가
            documentReference.set(new Favorite(post_id, user_id))
                    .addOnSuccessListener(aVoid -> showToast("즐겨찾기에 추가됐습니다"))
                    .addOnFailureListener(e -> showToast("즐겨찾기 추가 실패: " + e.getMessage()));
        }
    }
    private void removeFromFavorites() {
        String uid = FirebaseAuth.getInstance().getUid();

        if (uid != null) {
            // 현재 사용자가 로그인되어 있을 때만 실행
            String PostID = post_id; // 게시글의 고유 아이디

            // "favorites" 컬렉션에서 즐겨찾기 정보 삭제
            CollectionReference favoritesCollection = db.collection("favorites");
            DocumentReference documentReference = favoritesCollection.document(uid + "_" + post_id);

            // 즐겨찾기 정보 삭제
            documentReference.delete()
                    .addOnSuccessListener(aVoid -> showToast("즐겨찾기가 해제되었습니다"))
                    .addOnFailureListener(e -> showToast("즐겨찾기 해제 실패: " + e.getMessage()));
        }
    }
    public static class Favorite {
        private String PostID;
        private String UserID;

        public Favorite() {
            // 기본 생성자가 필요합니다.
        }

        public Favorite(String PostID, String UserID) {
            this.PostID = PostID;
            this.UserID = UserID;
        }

        public String getPostID() {
            return PostID;
        }

        public String getUserID() {
            return UserID;
        }
    }
    public void onBackPressed() {
        super.onBackPressed();
        // 여기에 원하는 동작을 추가하세요
    }

    private void checkFavoriteStatus() {
        // "favorites" 컬렉션에서 해당 게시글이 존재하는지 확인
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid != null) {
            DocumentReference favoriteDocRef = db.collection("favorites").document(uid + "_" + post_id);
            favoriteDocRef.get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        // 해당 게시글이 즐겨찾기에 추가되어 있음
                        isFavorite = true;
                        updateFavoriteUI();
                    }
                }
            });
        }
    }



}

