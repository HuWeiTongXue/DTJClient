package com.rotai.dtjclient.base;

import android.os.Environment;

/**
 * 常量
 */

public class Constant {

    public static final String SDCARD_PATH= Environment.getExternalStorageDirectory().getAbsolutePath()+"/";
    public static final String adUrl = "http://cdn.pm.rotai.com/uploads/attachment/url/2pgk9gyb8ql8b6rclai/5.mp4";
    public static final String apkUrl = "http://cdn.pm.rotai.com/uploads/attachment/url/2pgkg0p5m8xfrl7o4vc/DTJService-release.apk";

    public static final String fileName_rotai="8.mp4";
    public static final String apkName_service="DTJService-release.apk";


    /**
     * 水波纹效果
     */
    public static final boolean ANTI_ALIAS = true;

    public static final int DEFAULT_SIZE = 150;
    public static final int DEFAULT_START_ANGLE = 270;
    public static final int DEFAULT_SWEEP_ANGLE = 360;

    public static final int DEFAULT_ANIM_TIME = 1000;

    public static final int DEFAULT_MAX_VALUE = 100;
    public static final int DEFAULT_VALUE = 0;

    public static final int DEFAULT_HINT_SIZE = 15;
    public static final int DEFAULT_UNIT_SIZE = 30;
    public static final int DEFAULT_VALUE_SIZE = 15;

    public static final int DEFAULT_ARC_WIDTH = 15;

    public static final int DEFAULT_WAVE_HEIGHT = 40;



}