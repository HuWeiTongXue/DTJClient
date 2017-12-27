package com.rotai.dtjclient.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.rotai.dtjclient.base.BaseActivity;

/**
 * 检查系统状态
 */

public class CheckStatusReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if(BaseActivity.isBackground()==0){
            Intent it= context.getPackageManager().getLaunchIntentForPackage("com.rotai.dtjclient");
            if (it != null) {
                it.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(it);
            }
        }
    }
}
