package com.cpigeon.app.broadcastreceiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import com.cpigeon.app.networkstatus.NetChangeObserver;
import com.cpigeon.app.utils.NetUtils;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by Administrator on 2017/4/6.
 */

public class NetStateReceiver extends BroadcastReceiver {
    public static final String CUSTOM_ANDROID_NET_CHANGE_ACTION = "com.andysong.api.netstatus.CONNECTIVITY_CHANGE";
    private final static String ANDROID_NET_CHANGE_ACTION = "android.net.conn.CONNECTIVITY_CHANGE";
    private final static String TAG = NetStateReceiver.class.getSimpleName();
    private static boolean isNetAvailable = false;
    private static NetUtils.NetType mNetType;
    private static List<WeakReference<NetChangeObserver>> mNetChangeObservers;
    private static BroadcastReceiver mBroadcastReceiver;

    private static BroadcastReceiver getReceiver() {
        if (null == mBroadcastReceiver) {
            synchronized (NetStateReceiver.class) {
                if (null == mBroadcastReceiver) {
                    mBroadcastReceiver = new NetStateReceiver();
                    initNetChangeObservers();
                }
            }
        }
        return mBroadcastReceiver;
    }

    private static void initNetChangeObservers() {
        if (mNetChangeObservers == null)
            mNetChangeObservers = new ArrayList<>();
        //清理为空的引用
        Iterator<WeakReference<NetChangeObserver>> iterator = mNetChangeObservers.iterator();
        while (iterator.hasNext()) {
            WeakReference<NetChangeObserver> ref = iterator.next();
            if (ref.get() == null)
                iterator.remove();
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        mBroadcastReceiver = NetStateReceiver.this;
        if (intent.getAction().equalsIgnoreCase(ANDROID_NET_CHANGE_ACTION) || intent.getAction().equalsIgnoreCase(CUSTOM_ANDROID_NET_CHANGE_ACTION)) {
            if (!NetUtils.isNetworkAvailable(context)) {
                Log.e(this.getClass().getName(), "<--- network disconnected --->");
                isNetAvailable = false;
            } else {
                Log.e(this.getClass().getName(), "<--- network connected --->");
                isNetAvailable = true;
                mNetType = NetUtils.getAPNType(context);
            }
            notifyObserver();
        }
    }

    /**
     * 注册
     *
     * @param mContext
     */
    public static void registerNetworkStateReceiver(Context mContext) {
        IntentFilter filter = new IntentFilter();
        filter.addAction(CUSTOM_ANDROID_NET_CHANGE_ACTION);
        filter.addAction(ANDROID_NET_CHANGE_ACTION);
        mContext.getApplicationContext().registerReceiver(getReceiver(), filter);
    }

    /**
     * 清除
     *
     * @param mContext
     */
    public static void checkNetworkState(Context mContext) {
        Intent intent = new Intent();
        intent.setAction(CUSTOM_ANDROID_NET_CHANGE_ACTION);
        mContext.sendBroadcast(intent);
    }

    /**
     * 反注册
     *
     * @param mContext
     */
    public static void unRegisterNetworkStateReceiver(Context mContext) {
        if (mBroadcastReceiver != null) {
            try {
                mContext.getApplicationContext().unregisterReceiver(mBroadcastReceiver);
            } catch (Exception e) {

            }
        }

    }

    public static boolean isNetworkAvailable() {
        return isNetAvailable;
    }

    public static NetUtils.NetType getAPNType() {
        return mNetType;
    }

    private void notifyObserver() {
        if (!mNetChangeObservers.isEmpty()) {
            int size = mNetChangeObservers.size();
            for (int i = 0; i < size; i++) {
                NetChangeObserver observer = mNetChangeObservers.get(i).get();
                if (observer != null) {
                    if (isNetworkAvailable()) {
                        observer.onNetConnected(mNetType);
                    } else {
                        observer.onNetDisConnect();
                    }
                }
            }
        }
    }

    /**
     * 添加网络监听
     *
     * @param observer
     */
    public static void registerObserver(NetChangeObserver observer) {
        initNetChangeObservers();
        mNetChangeObservers.add(new WeakReference<NetChangeObserver>(observer));
    }

//    /**
//     * 移除网络监听
//     *
//     * @param observer
//     */
//    public static void removeRegisterObserver(NetChangeObserver observer) {
//        if (mNetChangeObservers != null) {
//            if (mNetChangeObservers.contains(observer)) {
//                mNetChangeObservers.remove(observer);
//            }
//        }
//    }
}
