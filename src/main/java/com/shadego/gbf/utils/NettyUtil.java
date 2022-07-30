package com.shadego.gbf.utils;

import com.github.monkeywie.proxyee.intercept.HttpProxyInterceptPipeline;
import com.github.monkeywie.proxyee.util.ProtoUtil;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.HttpHeaders;

import java.util.List;
import java.util.Map;

public class NettyUtil {
    public static HttpHeaders fromSpringHeader(org.springframework.http.HttpHeaders springHeaders){
        HttpHeaders headers = new DefaultHttpHeaders();
        for (Map.Entry<String, List<String>> stringListEntry : springHeaders.entrySet()) {
            headers.set(stringListEntry.getKey(),stringListEntry.getValue());
        }
        return headers;
    }

    public static org.springframework.http.HttpHeaders toSpringHeader(HttpHeaders headers){
        org.springframework.http.HttpHeaders springHeaders=new org.springframework.http.HttpHeaders();
        for (Map.Entry<String, String> entry : headers.entries()) {
            springHeaders.set(entry.getKey(),entry.getValue());
        }
        return springHeaders;
    }

    public static String getURL(HttpProxyInterceptPipeline pipeline){
        ProtoUtil.RequestProto requestProto = pipeline.getRequestProto();
        String url=requestProto.getSsl()?"https":"http";
        url=url+"://"+requestProto.getHost()+pipeline.getHttpRequest().uri();
        return url;
    }
}
