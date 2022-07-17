package com.shadego.gbf.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.framework.AopContext;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.util.Map;

/**
 * 网络服务
 */
@Service
@EnableAspectJAutoProxy(proxyTargetClass = true, exposeProxy = true)
public class NetService {
    private static final Logger logger = LoggerFactory.getLogger(NetService.class);
    @Resource
    private RestTemplate restTemplate;

    @Retryable(value = RestClientException.class, maxAttemptsExpression = "${cache.retry.maxAttempts:3}",backoff = @Backoff(delayExpression = "${cache.retry.delay:500}",multiplier = 0.0))
    public ResponseEntity<byte[]> getBytes(String url, HttpHeaders headers){
        logger.info("Downloading:{}", url);
        if(headers==null){
            headers=new HttpHeaders();
        }
        //伪装user-agent
        headers.set("user-agent","Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/71.0.3578.98 Safari/537.36");
        return restTemplate.exchange(url, HttpMethod.GET,new HttpEntity<>(headers),byte[].class);
    }

    public ResponseEntity<byte[]> getBytes(String url, Map<String, String> headers){
        HttpHeaders httpHeaders=new HttpHeaders();
        if(headers!=null){
            httpHeaders.setAll(headers);
        }
        return ((NetService)AopContext.currentProxy()).getBytes(url,httpHeaders);
    }
}
