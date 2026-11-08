package com.yangcheng.activityapplication;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ActResponseActivity extends AppCompatActivity {

    private static final String mResponse = "我还没睡，我爸妈不在家。";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_act_respose);

        // 从上一个ActRequestActivity获取到数据的包裹
        TextView tv_request = findViewById(R.id.tv_request);
        Bundle bundle = getIntent().getExtras();
        String requestTime = bundle.getString("request_time");
        String requestData = bundle.getString("request_data");
        String desc = String.format("收到的请求消息：\n请求时间为%s\n请求内容为%s", requestTime, requestData);
        tv_request.setText(desc);

        findViewById(R.id.button_response).setOnClickListener(v -> {
            Intent intent = new Intent();
            Bundle bd = new Bundle();
            String responseTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
            bd.putString("response_time", responseTime);
            bd.putString("response_data", mResponse);
            // 携带意图返回上一个页面，返回成功 <11>
            setResult(RESULT_OK, intent.putExtras(bd));
            // 结束当前活动页面
            finish();
        });

        TextView tv_response = findViewById(R.id.tv_response);
        tv_response.setText("待返回的消息: "+mResponse );
    }
}