package com.yuan.packageutils;

import android.graphics.drawable.Drawable;

/**
 * Created by Yuan on 10/31/15.
 */
public class AppInfo {

    private int versionCode = 0;  //版本号

    private String appName = "";// 程序名称

    private String packageName = "";    //包名称

    private Drawable appIcon = null;//图标

    private String lastInstall;//最后一次安装时间

    private String installPath;//安装路径

    private boolean isSystem;

    private String versionName;

    public int getVersionCode() {
        return versionCode;
    }

    public void setVersionCode(int versionCode) {
        this.versionCode = versionCode;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public Drawable getAppIcon() {
        return appIcon;
    }

    public void setAppIcon(Drawable appIcon) {
        this.appIcon = appIcon;
    }

    public String getLastInstall() {
        return lastInstall;
    }

    public void setLastInstall(String lastInstall) {
        this.lastInstall = lastInstall;
    }

    public String getInstallPath() {
        return installPath;
    }

    public void setInstallPath(String installPath) {
        this.installPath = installPath;
    }

    public boolean isSystem() {
        return isSystem;
    }

    public void setSystem(boolean system) {
        isSystem = system;
    }

    public String getVersionName() {
        return versionName;
    }

    public void setVersionName(String versionName) {
        this.versionName = versionName;
    }
}
