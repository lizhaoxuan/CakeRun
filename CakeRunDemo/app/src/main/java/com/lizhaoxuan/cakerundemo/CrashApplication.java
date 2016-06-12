package com.lizhaoxuan.cakerundemo;

import android.app.Application;
import android.util.Log;

import com.cakerun.apt.AppInit;
import com.cakerun.apt.AsyncInit;
import com.lizhaoxuan.cakerun.CakeRun;

/**
 * Created by lizhaoxuan on 16/4/28.
 */
public class CrashApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        //CakeRun需要在Application中第一个初始化
        new CakeRun.Build(this, //Application
                "0.1", //App版本号
                new CrashPatch(this))  //Crash后响应
                .setAsyncMonitorTime(1500) // 异步任务在Application结束后持续监控时间 默认1500
                .setMaxRestartNum(5) //Application Crash后，最大重启次数
                .setAlwaysRestartApp(true) //非Application Crash后 是否一直重启
                .setResetCrashLogTime(1000 * 60 * 60 * 24 * 2) //重置Crash日志记录时间（根据App使用频率设置）。默认三天重置一次。
                .build();
        CakeRun.getInstance().applicationInit();
    }

    @AppInit(tag = 1, canSkip = true)
    protected void init1() {
        Log.d("TAG", "init1()  将引起crash。非关键路径可以跳过");
        String s = null;
        Log.d("TAG", s);
    }

    /**
     * 该初始化中有异步操作，需设置包名，在Application结束后持续监控属于该包名的Crash
     * 包名可设置多个
     * 建议：第三方库设置包名，项目代码设置全类名
     */
    @AsyncInit(tag = 2, packageName = {"com.lizhaoxuan.cakerundemo.Lib1", "other packageName"})
    protected void init2() {
        Log.d("TAG", "AsyncInit2() 引起Crash ,关键路径不可跳过");
        Lib1.AsyncInit();
    }

    @AppInit(tag = 3)
    protected void init3() {
        Log.d("TAG", "init3() 未引起crash");
    }

    @AppInit(tag = 4)
    protected void init4() {
        Log.d("TAG", "init4() 未引起crash");
    }


}
