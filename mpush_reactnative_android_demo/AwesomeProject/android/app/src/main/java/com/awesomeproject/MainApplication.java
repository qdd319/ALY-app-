package com.awesomeproject;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;

import androidx.core.content.ContextCompat;

import com.alibaba.sdk.android.push.CloudPushService;
import com.alibaba.sdk.android.push.CommonCallback;
import com.alibaba.sdk.android.push.huawei.HuaWeiRegister;
import com.alibaba.sdk.android.push.noonesdk.PushServiceFactory;
import com.alibaba.sdk.android.push.register.GcmRegister;
import com.alibaba.sdk.android.push.register.MeizuRegister;
import com.alibaba.sdk.android.push.register.MiPushRegister;
import com.alibaba.sdk.android.push.register.OppoRegister;
import com.alibaba.sdk.android.push.register.VivoRegister;
import com.awesomeproject.push.PushModule;
import com.awesomeproject.push.PushPackage;
import com.facebook.react.ReactApplication;
import com.facebook.react.ReactNativeHost;
import com.facebook.react.ReactPackage;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.shell.MainReactPackage;
import com.facebook.soloader.SoLoader;

import java.io.File;
import java.util.Arrays;
import java.util.List;

public class MainApplication extends Application implements ReactApplication {

    private final ReactNativeHost mReactNativeHost = new ReactNativeHost(this) {
        @Override
        public boolean getUseDeveloperSupport() {
            return BuildConfig.DEBUG;
        }

        @Override
        protected List<ReactPackage> getPackages() {
            return Arrays.<ReactPackage>asList(
                    new MainReactPackage(),
                    new PushPackage()
            );
        }
    };

    @Override
    public ReactNativeHost getReactNativeHost() {
        return mReactNativeHost;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        SoLoader.init(this, /* native exopackage */ false);

        PushServiceFactory.init(this);


        initNotificationChannel();
        initCloudChannel();
    }

    /**
     * ?????????????????????
     */
    private void initNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            // ???????????????id???
            String id = "1";
            // ?????????????????????????????????????????????
            CharSequence name = "notification channel";
            // ?????????????????????????????????????????????
            String description = "notification description";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel mChannel = new NotificationChannel(id, name, importance);
            // ??????????????????????????????
            mChannel.setDescription(description);
            // ???????????????????????????????????????Android????????????????????????
            mChannel.enableLights(true);
            mChannel.setLightColor(Color.RED);
            // ???????????????????????????????????????Android????????????????????????
            mChannel.enableVibration(true);
            mChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
            // ?????????notificationmanager???????????????????????????
            mNotificationManager.createNotificationChannel(mChannel);
        }
    }

    private boolean pushInit;

    /**
     * ????????????
     */
    public void initCloudChannel() {
        File is_privacy = new File(ContextCompat.getDataDir(this).getAbsolutePath(), ContVar.P_FILE);
        if (!is_privacy.exists()) return;
        if (pushInit) return;
        pushInit = true;

        if (BuildConfig.DEBUG) {
            //????????????Debug??????????????????????????????
            PushServiceFactory.getCloudPushService().setLogLevel(CloudPushService.LOG_DEBUG);
        }
        PushServiceFactory.getCloudPushService().register(this.getApplicationContext(), new CommonCallback() {
            @Override
            public void onSuccess(String s) {
                pushInit = true;
                WritableMap params = Arguments.createMap();
                params.putBoolean("success", true);
                PushModule.sendEvent("onInit", params);
                initCS();
            }

            @Override
            public void onFailed(String s, String s1) {
                pushInit = false;
                WritableMap params = Arguments.createMap();
                params.putBoolean("success", false);
                params.putString("errorMsg", "errorCode:" + s + ". errorMsg:" + s1);
                PushModule.sendEvent("onInit", params);
            }
        });
    }

    /**
     * ???????????????
     */
    private void initCS() {
        HuaWeiRegister.register(this);
        MiPushRegister.register(this, "XIAOMI_ID", "XIAOMI_KEY"); // ???????????????????????????
        HuaWeiRegister.register(this); // ????????????????????????
        VivoRegister.register(this);
        OppoRegister.register(this, "OPPO_KEY", "OPPO_SECRET");
        MeizuRegister.register(this, "MEIZU_ID", "MEIZU_KEY");

        GcmRegister.register(this, "send_id", "application_id"); // ??????FCM/GCM???????????????
    }

}
