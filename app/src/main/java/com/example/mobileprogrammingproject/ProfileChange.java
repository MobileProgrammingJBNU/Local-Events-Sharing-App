package com.example.mobileprogrammingproject;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

public class ProfileChange extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_change);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true); // 툴바 왼쪽에, 뒤로가기 버튼 추가.
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
            Toast.makeText(this, "click", Toast.LENGTH_SHORT).show();
        }
        return super.onOptionsItemSelected(item);
    }
}