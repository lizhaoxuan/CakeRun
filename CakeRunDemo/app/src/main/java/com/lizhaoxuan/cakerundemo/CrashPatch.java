package com.lizhaoxuan.cakerundemo;

import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.lizhaoxuan.cakerun.ICrashPatch;

/**
 * Created by lizhaoxuan on 16/6/12.
 */
public class CrashPatch implements ICrashPatch {
    private Context context;
    public CrashPatch(Context context) {
        this.context = context;
    }

    @Override
    public void startApplicationCrashActivity() {
        Intent in = new Intent(context, CrashPatchActivity.class);
        in.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
        context.startActivity(in);
    }
    @Override
    public void showCrashNotice() {
        try {
            Thread.sleep(3000);
            Toast.makeText(context, "抱歉，程序奔溃，正在重新启动", Toast.LENGTH_SHORT).show();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    @Override
    public void closeActivityNotice() {
        Toast.makeText(context, "抱歉，该页面发生错误", Toast.LENGTH_SHORT).show();
    }
}
