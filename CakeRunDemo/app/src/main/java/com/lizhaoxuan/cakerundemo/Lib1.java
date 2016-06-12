package com.lizhaoxuan.cakerundemo;

import android.util.Log;

/**
 * Created by lizhaoxuan on 16/6/12.
 */
public class Lib1 {

    public static void AsyncInit() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(3000);
                    String s = null;
                    Log.d("TAG",s);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

}
