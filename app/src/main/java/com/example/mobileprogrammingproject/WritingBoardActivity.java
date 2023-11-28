package com.example.mobileprogrammingproject;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;
import java.util.Calendar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class WritingBoardActivity extends AppCompatActivity {

    Toolbar toolbar;
    TextView dateText;
    TextView dateText2;
    DatePickerDialog datePickerDialog;
    DatePickerDialog datePickerDialog2;
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
        Button datePickerBtn = findViewById(R.id.date_picker_btn);
        Button datePickerBtn2 = findViewById(R.id.date_picker_btn2);
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
                            }
                        }, pYear, pMonth, pDay);
                datePickerDialog.show();
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
                            }
                        }, pYear, pMonth, pDay);
                datePickerDialog2.show();
            } //onClick
        });


    } // onCreate
} //WritingBoardActivity
