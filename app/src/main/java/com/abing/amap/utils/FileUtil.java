package com.abing.amap.utils;

import android.os.Environment;

import java.io.File;

/**
 * 项目名称：AMapDemo
 * 类描述：
 * 创建人：liubing
 * 创建时间：2018-4-25 15:53
 * 修改人：Administrator
 * 修改时间：2018-4-25 15:53
 * 修改备注：
 */
public class FileUtil {

    public static String getSDPath(){
        File sdDir = null;
        boolean sdCardExist = Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED); //判断sd卡是否存在
        if (sdCardExist) {
            sdDir = Environment.getExternalStorageDirectory();//获取跟目录
        }else {
            return null;
        }
        return sdDir.toString();
    }
}
