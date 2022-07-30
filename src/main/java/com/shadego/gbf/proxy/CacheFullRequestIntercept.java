package com.shadego.gbf.proxy;

import com.github.monkeywie.proxyee.intercept.HttpProxyIntercept;
import com.github.monkeywie.proxyee.intercept.HttpProxyInterceptPipeline;
import com.github.monkeywie.proxyee.intercept.common.FullRequestIntercept;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class CacheFullRequestIntercept extends FullRequestIntercept {
    private static final Logger logger = LoggerFactory.getLogger(HttpProxyIntercept.class);

    @Override
    public boolean match(HttpRequest httpRequest, HttpProxyInterceptPipeline pipeline) {
        logger.info("beforeRequest match");
        return true;
    }

    @Override
    public void handleRequest(FullHttpRequest httpRequest, HttpProxyInterceptPipeline pipeline) {
        logger.info("beforeRequest handleRequest：{}",httpRequest.uri());
        super.handleRequest(httpRequest, pipeline);
    }

    @Override
    public void beforeRequest(Channel clientChannel, HttpContent httpContent, HttpProxyInterceptPipeline pipeline) throws Exception {
        logger.info("beforeRequest httpContent");
        super.beforeRequest(clientChannel, httpContent, pipeline);
    }

    @Override
    public void afterResponse(Channel clientChannel, Channel proxyChannel, HttpResponse httpResponse, HttpProxyInterceptPipeline pipeline) throws Exception {
        logger.info("afterResponse httpResponse");
        super.afterResponse(clientChannel, proxyChannel, httpResponse, pipeline);
    }

    @Override
    public void afterResponse(Channel clientChannel, Channel proxyChannel, HttpContent httpContent, HttpProxyInterceptPipeline pipeline) throws Exception {
        logger.info("afterResponse httpContent");
        super.afterResponse(clientChannel, proxyChannel, httpContent, pipeline);
    }
}
