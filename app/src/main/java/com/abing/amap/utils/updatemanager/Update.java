package com.abing.amap.utils.updatemanager;

import java.io.Serializable;

/**
 * app更新实体类
 * Created by Administrator on 2015/12/14.
 */
public class Update implements Serializable {

    public final static String UTF8 = "UTF-8";

    private String versionCode;
    private String versionName;
    private String updateLog;
    private String forceUpdate;


    public String getVersionCode() {
        return versionCode;
    }

    public void setVersionCode(String versionCode) {
        this.versionCode = versionCode;
    }

    public String getVersionName() {
        return versionName;
    }

    public void setVersionName(String versionName) {
        this.versionName = versionName;
    }

    public String getUpdateLog() {
        return updateLog;
    }

    public void setUpdateLog(String updateLog) {
        this.updateLog = updateLog;
    }

    public String getForceUpdate() {
        return forceUpdate;
    }

    public void setForceUpdate(String forceUpdate) {
        this.forceUpdate = forceUpdate;
    }
}
