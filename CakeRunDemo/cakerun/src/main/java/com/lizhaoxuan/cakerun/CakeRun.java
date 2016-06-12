package com.lizhaoxuan.cakerun;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.cakerun.apt.AppInitDto;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by lizhaoxuan on 16/6/10.
 */
public class CakeRun {

    private static CakeRun instance;

    private static CakeRun init(Application context, String appVersion,
                                long resetCrashLogTime, boolean tryClearData,
                                long asyncMonitorTime, boolean alwaysRestartApp,
                                ICrashPatch crashPatch, int maxRestartNum) {
        if (instance == null) {
            instance = new CakeRun(context, appVersion, resetCrashLogTime,
                    tryClearData, asyncMonitorTime, alwaysRestartApp, crashPatch, maxRestartNum);
        }
        return instance;
    }

    public static CakeRun getInstance() {
        if (instance == null) {
            throw new Error("CakeRun must be init");
        }
        return instance;
    }

    private Context context;
    private CrashHandler crashHandler;

    /**
     * 由开发者从外部（网络等）设置在何种需求下关闭哪些Activity
     */
    private List<CloseActivityDto> closeActivity;

    /**
     * Crash记录日志每隔一段时间清空一次
     * 1天、3天、一周。（依照你APP使用频率配置）
     * 默认 3天。
     */
    private long resetCrashLogTime;

    /**
     * application Crash后，是否尝试清空数据再次启动
     */
    private boolean tryClearData;

    /**
     * 当前执行Tag
     */
    private int nowTag;

    /**
     * 在有异步任务存在是，application执行完毕后，持续监控时间
     */
    private long asyncMonitorTime;

    private boolean alwaysRestartApp;

    private ICrashPatch crashPatch;

    private int maxRestartNum;

    private CakeRun(Application context, String appVersion, long resetCrashLog,
                    boolean tryClearData, long asyncMonitorTime,
                    boolean alwaysRestartApp, ICrashPatch crashPatch,
                    int maxRestartNum) {
        this.context = context;
        this.resetCrashLogTime = resetCrashLog;
        this.tryClearData = tryClearData;
        this.asyncMonitorTime = asyncMonitorTime;
        this.alwaysRestartApp = alwaysRestartApp;
        this.crashPatch = crashPatch;
        this.maxRestartNum = maxRestartNum;
        crashHandler = CrashHandler.init(context);
        checkCrashLogDto(appVersion);
    }

    /**
     * 检查CrashLog是否需要重置
     *
     * @param versionName app版本号
     */
    private void checkCrashLogDto(String versionName) {
        CrashLogDto crashLogDto = getCrashLogDto();
        if (!versionName.equals(crashLogDto.getVersion()) || new Date().getTime() - crashLogDto.getBuildDate() > resetCrashLogTime) {
            //重置CrashLog
            getSharedUtil().saveCrashLog(new CrashLogDto(new Date().getTime(), versionName));
        } else {
            crashLogDto.setApplicationFinish(false);
            getSharedUtil().saveCrashLog(crashLogDto);
        }
    }

    public CrashLogDto getCrashLogDto() {
        return getSharedUtil().loadCrashLog();
    }

    public void saveCrashLogDto(CrashLogDto dto) {
        getSharedUtil().saveCloseActivity(dto.toString());
    }

    /**
     * Activity关闭交由开发者来控制
     * CakeRun会记录  奔溃位置的全类名 + 奔溃次数
     * 开发者根据其他Crash采集工具，向CakeRun发出命令：
     * 对在该类下发生过Crash并超过N次的的设备，停止开放某个Activity
     *
     * @return true :不再跳转该Activity
     */
    private boolean checkTargetActivity(Class<Activity> activity) {
        for (CloseActivityDto closeActivityDto : getCloseActivity()) {
            closeActivityNotice();
            return closeActivityDto.check(activity.getCanonicalName(), getCrashLogDto());
        }
        return false;
    }

    private List<CloseActivityDto> getCloseActivity() {
        if (closeActivity == null) {
            closeActivity = getSharedUtil().loadCloseActivity();
        }
        return closeActivity;
    }

    public void applicationInit() {
        Class clazz = context.getClass();
        try {
            Class injectorClazz = Class.forName(clazz.getName() + "$$PROXY");
            AbstractApplication application = (AbstractApplication) injectorClazz
                    .newInstance();
            application.init(context);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void setCloseActivity(String json) {
        getSharedUtil().saveCloseActivity(json);
        closeActivity = getSharedUtil().loadCloseActivity();
    }

    public boolean haveAsyncInit() {
        return getAsyncInitList().size() != 0;
    }

    /**
     * 取得异步初始化
     */
    public List<AppInitDto> getAsyncInitList() {
        List<AppInitDto> list = new ArrayList<>();
        for (AppInitDto appInitDto : getAppInitList()) {
            if (appInitDto.isAsync) {
                list.add(appInitDto);
            }
        }
        return list;
    }

    /**
     * 取得所有初始化操作
     */
    public List<AppInitDto> getAppInitList() {
        List<AppInitDto> list = new ArrayList<>();
        try {
            Class injectorClazz = Class.forName("com.lizhaoxuan.cakerun.AppInitList$$Cake");
            IAppInitList asyncInitList = (IAppInitList) injectorClazz
                    .newInstance();
            list = asyncInitList.getAppInitList();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    private SharedUtil getSharedUtil() {
        return SharedUtil.getInstance(context);
    }

    public void setNowTag(int tag) {
        nowTag = tag;
    }

    public void setApplicationFinish() {
        CrashLogDto crashLogDto = getCrashLogDto();
        crashLogDto.setApplicationFinish(true);
        getSharedUtil().saveCrashLog(crashLogDto);
    }

    public boolean isTryClearData() {
        return tryClearData;
    }

    public int getNowTag() {
        return nowTag;
    }

    public boolean isAlwaysRestartApp() {
        return alwaysRestartApp;
    }

    public void showCrashNotice() {
        if (crashPatch != null) {
            crashPatch.showCrashNotice();
        }
    }

    public void closeActivityNotice() {
        if (crashPatch != null) {
            crashPatch.closeActivityNotice();
        }
    }

    public int getMaxRestartNum() {
        return maxRestartNum;
    }

    public void startApplicationCrashActivity() {
        if (crashPatch != null) {
            crashPatch.startApplicationCrashActivity();
        }
    }

    /**
     * 异步初始化任务是否已完成
     * 无异步任务或application执行完毕，且超过最大持续监控时间表示异步任务以完成
     *
     * @return true :完成  false : 未完成
     */
    public boolean isAsyncInitFinish() {
        CrashLogDto crashLogDto = getCrashLogDto();
        return !haveAsyncInit() || crashLogDto.isApplicationFinish()
                && new Date().getTime() - crashLogDto.getApplicationFinishTime() > asyncMonitorTime;
    }

    /**
     * startActivity。
     * startActivity 检查该Activity是否已被开发者屏蔽
     *
     * @param activity
     * @param targetActivity
     * @param intent
     */
    public void startActivity(Activity activity, Class targetActivity, Intent intent) {
        if (checkTargetActivity(targetActivity)) {
            return;
        }
        activity.startActivity(intent);
    }

    public void startActivity(Activity activity, Class targetActivity, Intent intent, Bundle options) {
        if (checkTargetActivity(targetActivity)) {
            return;
        }
        activity.startActivity(intent, options);
    }

    public void startActivityForResult(Activity activity, Class<Activity> targetActivity, Intent intent, int requestCode) {
        if (checkTargetActivity(targetActivity)) {
            return;
        }
        activity.startActivityForResult(intent, requestCode);
    }

    public void startActivityForResult(Activity activity, Class targetActivity, Intent intent, int requestCode, Bundle options) {
        if (checkTargetActivity(targetActivity)) {
            return;
        }
        activity.startActivityForResult(intent, requestCode, options);
    }

    public void startActivityFromChild(Activity activity, Class targetActivity, Activity child, Intent intent,
                                       int requestCode) {
        if (checkTargetActivity(targetActivity)) {
            return;
        }
        activity.startActivityFromChild(child, intent, requestCode);
    }

    public void startActivityFromChild(Activity activity, Class targetActivity, Activity child, Intent intent,
                                       int requestCode, Bundle options) {
        if (checkTargetActivity(targetActivity)) {
            return;
        }
        activity.startActivityFromChild(child, intent, requestCode, options);
    }

    public boolean startActivityIfNeeded(Activity activity, Class targetActivity, Intent intent, int requestCode) {
        if (checkTargetActivity(targetActivity)) {
            return false;
        }
        return activity.startActivityIfNeeded(intent, requestCode);
    }

    public boolean startActivityIfNeeded(Activity activity, Class targetActivity, Intent intent, int requestCode, Bundle options) {
        if (checkTargetActivity(targetActivity)) {
            return false;
        }
        return activity.startActivityIfNeeded(intent, requestCode, options);
    }

    public static class Build {

        private Application context;

        private String appVersion;

        private long resetCrashLogTime = 1000 * 60 * 60 * 24 * 3;

        private boolean tryClearData = false;

        private long asyncMonitorTime = 1500;

        private boolean alwaysRestartApp = false;

        private int maxRestartNum = 5;

        private ICrashPatch crashPatch;

        public Build(Application context, String appVersion, ICrashPatch crashPatch) {
            this.context = context;
            this.appVersion = appVersion;
            this.crashPatch = crashPatch;
        }

        public Build setResetCrashLogTime(long resetCrashLogTime) {
            this.resetCrashLogTime = resetCrashLogTime;
            return this;
        }

        private Build setTryClearData(boolean tryClearData) {
            this.tryClearData = tryClearData;
            return this;
        }

        public Build setAlwaysRestartApp(boolean alwaysRestartApp) {
            this.alwaysRestartApp = alwaysRestartApp;
            return this;
        }

        public Build setAsyncMonitorTime(long time) {
            this.asyncMonitorTime = time;
            return this;
        }

        public Build setMaxRestartNum(int num) {
            this.maxRestartNum = num;
            return this;
        }

        public CakeRun build() {
            return CakeRun.init(context, appVersion, resetCrashLogTime, tryClearData, asyncMonitorTime, alwaysRestartApp, crashPatch, maxRestartNum);
        }
    }
}
