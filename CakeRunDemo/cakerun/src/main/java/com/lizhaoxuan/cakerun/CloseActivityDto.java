package com.lizhaoxuan.cakerun;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by lizhaoxuan on 16/6/11.
 */
public class CloseActivityDto {

    private String fullActivityName;

    private int crashNum;

    private String versionName;

    CloseActivityDto(String json) {
        try {
            JSONObject jsonObject = new JSONObject(json);
            fullActivityName = jsonObject.getString("fullActivityName");
            crashNum = jsonObject.getInt("crashNum");
            versionName = jsonObject.getString("versionName");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    boolean check(String activityName, CrashLogDto crashLogDto) {
        if (activityName.equals(fullActivityName)) {
            if (crashLogDto != null && crashLogDto.getActivities() != null && crashLogDto.getActivities().size() > 0) {
                for (CrashLogDto.CrashActivity crashActivity : crashLogDto.getActivities()) {
                    if (crashActivity.getActivityName().contains(fullActivityName) && crashActivity.getCrashNum() >= crashNum) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @Override
    public String toString() {
        JSONObject object = new JSONObject();
        try {
            object.put("fullActivityName", fullActivityName);
            object.put("crashNum", crashNum);
            object.put("versionName", versionName);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return object.toString();
    }
}
