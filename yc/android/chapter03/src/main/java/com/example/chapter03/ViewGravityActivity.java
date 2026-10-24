package com.example.chapter03;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

public class ViewGravityActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_gravity);

        // layout_gravity : 当前视图相对于上级视图的对齐方式 会受orientation影响
        // gravity : 下级视图相对于上级视图的对齐方式
        // 左上角对齐 : left|top
    }
}