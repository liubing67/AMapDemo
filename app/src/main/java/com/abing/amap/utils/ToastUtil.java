package com.abing.amap.utils;

import android.content.Context;
import android.widget.Toast;

/**
 * 项目名称：AgentManage
 * 类描述：
 * 创建人：liubing
 * 创建时间：2016/12/28 14:28
 * 修改人：Administrator
 * 修改时间：2016/12/28 14:28
 * 修改备注：
 */
public class ToastUtil {

    private static Toast toast;
    private static Context contex;
    public static void init(Context context) {
        if (null == context) {
            throw new IllegalArgumentException("context cannot be null.");
        }
        contex = context.getApplicationContext();
    }
    public static void show(Context context, String message) {
        if (null == toast) {
            toast = Toast.makeText(context, message, Toast.LENGTH_SHORT);
            // toast.setGravity(Gravity.CENTER, 0, 0);
        } else {
            toast.setText(message);
        }
        toast.show();
    }
    public static void show(Context context, int message) {
        if (null == toast) {
            toast = Toast.makeText(context, message, Toast.LENGTH_SHORT);
            // toast.setGravity(Gravity.CENTER, 0, 0);
        } else {
            toast.setText(message);
        }
        toast.show();
    }
    public static void show(String message) {
        if (null == toast) {
            toast = Toast.makeText(contex, message, Toast.LENGTH_SHORT);
            // toast.setGravity(Gravity.CENTER, 0, 0);
        } else {
            toast.setText(message);
        }
        toast.show();
    }
    public static void show(int message) {
        if (null == toast) {
            toast = Toast.makeText(contex, message, Toast.LENGTH_SHORT);
            // toast.setGravity(Gravity.CENTER, 0, 0);
        } else {
            toast.setText(message);
        }
        toast.show();
    }
}
