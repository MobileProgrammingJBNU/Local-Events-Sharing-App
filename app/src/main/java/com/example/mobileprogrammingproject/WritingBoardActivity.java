package com.example.mobileprogrammingproject;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;


import org.w3c.dom.Text;

public class WritingBoardActivity extends AppCompatActivity {

    Toolbar toolbar;
    TextView dateText;
    TextView dateText2;
    DatePickerDialog datePickerDialog;
    DatePickerDialog datePickerDialog2;
    double markerLatitude;
    double markerLongitude;
    TimePickerDialog timePickerDialog;
    TimePickerDialog timePickerDialog2;
    int pHour;
    int pMinute;
    TextView timeText;
    TextView timeText2;


    public static class Post {
        private String title;
        private String content;
        private String location;
        private String startdate;
        private String enddate;
        private String starttime;
        private String endtime;

        public Post() {
            // Firebase에서 객체를 가져올 때 필요한 기본 생성자
        }

        public Post(String title, String content, String location, String startdate, String
                enddate, String starttime, String endtime) {
            this.title = title;
            this.content = content;
            this.location = location;
            this.startdate = startdate;
            this.enddate = enddate;
            this.starttime = starttime;
            this.endtime = endtime;
        }

        // Getter 및 Setter 메서드 추가
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.writing_board);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        dateText = findViewById(R.id.textView);
        dateText2 = findViewById(R.id.textView2);
        timeText = findViewById(R.id.textView3);
        timeText2 = findViewById(R.id.textView5);
        Button datePickerBtn = findViewById(R.id.date_picker_btn);
        Button datePickerBtn2 = findViewById(R.id.date_picker_btn2);
        Button registerButton = findViewById(R.id.button2);

        Intent intent = getIntent();
        markerLatitude = intent.getDoubleExtra("markerLatitude", 0.0);
        markerLongitude = intent.getDoubleExtra("markerLongitude", 0.0);
        String locationName = intent.getStringExtra("locationName");

        TextView locationEditText = findViewById(R.id.editTextText5);
        locationEditText.setText(locationName);

        datePickerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Calendar calendar = Calendar.getInstance();
                int pYear = calendar.get(Calendar.YEAR);
                int pMonth = calendar.get(Calendar.MONTH);
                int pDay = calendar.get(Calendar.DAY_OF_MONTH);

                datePickerDialog = new DatePickerDialog(WritingBoardActivity.this,
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                                // 1월은 0부터 시작하기 때문에 +1을 해준다
                                month = month + 1;
                                String date = year + "/" + month + "/" + day;

                                dateText.setText(date);
                                timePickerDialog.show();
                            }
                        }, pYear, pMonth, pDay);
                datePickerDialog.show();

                timePickerDialog = new TimePickerDialog(WritingBoardActivity.this,
                        new TimePickerDialog.OnTimeSetListener() {
                            @Override
                            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                                pHour = hourOfDay;
                                pMinute = minute;
                                String time = pHour + " : " + pMinute;

                                timeText.setText(time);
                            }
                        }, 21, 12, true);
            } //onClick
        });

        datePickerBtn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Calendar calendar = Calendar.getInstance();
                int pYear = calendar.get(Calendar.YEAR);
                int pMonth = calendar.get(Calendar.MONTH);
                int pDay = calendar.get(Calendar.DAY_OF_MONTH);

                datePickerDialog2 = new DatePickerDialog(WritingBoardActivity.this,
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                                // 1월은 0부터 시작하기 때문에 +1을 해준다
                                month = month + 1;
                                String date = year + "/" + month + "/" + day;

                                dateText2.setText(date);
                                timePickerDialog2.show();
                            }
                        }, pYear, pMonth, pDay);

                datePickerDialog2.show();

                timePickerDialog2 = new TimePickerDialog(WritingBoardActivity.this,
                        new TimePickerDialog.OnTimeSetListener() {
                            @Override
                            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                                pHour = hourOfDay;
                                pMinute = minute;
                                String time = pHour + " : " + pMinute;

                                timeText2.setText(time);
                            }
                        }, 21, 12, true);
            } //onClick
        });
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                savePostToFirestore(); // Firestore에 데이터 저장 메서드 호출
            }
        });
    } // onCreate

    private void savePostToFirestore() {
        EditText titleEditText = findViewById(R.id.editTextText);
        EditText contentEditText = findViewById(R.id.editTextText2);
        TextView locationEditText = findViewById(R.id.editTextText5);
        TextView startdateTextView = findViewById(R.id.textView);
        TextView enddateTextView = findViewById(R.id.textView2);
        TextView starttimeTextView = findViewById(R.id.textView3);
        TextView endtimeTextView = findViewById(R.id.textView5);

        String title = titleEditText.getText().toString();
        String content = contentEditText.getText().toString();
        String location = locationEditText.getText().toString();
        String startdate = startdateTextView.getText().toString();
        String enddate = enddateTextView.getText().toString();
        String latitudeString = String.valueOf(markerLatitude);
        String longitudeString = String.valueOf(markerLongitude);
        String starttime = starttimeTextView.getText().toString();
        String endtime = endtimeTextView.getText().toString();

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        Map<String, Object> post = new HashMap<>();
        post.put("Title", title);
        post.put("Content", content);
        post.put("StartDate", startdate);
        post.put("EndDate", enddate);
        post.put("Location", location);
        post.put("Latitude", latitudeString);
        post.put("Longitude", longitudeString);
        post.put("StartTime", starttime);
        post.put("EndTime", endtime);

        db.collection("posts")
                .add(post)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Log.d("TAG", "DocumentSnapshot added with ID: " + documentReference.getId());
                        // 성공적으로 추가된 경우 실행할 코드를 여기에 작성하세요.
                        Toast.makeText(getApplicationContext(), "게시글 작성이 완료되었습니다.", Toast.LENGTH_SHORT).show();
                        String post_id = documentReference.getId(); // 생성된 post의 id
                        db.collection("posts")
                                .document(post_id)
                                .update("id", post_id)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        finish(); // 예를 들어, 현재 액티비티를 종료하는 등의 동작
                                    }
                                });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(Exception e) {
                        Log.w("TAG", "Error adding document", e);
                        // 실패한 경우 실행할 코드를 여기에 작성하세요.
                    }
                });
    }
} //WritingBoardActivity
