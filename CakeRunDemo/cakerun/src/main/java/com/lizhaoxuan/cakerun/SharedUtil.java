package com.lizhaoxuan.cakerun;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by lizhaoxuan on 16/6/11.
 */
public class SharedUtil {

    private static String SHARED_NAME = "cakeRun";

    private static String CRASH_LOG_KEY = "crashLog";
    private static String CLOSE_ACTIVITY_KEY = "closeActivity";

    private static SharedUtil instance;

    private SharedPreferences sharedPreferences;

    static SharedUtil getInstance(Context context) {
        if (instance == null) {
            instance = new SharedUtil(context);
        }
        return instance;
    }

    private SharedUtil(Context context) {
        sharedPreferences = context.getSharedPreferences(SHARED_NAME, Context.MODE_PRIVATE);
    }

    CrashLogDto loadCrashLog() {
        String log = sharedPreferences.getString(CRASH_LOG_KEY, "");
        return new CrashLogDto(log);
    }

    void saveCrashLog(CrashLogDto logDto) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(CRASH_LOG_KEY, logDto.toString());
        editor.apply();
    }

    List<CloseActivityDto> loadCloseActivity() {
        List<CloseActivityDto> list = new ArrayList<>();
        String log = sharedPreferences.getString(CLOSE_ACTIVITY_KEY, "");
        try {
            JSONArray array = new JSONArray(log);
            for (int i = 0, l = array.length(); i < l; i++) {
                list.add(new CloseActivityDto(array.getJSONObject(i).toString()));
            }
        } catch (JSONException e) {
            Log.w("CakeRun","CloseActivity json invalid format");
            e.printStackTrace();
            return list;
        }
        return list;
    }

    void saveCloseActivity(String json) {
        if (json == null || json.equals("")) {
            return;
        }
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(CLOSE_ACTIVITY_KEY, json);
        editor.apply();

    }


}
