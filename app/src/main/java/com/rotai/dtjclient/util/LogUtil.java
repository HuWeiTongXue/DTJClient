package com.rotai.dtjclient.util;

import android.os.Bundle;
import android.os.Message;
import android.os.RemoteException;

import com.rotai.dtjclient.base.Application;

public class LogUtil {

    public static int d(String tag, String msg) {
        send(msg);

        return android.util.Log.d(tag, msg);
    }

    public static int e(String tag, String msg, Throwable e) {
        send(msg);
        for (StackTraceElement line : e.getStackTrace())
            send(line.toString());
        return android.util.Log.e(tag, msg, e);
    }

    public static int e(String tag, String msg) {
        return android.util.Log.e(tag, msg, null);
    }

    public static void send(String msg) {
        if (Application.serviceMessenger != null) {
            try {
                Message message = Message.obtain();
                Bundle data = new Bundle();
                data.putString("op", "log");
                data.putString("log", msg);
                message.setData(data);
                Application.serviceMessenger.send(message);
            } catch (RemoteException ignore) { }
        }
    }

    public static String bundleToStr(Bundle data) {
        StringBuilder sb = new StringBuilder();
        for (String key : data.keySet())
            sb.append(key).append(": ").append(data.get(key)).append(", ");
        if (sb.length() == 0)
            sb.append("empty");
        else {
            sb.insert(0, "{ ");
            sb.delete(sb.length() - 2, sb.length());
            sb.append(" }");
        }
        return sb.toString();
    }
}