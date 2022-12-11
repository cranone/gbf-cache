package com.shadego.gbf.utils;

import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.HttpHeaders;
import kotlin.Pair;
import okhttp3.Headers;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class HeaderUtil {
    /**
     * Netty->Map
     * @return Map<String,String>
     */
    public static Map<String,String> toMapHeader(Headers param){
        Map<String,String> map=new LinkedHashMap<>();
        for (Pair<? extends String, ? extends String> pair : param) {
            map.put(pair.getFirst(), pair.getSecond());
        }
        return map;
    }

    /**
     * Map->Netty
     * @return HttpHeaders
     */
    public static HttpHeaders toNettyHeader(Map<String,String> param){
        HttpHeaders headers = new DefaultHttpHeaders();
        for (Map.Entry<String, String> entry : param.entrySet()) {
            headers.set(entry.getKey(), entry.getValue());
        }
        return headers;
    }

    /**
     * Spring header转换为Netty header
     * @param param Spring header
     * @return Netty header
     */
    public static HttpHeaders toNettyHeader(org.springframework.http.HttpHeaders param){
        HttpHeaders headers = new DefaultHttpHeaders();
        for (Map.Entry<String, List<String>> stringListEntry : param.entrySet()) {
            headers.set(stringListEntry.getKey(),stringListEntry.getValue());
        }
        return headers;
    }

    /**
     * Netty->Spring
     * @return HttpHeaders
     */
    public static org.springframework.http.HttpHeaders toSpringHeader(HttpHeaders param){
        org.springframework.http.HttpHeaders springHeaders=new org.springframework.http.HttpHeaders();
        for (Map.Entry<String, String> entry : param.entries()) {
            springHeaders.set(entry.getKey(),entry.getValue());
        }
        return springHeaders;
    }

    public static org.springframework.http.HttpHeaders toSpringHeader(Map<String,String> param){
        org.springframework.http.HttpHeaders headers=new org.springframework.http.HttpHeaders();
        for (Map.Entry<String, String> entry : param.entrySet()) {
            headers.set(entry.getKey(), entry.getValue());
        }
        return headers;
    }
}
