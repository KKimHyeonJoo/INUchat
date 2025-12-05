package com.example.easychatgpt;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class SelectActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.select_lan);
        getSupportActionBar().hide();

        Button kor_btn=findViewById(R.id.kor_btn);

        kor_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getBaseContext(),KorActivity.class);
                startActivity(intent);
                finish();
            }
        });


        Button eng_btn=findViewById(R.id.eng_btn);

        eng_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getBaseContext(),EngActivity.class);
                startActivity(intent);
                finish();
            }
        });

    }

}