package com.abing.amap.utils;

//导入的包

import android.app.Activity;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;

/**
 * 项目名称：LiuTest
 * 类描述： 权限请求工具类
 * 创建人：liubing
 * 创建时间：2016/10/19 9:44
 * 修改人：Administrator
 * 修改时间：2016/10/19 9:44
 * 修改备注：
 * 使用步骤
 * 1.在activity中在PERMISSIONS数组中放入该类中所有用到的权限
 * private String [] PERMISSIONS = new String[]{
 * Manifest.permission.CALL_PHONE,
 * Manifest.permission.READ_PHONE_STATE
 * };
 * 2. 点击拨打电话时调用requestPer方法判断
 * findViewById(R.id.button).setOnClickListener(new View.OnClickListener() {
 *
 * @Override public void onClick(View v) {
 * if (RequstPermissionsUtil.requestPer(MainActivity.this, PERMISSIONS[0],0))
 * {
 * Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:4001851518"));
 * startActivity(intent);
 * }
 * }
 * });
 * 3.在activity中的onRequestPermissionsResult方法中调用RequstPermissionsUtil.onRequestPermissionsResult方法将数据传进去进行回调
 * @Override public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
 * super.onRequestPermissionsResult(requestCode, permissions, grantResults);
 * RequstPermissionsUtil.onRequestPermissionsResult(MainActivity.this,PERMISSIONS[requestCode],requestCode,permissions,grantResults);
 * }
 */
public class RequestPermissionsUtil {

    private static boolean isper;//返回是否获取到权限
    //权限判断

    /**
     * @param activity    当前的activity
     * @param permiss     请求的权限
     * @param requestCode 请求码
     * @return true: already granted
     * false: have not permission
     */
    public static boolean requestPer(Activity activity, String permiss, int requestCode) {
        if (ContextCompat.checkSelfPermission(activity, permiss) != PackageManager.PERMISSION_GRANTED) {
            //如果App的权限申请曾经被用户拒绝过，就需要在这里跟用户做出解释
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity,
                    permiss)) {
            }
            //请求权限
            ActivityCompat.requestPermissions(activity, new String[]{permiss},
                    requestCode);
            Log.e("222222222", "请求权限");
            isper = false;
            return isper;
        } else {
            //请求成功之后
            isper = true;
            return true;
        }
    }

    /**
     * @param activity     当前的activity
     * @param permiss      请求的权限
     * @param requestCode  activity中的onRequestPermissionsResult方法自带的参数
     * @param permissions  activity中的onRequestPermissionsResult方法自带的参数
     * @param grantResults activity中的onRequestPermissionsResult方法自带的参数
     */
    public static void onRequestPermissionsResult(Activity activity, String permiss, int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            requestPer(activity, permiss, requestCode);
        } else {
            Log.e("333333333333333", "没有获取到权限");
            isper = false;
            Toast.makeText(activity, "请打开该权限", Toast.LENGTH_SHORT).show();
        }
        Log.e("333333333333333", "3333333333333");
    }
}
