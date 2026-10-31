package com.example.commonlayout;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class LinearLayoutActivity extends AppCompatActivity {

    /**
     * android:orientation="horizontal" : 内部视图水平方向从左向右排列<p>
     * android:orientation="vertical" : 内部视图垂直方向从上向下排列<p>
     * 不指定默认水平
     * <p>
     * layout_wight 指线性布局的下级视图各自拥有的多大比例的宽高(设置位置为linearlayout子布局)<p>
     * layout_width = 0dp layout_wight表示水平方向的宽度比例<p>
     * layout_height = 0dp layout_wight表示垂直方向的高度比例
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_linear_layout);

    }
}