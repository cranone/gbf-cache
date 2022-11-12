package com.shadego.gbf.service;

import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;

import javax.annotation.Resource;
import java.io.IOException;

@Service
public class OkHttpService {
    private static final Logger logger = LoggerFactory.getLogger(OkHttpService.class);
    @Resource
    private OkHttpClient okHttpClient;

    @Retryable(value = RestClientException.class, maxAttemptsExpression = "${cache.retry.maxAttempts:3}",backoff = @Backoff(delayExpression = "${cache.retry.delay:500}",multiplier = 0.0))
    public Response getBytes(String url, MultiValueMap<String, String> headers) throws IOException {
        if(headers==null){
            headers=new HttpHeaders();
        }
        //伪装user-agent
        if(headers.get("user-agent")==null&&headers.get("User-Agent")==null){
            headers.set("user-agent","Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/71.0.3578.98 Safari/537.36");
        }
        return this.get(url,headers);
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
