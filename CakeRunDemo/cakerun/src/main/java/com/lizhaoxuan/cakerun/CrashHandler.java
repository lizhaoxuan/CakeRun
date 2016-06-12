package com.lizhaoxuan.cakerun;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.cakerun.apt.AppInitDto;

import java.lang.Thread.UncaughtExceptionHandler;

/**
 * UncaughtException处理类,当程序发生Uncaught异常的时候,由该类来接管程序
 * 需要在Application中注册，为了要在程序启动器就监控整个程序。
 */
public class CrashHandler implements UncaughtExceptionHandler {
    public static final String TAG = "CrashHandler";

    //CrashHandler实例
    private static CrashHandler instance;
    //程序的Context对象
    private Context context;
    //系统默认的UncaughtException处理类
    private Thread.UncaughtExceptionHandler defaultHandler;
    /**
     * 获取CrashHandler实例 ,单例模式
     */
    public static CrashHandler init(Context context) {
        if (instance == null) {
            instance = new CrashHandler(context);
        }
        return instance;
    }

    public CrashHandler(Context context) {
        this.context = context;
        defaultHandler = Thread.getDefaultUncaughtExceptionHandler();
        //设置该CrashHandler为程序的默认处理器
        Thread.setDefaultUncaughtExceptionHandler(this);
    }

    /**
     * 当UncaughtException发生时会转入该函数来处理
     */
    @Override
    public void uncaughtException(Thread thread, Throwable ex) {
        if (!handleException(ex)) {
            //如果用户没有处理则让系统默认的异常处理器来处理
            defaultHandler.uncaughtException(thread, ex);
        }
    }

    /**
     * 自定义错误处理,收集错误信息 发送错误报告等操作均在此完成.
     *
     * @param ex
     * @return true:如果处理了该异常信息;否则返回false.
     */
    private boolean handleException(Throwable ex) {
        if (ex == null) {
            return false;
        }
        StringBuilder crashClassName = new StringBuilder();
        String className = "";
        for (StackTraceElement stack: ex.getStackTrace()){
            crashClassName.append(stack.toString()).append("  ");
        }
        className = crashClassName.toString();
        CrashLogDto crashLogDto = SharedUtil.getInstance(context).loadCrashLog();
        addCrashClass(crashLogDto, crashClassName.toString());

        //有异步初始化任务未完成
        if (!CakeRun.getInstance().isAsyncInitFinish()) {
            for (AppInitDto asyncInitDto : CakeRun.getInstance().getAsyncInitList()) {
                if (className.contains(asyncInitDto.packageName)) {
                    crashLogDto.addAppCrashTag(asyncInitDto.tag);
                    return applicationRestartApp(crashLogDto);
                }
            }
        }
        if (!crashLogDto.isApplicationFinish()) {
            crashLogDto.addAppCrashTag(CakeRun.getInstance().getNowTag());
            return applicationRestartApp(crashLogDto);
        } else {
            CakeRun.getInstance().showCrashNotice();
            if (CakeRun.getInstance().isAlwaysRestartApp()) {
                restartApp();
                return true;
            } else {
                return false;
            }
        }
    }

    private boolean applicationRestartApp(CrashLogDto dto) {
        int num = dto.getApplicationRestartNum();
        if (num > CakeRun.getInstance().getMaxRestartNum()) {
            SharedUtil.getInstance(context).saveCrashLog(dto);
            return false;
        }
        dto.addApplicationRestartNum();
        SharedUtil.getInstance(context).saveCrashLog(dto);
        restartApp();
        return true;
    }

    private void restartApp() {
        Intent i = context.getPackageManager()
                .getLaunchIntentForPackage(context.getPackageName());
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        context.startActivity(i);
        //退出程序
        android.os.Process.killProcess(android.os.Process.myPid());
    }

    /**
     * 增加一个Crash记录
     */
    private void addCrashClass(CrashLogDto crashLogDto, String crashClassName) {
        if (crashLogDto.getActivities() == null) {
            crashLogDto.addCrashActivity(crashClassName);
        } else {
            for (CrashLogDto.CrashActivity activity : crashLogDto.getActivities()) {
                if (activity.equals(crashClassName)) {
                    activity.addCrashNum();
                } else {
                    crashLogDto.addCrashActivity(crashClassName);
                }
            }
        }
        SharedUtil.getInstance(context).saveCrashLog(crashLogDto);
    }

}