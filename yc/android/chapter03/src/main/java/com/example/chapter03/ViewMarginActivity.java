package com.example.chapter03;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class ViewMarginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_margin);

        Button buttonLeft = findViewById(R.id.left_button);
        buttonLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(ViewMarginActivity.this, ViewLeftActivity.class);
                startActivity(intent);
            }
        });

        Button buttonRight = findViewById(R.id.right_button);
        buttonRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(ViewMarginActivity.this, ViewRightActivity.class);
                startActivity(intent);
            }
        });
    }
}