package com.lizhaoxuan.cakerun;

/**
 * 自定义Crash后，进行补偿措施
 * Created by lizhaoxuan on 16/6/11.
 */
public interface ICrashPatch {

    /**
     * Application关键路径Crash后，启动的更新Activity
     */
    void startApplicationCrashActivity();

    /**
     * 非Application Crash后，给予的提示
     * 若弹Toast提示，可延时几秒
     * 该方法可空缺
     */
    void showCrashNotice();

    /**
     * 屏蔽某Activity启动后，给予用户的提示
     */
    void closeActivityNotice();
}
