package com.shadego.gbf.utils;

import java.util.List;
import java.util.regex.Pattern;

public class UrlUtil {
    public static boolean isCompile(String uri, List<Pattern> patternList){
        //判断是否被排除
        for (Pattern pattern : patternList) {
            if(pattern.matcher(uri).find()){
                return true;
            }
        }
        return false;
    }
}
