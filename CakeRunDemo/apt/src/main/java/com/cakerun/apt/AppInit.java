package com.cakerun.apt;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 同步的初始化方法
 * tag:唯一标记，将按照tag从小到大执行
 * Created by lizhaoxuan on 16/6/10.
 */
@Retention(RetentionPolicy.CLASS)
@Target(ElementType.METHOD)
public @interface AppInit {

    int tag();

    boolean canSkip() default false;
}
