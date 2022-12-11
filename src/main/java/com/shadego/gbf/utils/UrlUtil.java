package com.shadego.gbf.utils;

import com.github.monkeywie.proxyee.intercept.HttpProxyInterceptPipeline;
import com.github.monkeywie.proxyee.util.ProtoUtil;

import java.util.List;
import java.util.regex.Pattern;

public class UrlUtil {
    public static String getURL(HttpProxyInterceptPipeline pipeline){
        ProtoUtil.RequestProto requestProto = pipeline.getRequestProto();
        String url=requestProto.getSsl()?"https":"http";
        url=url+"://"+requestProto.getHost()+pipeline.getHttpRequest().uri();
        return url;
    }

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
