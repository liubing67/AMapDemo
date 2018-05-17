package com.abing.amap.utils.updatemanager;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.util.Xml;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.abing.amap.BuildConfig;
import com.abing.amap.R;
import com.abing.amap.utils.FileUtil;
import com.abing.amap.utils.RequestPermissionsUtil;
import com.abing.amap.utils.ToastUtil;
import com.abing.amap.utils.UpdateXmlResolve;

import org.dom4j.DocumentException;
import org.dom4j.io.SAXReader;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xmlpull.v1.XmlPullParser;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.Iterator;

/**
 * App更新工具包
 * Created by Administrator on 2015/12/14.
 */
public class UpdateManager {
    private static final int DOWN_NOSDCARD = 0;//没有挂载SD卡，无法下载文件
    private static final int DOWN_UPDATE = 1;//下载时更新进度
    private static final int DOWN_OVER = 2; //下载完成时
    private final int GET_UNDATAINFO_ERROR = 3; //请求xml和解析xml抛出异常时
    private final int DOWN_ERROR = 4; //下载apk失败时
    private final int UPDATA_NONEED = 5; //版本号相同时
    private final int UPDATA_CLIENT = 6; //服务器版本号大于当前版本号
    private final int UPDATA_XIAOYU = 7; //服务器版本号小于当前版本号


    public static boolean isJump = false; //判断是否跳入打开权限页面

    private Context mContext;
    // 进度值
    private int progress;
    // apk保存完整路径
    private String apkFilePath = "";

    private String curVersionName = "";
    private int curVersionCode;
    private Update updateinfo;

    private boolean isAutoShowMsg;
    private PopupWindow popupWindow;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case DOWN_UPDATE: //下载时更新进度
                    break;
                case DOWN_OVER:
                    break;
                case DOWN_NOSDCARD://没有挂载SD卡，无法下载文件
                    Toast.makeText(mContext, "无法安装文件，请检查SD卡是否挂载", Toast.LENGTH_SHORT).show();
                    break;
                case GET_UNDATAINFO_ERROR:
                    //服务器超时
                    Toast.makeText(mContext, "获取版本更新信息失败", Toast.LENGTH_SHORT).show();
                    break;
                case DOWN_ERROR:
                    //下载apk失败
                    Toast.makeText(mContext, "下载新版本失败", Toast.LENGTH_SHORT).show();
                    break;
                case UPDATA_NONEED:
                    if (!isAutoShowMsg) {
                        Toast.makeText(mContext, "已是最新版本",
                                Toast.LENGTH_SHORT).show();
                    }

                    break;
                case UPDATA_CLIENT:
                    showNoticeDialog();
                    break;
                case UPDATA_XIAOYU:
                    if (!isAutoShowMsg) {
                        Toast.makeText(mContext, "服务器版本号小于当前版本号", Toast.LENGTH_SHORT).show();
                    }
                    break;
            }
        }
    };


    /**
     * 检查App更新
     *
     * @param context
     * @param isAutoShowMsg 是否开始进入首页自动检测  在首页自动检测 true   手动检测 false
     */
    public void checkAppUpdate(Context context, final boolean isAutoShowMsg, final String updateurl) {
        this.mContext = context;
        this.isAutoShowMsg = isAutoShowMsg;
        getCurrentVersion();
        new Thread(new Runnable() {
            @Override
            public void run() {
                updateVersion(updateurl);
            }
        }).start();
    }

    /**
     * 获取当前客户端版本信息
     */
    private void getCurrentVersion() {
        try {
            PackageInfo info = mContext.getPackageManager().getPackageInfo(
                    mContext.getPackageName(), 0);
            curVersionName = info.versionName;
            curVersionCode = info.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * 显示版本更新通知对话框
     */
    private void showNoticeDialog() {


        View view = LayoutInflater.from(mContext).inflate(R.layout.update_dialog, null);
        popupWindow = new PopupWindow(view, LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.FILL_PARENT);
        popupWindow.setFocusable(false);
        popupWindow.setOutsideTouchable(true);
        popupWindow.setBackgroundDrawable(new BitmapDrawable());
        popupWindow.showAtLocation(view, Gravity.CENTER, 0, 0);
        TextView textVersion = (TextView) view.findViewById(R.id.text_version);
        TextView textUpdateMessage = (TextView) view.findViewById(R.id.text_updateMessage);
        TextView textCancle = (TextView) view.findViewById(R.id.text_cancle);
        TextView textUpdate = (TextView) view.findViewById(R.id.text_update);
        View view_Split = (View) view.findViewById(R.id.view_Split);

        textUpdateMessage.setText(updateinfo.getUpdateLog());
        textVersion.setText("[" + updateinfo.getVersionName() + "]  新版上线");
        int forceUpdate=Integer.parseInt(updateinfo.getForceUpdate());
        if (forceUpdate != 0) {
            view_Split.setVisibility(View.GONE);
            textCancle.setVisibility(View.GONE);

        }
        textUpdate.setOnClickListener(new View.OnClickListener() {//去更新
            @Override
            public void onClick(View v) {
                popupWindow.dismiss();

                apkFilePath= FileUtil.getSDPath()+"/aaamap.apk";
                if (RequestPermissionsUtil.requestPer((Activity) mContext, Manifest.permission.READ_EXTERNAL_STORAGE, 1)) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        boolean b = mContext.getPackageManager().canRequestPackageInstalls();
                        if (b) {
                            installApk(apkFilePath);
                        } else {
                            //请求安装未知应用来源的权限
                            if (RequestPermissionsUtil.requestPer((Activity) mContext, Manifest.permission.REQUEST_INSTALL_PACKAGES, 2)) {
                                installApk(apkFilePath);
                            } else {
                                isJump = true;//
                                ToastUtil.show(mContext, "请允许安装应用");
                                Intent intent = new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
                                        Uri.parse("package:" + ((Activity) mContext).getPackageName()));
                                ((Activity) mContext).startActivity(intent);
                            }
                        }
                    } else {
                        installApk(apkFilePath);
                    }
                } else {
                    isJump = true;//
                    ToastUtil.show(mContext, "请打开访问内存卡权限");
                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                            Uri.parse("package:" + ((Activity) mContext).getPackageName()));
                    ((Activity) mContext).startActivity(intent);
                }

            }
        });
        textCancle.setOnClickListener(new View.OnClickListener() {//点击取消
            @Override
            public void onClick(View v) {
                popupWindow.dismiss();
            }
        });
    }

    public void dismissPopupWindow() {
        if (popupWindow != null) {
            popupWindow.dismiss();
        }
    }
    /**
     * 安装apk
     */
    private void installApk(String apkFilePath) {

        // 判断文件路径是否存在
        if (apkFilePath == null || apkFilePath == "") {
            mHandler.sendEmptyMessage(DOWN_NOSDCARD);
            return;
        }
        File apkfile = new File(apkFilePath);
        if (!apkfile.exists()) {
            ToastUtil.show(mContext,"安装包不存在！");
            return;
        }
        Intent intent = new Intent(Intent.ACTION_VIEW);
        //判断是否是AndroidN以及更高的版本
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            Uri contentUri = FileProvider.getUriForFile(mContext, BuildConfig.APPLICATION_ID+".provider", apkfile);
            intent.setDataAndType(contentUri, "application/vnd.android.package-archive");
        } else {
            intent.setDataAndType(Uri.fromFile(apkfile), "application/vnd.android.package-archive");
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        ((Activity) mContext).startActivityForResult(intent, 0);
////        // 会返回结果,回调方法onActivityResult
//        android.os.Process.killProcess(android.os.Process.myPid());
    }

    /**
     * 获取服务器的最新版本信息
     */
    private void updateVersion(String updateurl) {
        try {
            UpdateXmlResolve updateXmlResolve=new UpdateXmlResolve();
            updateinfo=updateXmlResolve.findAll(updateurl);
            Log.d("UpdateManager", "forceUpdate-----" + updateinfo.getForceUpdate());
            Log.d("UpdateManager", "updateLog-----" + updateinfo.getUpdateLog());
            Log.d("UpdateManager", "versionName-----" + updateinfo.getVersionName());
            Log.d("UpdateManager", "versionCode-----" + updateinfo.getVersionCode());
            int versionCode=Integer.parseInt(updateinfo.getVersionCode());
           if (versionCode == curVersionCode) {
//                Log.i(TAG, "版本号相同");
                Message msg = new Message();
                msg.what = UPDATA_NONEED;
                mHandler.sendMessage(msg);
            } else if (versionCode > curVersionCode) {
//                Log.i(TAG, "版本号不相同 ");
                Message msg = new Message();
                msg.what = UPDATA_CLIENT;
                mHandler.sendMessage(msg);
            } else if (versionCode < curVersionCode) {
                Message msg = new Message();
                msg.what = UPDATA_XIAOYU;
                mHandler.sendMessage(msg);
            }
        } catch (Exception e) {
            Message msg = new Message();
            msg.what = GET_UNDATAINFO_ERROR;
            mHandler.sendMessage(msg);
            e.printStackTrace();
        }
    }

}
