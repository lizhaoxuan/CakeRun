package com.cakerun.apt;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.lang.model.element.TypeElement;

/**
 * Created by lizhaoxuan on 16/6/12.
 */
public class ProxyInfo {
    public static final String PROXY = "PROXY";

    private String packageName;
    private String targetClassName;
    private String proxyClassName;
    private TypeElement typeElement;

    private List<AppInitDto> appInitDtos;
    private boolean isCheck = false;

    ProxyInfo(String packageName, String className) {
        this.packageName = packageName;
        this.targetClassName = className;
        this.proxyClassName = className + "$$" + PROXY;
    }

    String getApplicationFullName() {
        return packageName + "." + proxyClassName;
    }

    String getAppInitListFullName() {
        return "com.lizhaoxuan.cakerun.AppInitList$$Cake";
    }

    String getAppInitListName() {
        return "AppInitList$$Cake";
    }

    String getAppInitListPackageName() {
        return "com.lizhaoxuan.cakerun";
    }

    TypeElement getTypeElement() {
        return typeElement;
    }

    void setTypeElement(TypeElement typeElement) {
        this.typeElement = typeElement;
    }

    public String getProxyClassName() {
        return proxyClassName;
    }

    public String getTargetClassName() {
        return targetClassName;
    }


    public void addAppInitDto(AppInitDto dto) throws CakeRunException {
        if (appInitDtos == null) {
            appInitDtos = new ArrayList<>();
        }
        for (AppInitDto appInitDto : appInitDtos) {
            if (appInitDto.tag == dto.tag) {
                throw new CakeRunException("@AppInit or @AsyncInit tag can not be equal!!!");
            }
        }
        appInitDtos.add(dto);
    }

    private List<AppInitDto> getAppInitDtos() {
        if (appInitDtos == null) {
            return new ArrayList<>();
        }
        return appInitDtos;
    }


    public String generateApplicationCode() throws CakeRunException {
        checkTag();
        StringBuilder builder = new StringBuilder();
        builder.append("// Generated code from CakeRun. Do not modify!\n");
        builder.append("package ").append(packageName).append(";\n\n");

        builder.append("import com.lizhaoxuan.cakerun.CakeRun;\n");
        builder.append("import com.lizhaoxuan.cakerun.CrashLogDto;\n");
        builder.append("import com.lizhaoxuan.cakerun.AbstractApplication;\n");
        builder.append("import java.util.Date;\n");
        builder.append('\n');

        builder.append("public class ").append(proxyClassName);
        builder.append("<T extends ").append(getTargetClassName()).append(">");
        builder.append(" implements AbstractApplication<T>");
        builder.append(" {\n");

        builder.append("  @Override ")
                .append("public void init(final ")
                .append(getTargetClassName())
                .append(" target) {\n");
        builder.append(" CakeRun cakeRun = CakeRun.getInstance();\n");
        builder.append(" CrashLogDto crashLogDto = cakeRun.getCrashLogDto();\n");

        for (AppInitDto dto : getAppInitDtos()) {
            builder.append("if(crashLogDto.haveTag(")
                    .append(dto.tag).append(")){\n")
                    .append("if(").append(!dto.canSkip).append("){\n")
                    .append("cakeRun.startApplicationCrashActivity();\n " +
                            "return;}\n");
            builder.append("}else{ \n")
                    .append("cakeRun.setNowTag(")
                    .append(dto.tag)
                    .append(");\n");
            builder.append("target.")
                    .append(dto.methodName)
                    .append("();\n } \n");
        }
        builder.append("crashLogDto.setApplicationFinish(true); \n")
                .append("crashLogDto.setApplicationFinishTime(new Date().getTime()); \n")
                .append("crashLogDto.setApplicationRestartNum(0);\n")
                .append("cakeRun.saveCrashLogDto(crashLogDto);\n");

        builder.append("}\n");
        builder.append("}\n");

        return builder.toString();
    }

    public String generateAppInitListCode() throws CakeRunException {
        checkTag();
        StringBuilder builder = new StringBuilder();
        builder.append("// Generated code from CakeRun. Do not modify!\n");
        builder.append("package ").append(getAppInitListPackageName()).append(";\n\n");

        builder.append("import com.lizhaoxuan.cakerun.IAppInitList;\n");
        builder.append("import java.util.ArrayList;\n");
        builder.append("import java.util.List;\n");
        builder.append("import com.cakerun.apt.AppInitDto;\n");
        builder.append('\n');

        builder.append("public class ").append(getAppInitListName());
        builder.append(" implements IAppInitList");
        builder.append(" {\n");

        builder.append("private List<AppInitDto> list = null; \n");

        builder.append("  @Override ")
                .append("public List<AppInitDto> getAppInitList() {\n")
                .append("if(list == null){ \n")
                .append("list = new ArrayList<>();\n");

        for (AppInitDto dto : getAppInitDtos()) {
            builder.append("list.add(new AppInitDto(")
                    .append(dto.canSkip).append(", ")
                    .append(dto.isAsync).append(", \"")
                    .append(dto.methodName).append("\", \"")
                    .append(dto.packageName).append("\", ")
                    .append(dto.tag).append("));\n");
        }

        builder.append("}\n");
        builder.append(" return list;\n } \n");

        builder.append("} ");

        return builder.toString();
    }

    private boolean checkTag() throws CakeRunException {
        if (isCheck) {
            return true;
        }
        if (appInitDtos != null) {
            Collections.sort(appInitDtos);
        }
        return isCheck = true;

    }
}
