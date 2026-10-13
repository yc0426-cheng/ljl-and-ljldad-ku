package com.example.chapter03;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class TextColorActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_text_color);
        TextView tv_style = findViewById(R.id.tv_color_style);
        tv_style.setTextColor(Color.GREEN);
        // 十六进制： 前两位是代表是十六进制的数，三四位是透明度，越大越不透明
        // 五六位是红色浓度，七八位是绿色浓度，九十位是蓝色浓度， 越大越绿
        TextView tv_color = findViewById(R.id.tv_color_eight);
        tv_color.setTextColor(0xff00ff00); // 若是透明度选择00，则可以省略，而xml内相反
    }
}