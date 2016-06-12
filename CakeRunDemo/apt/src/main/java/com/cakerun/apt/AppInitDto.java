package com.cakerun.apt;

/**
 * Created by lizhaoxuan on 16/6/11.
 */
public class AppInitDto implements Comparable<AppInitDto> {

    public boolean isAsync = false;

    public String packageName;

    public int tag;

    public boolean canSkip;

    public String methodName;

    public AppInitDto(boolean canSkip, boolean isAsync, String methodName, String packageName, int tag) {
        this.canSkip = canSkip;
        this.isAsync = isAsync;
        this.methodName = methodName;
        this.packageName = packageName;
        this.tag = tag;
    }

    @Override
    public int compareTo(AppInitDto o) {
        if (this.tag > o.tag) {
            return 1;
        } else {
            return -1;
        }
    }
}
