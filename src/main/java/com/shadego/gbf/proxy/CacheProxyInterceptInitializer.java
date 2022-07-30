package com.shadego.gbf.proxy;

import com.github.monkeywie.proxyee.intercept.HttpProxyInterceptInitializer;
import com.github.monkeywie.proxyee.intercept.HttpProxyInterceptPipeline;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
public class CacheProxyInterceptInitializer extends HttpProxyInterceptInitializer {
    @Resource
    private CacheProxyIntercept cacheProxyIntercept;
    @Resource
    private CacheFullRequestIntercept cacheFullRequestIntercept;

    @Override
    public void init(HttpProxyInterceptPipeline pipeline) {
        pipeline.addLast(cacheProxyIntercept);
    }
}
