package com.example.chapter03.util;

import android.content.Context;

public class Utils {

    // 根据手机像素从dp转成px

    /**
     * px = dp × (dpi / 160)
     * <p>
     * dp = px / (dpi / 160)
     * <p>
     * dpi^2 = width^2 + height^2
     */
    public static int dip2px(Context context, Float dpValue) {
        // 获取手机像素密度(1个dp对应几个px)
        float scale = context.getResources().getDisplayMetrics().density;
        // 四舍五入
        return (int) (dpValue * scale + 0.5f);
    }
}
