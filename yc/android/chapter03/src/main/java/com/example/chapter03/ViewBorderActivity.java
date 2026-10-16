package com.example.chapter03;

import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.chapter03.util.Utils;

public class ViewBorderActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_border);
        TextView tv = findViewById(R.id.tv_java);
        // ViewGroup内的静态内部类
        ViewGroup.LayoutParams layoutParams = tv.getLayoutParams();
        layoutParams.width = Utils.dip2px(this, 300);
        tv.setLayoutParams(layoutParams);
    }
}