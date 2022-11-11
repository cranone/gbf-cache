package com.shadego.gbf.proxy;

import com.github.monkeywie.proxyee.exception.HttpProxyExceptionHandle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class CacheProxyExceptionHandle extends HttpProxyExceptionHandle {
    private static final Logger logger = LoggerFactory.getLogger(CacheProxyExceptionHandle.class);
    @Override
    public void startCatch(Throwable e) {
        logger.error("Netty Error",e);
    }
}
