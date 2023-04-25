package com.shadego.gbf.service;

import com.shadego.gbf.entity.param.ResponseData;
import com.shadego.gbf.utils.HeaderUtil;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;

import javax.annotation.Resource;
import java.io.IOException;
import java.net.SocketTimeoutException;

@Service
public class OkHttpService {
    private static final Logger logger = LoggerFactory.getLogger(OkHttpService.class);
    @Resource
    private OkHttpClient okHttpClient;

    @Retryable(value = {IOException.class, SocketTimeoutException.class}, maxAttemptsExpression = "${cache.retry.maxAttempts:3}",backoff = @Backoff(delayExpression = "${cache.retry.delay:500}",multiplier = 0.0))
    public ResponseData getBytes(String url, MultiValueMap<String, String> headers) throws IOException {
        if(headers==null){
            headers=new HttpHeaders();
        }
        //伪装user-agent
        if(headers.get("user-agent")==null&&headers.get("User-Agent")==null){
            headers.set("user-agent","Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/71.0.3578.98 Safari/537.36");
        }
        try (Response response = this.get(url, headers)){
            ResponseBody body = response.body();
            return new ResponseData(response.code(), HeaderUtil.toMapHeader(response.headers()),body==null?null:body.bytes());
        }
    }

    private Response get(String url,MultiValueMap<String, String> headers) throws IOException {
        Headers.Builder builder = new Headers.Builder();
        if(headers!=null){
            headers.toSingleValueMap().forEach(builder::add);
        }
        Request request = new Request.Builder().url(url).headers(builder.build()).build();
        Call call = okHttpClient.newCall(request);
        return call.execute();
    }

}
