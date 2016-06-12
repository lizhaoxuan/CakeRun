package com.lizhaoxuan.cakerundemo;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.lizhaoxuan.cakerun.CakeRun;

public class TwoActivity extends AppCompatActivity {


    public static void startActivity(Activity activity){
        Intent intent = new Intent(activity,TwoActivity.class);
        /**
         * 通过CakeRun 提供的 startActivity 启动Activity.
         * 必要时可以关闭该入口
         */
        CakeRun.getInstance().startActivity(activity,TwoActivity.class,intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_two);
    }
}
