package com.cakerun.apt;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 异步初始化方法.
 * 所有异步初始化方法会在Application中其他初始化方法执行完毕后，持续监控一段时间。
 * tag:唯一标记，不可重复，按照tag从小到大执行。
 * packageName：第三方库建议使用包名，项目代码建议使用全类名
 * Created by lizhaoxuan on 16/6/10.
 */
@Retention(RetentionPolicy.CLASS)
@Target(ElementType.METHOD)
public @interface AsyncInit {
    int tag();

    String[] packageName();

    boolean canSkip() default false;
}
