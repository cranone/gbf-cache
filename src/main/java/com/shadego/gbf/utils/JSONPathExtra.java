package com.shadego.gbf.utils;

import com.alibaba.fastjson2.JSONPath;

/**
 * JSONPath补充类，针对FastJSON部分不存在方法补完
 */
public abstract class JSONPathExtra extends JSONPath {
    protected JSONPathExtra(String path) {
        super(path);
    }

    public static boolean containsValue(Object rootObject, String path, String value) {
        Object eval = JSONPath.eval(rootObject, path);
        if (value == null) {
            return null == eval;
        } else {
            return value.equals(eval);
        }

    }
}
