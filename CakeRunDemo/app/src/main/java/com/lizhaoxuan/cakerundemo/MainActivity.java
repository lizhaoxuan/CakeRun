package com.lizhaoxuan.cakerundemo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.lizhaoxuan.cakerun.CakeRun;

    public class MainActivity extends AppCompatActivity {
        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_main);
            CakeRun.getInstance().setCloseActivity(loadCloseActivity());
        }
        /**
         * MainActivity是应用启动后第一个执行的Activity
         * 在这个Activity中，通常我们会获取一些更新信息、在线参数等
         * 通过其他Bug收集工具，开发者可以对Bug进行分析，决定关闭哪些正在发生Crash的非关键路径
         * Activity.
         * 假设我们通过下面方法取得开发者发出的Activity关闭信息。
         * 得到一个Json字符串后交给CakeRun处理。
         * 只有符合该版本且发生目标包名下的Crash到一定次数，Activity才会被关闭。
         */
        private String loadCloseActivity() {
            return "对应的json字符串";
        }
    }
