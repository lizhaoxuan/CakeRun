package com.lizhaoxuan.cakerun;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by lizhaoxuan on 16/6/10.
 */
public class CrashLogDto {

    /**
     * 日志创建时间
     * Crash记录日志每隔一段时间清空一次
     * 1天、3天、一周。（依照你APP使用频率配置）
     * 默认 3天。可配置
     */
    private long buildDate;

    /**
     * APP 版本信息，版本变更后清空日志记录
     */
    private String version = "";

    /**
     * Application代码执行完毕。
     * 执行完毕前的Crash属于Application Crash
     */
    private boolean isApplicationFinish = false;

    private long applicationFinishTime;

    private List<Integer> appCrashTag;

    private List<CrashActivity> activities;

    private int applicationRestartNum = 0;


    public CrashLogDto(long buildDate, String versionName) {
        this.version = versionName;
        this.buildDate = buildDate;
    }

    public CrashLogDto(String json) {
        if (json != null && !json.equals("")) {
            try {
                JSONObject jsonObject = new JSONObject(json);
                buildDate = jsonObject.optLong("buildDate");
                version = jsonObject.optString("version");
                isApplicationFinish = jsonObject.optBoolean("isApplicationFinish", false);
                applicationFinishTime = jsonObject.optLong("applicationFinishTime");
                applicationRestartNum = jsonObject.optInt("applicationRestartNum");
                String appCrashTagStr = jsonObject.optString("appCrashTag");
                if (appCrashTagStr != null && !appCrashTagStr.equals("")) {
                    JSONArray tagArray = new JSONArray(appCrashTagStr);
                    appCrashTag = new ArrayList<>();
                    for (int i = 0, l = tagArray.length(); i < l; i++) {
                        appCrashTag.add(tagArray.getInt(i));
                    }
                }
                String crashActivityStr = jsonObject.optString("crashActivity");
                if (crashActivityStr != null && !crashActivityStr.equals("")) {
                    JSONArray activityArray = new JSONArray(crashActivityStr);
                    activities = new ArrayList<>();
                    for (int i = 0, l = activityArray.length(); i < l; i++) {
                        JSONObject object = activityArray.getJSONObject(i);
                        if (object != null) {
                            activities.add(new CrashActivity(object.toString()));
                        }
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public String toString() {
        JSONObject jsonObject;
        try {
            jsonObject = new JSONObject();
            jsonObject.put("buildDate", buildDate);
            jsonObject.put("version", version);
            jsonObject.put("isApplicationFinish", isApplicationFinish);
            jsonObject.put("applicationFinishTime", applicationFinishTime);
            jsonObject.put("applicationRestartNum", applicationRestartNum);
            if (appCrashTag != null) {
                JSONArray tagArray = new JSONArray();
                for (Integer i : appCrashTag) {
                    tagArray.put(i);
                }
                jsonObject.put("appCrashTag", tagArray);
            }
            if (activities != null) {
                JSONArray array = new JSONArray();
                for (CrashActivity activity : activities) {
                    array.put(new JSONObject(activity.toString()));
                }
                jsonObject.put("crashActivity", array);
            }
        } catch (JSONException e) {
            e.printStackTrace();
            return "";
        }
        return jsonObject.toString();
    }

    public List<CrashActivity> getActivities() {
        return activities;
    }

    public void addCrashActivity(String crashName) {
        if (crashName == null || crashName.equals("")) {
            return;
        }
        if (activities == null) {
            activities = new ArrayList<>();
        }
        activities.add(new CrashActivity(crashName, 0));
    }

    public List<Integer> getAppCrashTag() {
        return appCrashTag;
    }

    public void addAppCrashTag(int tag) {
        if (appCrashTag == null) {
            appCrashTag = new ArrayList<>();
        }
        appCrashTag.add(tag);
    }

    public boolean haveTag(int tag) {
        if (appCrashTag == null) {
            return false;
        }
        for (int t : appCrashTag) {
            if (t == tag) {
                return true;
            }
        }
        return false;
    }

    public long getBuildDate() {
        return buildDate;
    }

    public void setBuildDate(long buildDate) {
        this.buildDate = buildDate;
    }

    public boolean isApplicationFinish() {
        return isApplicationFinish;
    }

    public void setApplicationFinish(boolean applicationFinish) {
        isApplicationFinish = applicationFinish;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public long getApplicationFinishTime() {
        return applicationFinishTime;
    }

    public void setApplicationFinishTime(long applicationFinishTime) {
        this.applicationFinishTime = applicationFinishTime;
    }

    public int getApplicationRestartNum() {
        return applicationRestartNum;
    }

    public void addApplicationRestartNum() {
        this.applicationRestartNum++;
    }

    public void setApplicationRestartNum(int applicationRestartNum) {
        this.applicationRestartNum = applicationRestartNum;
    }

    public class CrashActivity {
        private String activityName;
        private int crashNum;

        public CrashActivity(String activityName, int crashNum) {
            this.activityName = activityName;
            this.crashNum = crashNum;
        }

        CrashActivity(String json) {
            try {
                JSONObject object = new JSONObject(json);
                activityName = object.getString("activityName");
                crashNum = object.getInt("crashNum");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        @Override
        public boolean equals(Object o) {
            return o != null && o.toString().contains(activityName);
        }

        @Override
        public String toString() {
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("activityName", activityName);
                jsonObject.put("crashNum", crashNum);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return jsonObject.toString();
        }

        String getActivityName() {
            return activityName;
        }

        void setActivityName(String activityName) {
            this.activityName = activityName;
        }

        int getCrashNum() {
            return crashNum;
        }

        void addCrashNum() {
            this.crashNum++;
        }
    }
}
