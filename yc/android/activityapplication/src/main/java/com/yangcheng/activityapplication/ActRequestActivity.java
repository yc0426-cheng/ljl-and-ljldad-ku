package com.yangcheng.activityapplication;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ActRequestActivity extends AppCompatActivity {

    private static final String mRequest = "你睡了吗，来我家睡吧";

    private ActivityResultLauncher<Intent> register;

    private TextView tv_response;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_act_request);

        TextView tv_request = findViewById(R.id.tv_request);
        tv_request.setText("待发送的消息为" + mRequest);

        tv_response = findViewById(R.id.tv_response);

        Button b = findViewById(R.id.button_request);
        b.setOnClickListener(v -> {
            Intent intent = new Intent(ActRequestActivity.this, ActResponseActivity.class);
            Bundle bundle = new Bundle();
            String requestTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
            bundle.putString("request_time", requestTime);
            bundle.putString("request_data", mRequest);
            intent.putExtras(bundle);

            // startActivityForResult() 已过期;
            register.launch(intent);
        });

        // actResponseActivity返回的数据 <11>
        register = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), o -> {
            if (o != null) {
                Intent intent = o.getData();
                if (intent != null && o.getResultCode() == RESULT_OK) {
                    Bundle bundle = getIntent().getExtras();
                    String responseTime = bundle.getString("response_time");
                    String responseData = bundle.getString("response_data");
                    String desc = String.format("收到的返回消息：\n应答时间为%s\n应答内容为%s", responseTime, responseData);
                    tv_response.setText(desc);
                }
            }
        });
    }
}