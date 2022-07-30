package com.shadego.gbf.config;


import com.github.monkeywie.proxyee.server.HttpProxyServer;
import com.github.monkeywie.proxyee.server.HttpProxyServerConfig;
import com.shadego.gbf.proxy.CacheProxyExceptionHandle;
import com.shadego.gbf.proxy.CacheProxyInterceptInitializer;
import com.shadego.gbf.ssl.PfxCertFactory;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
public class NettyApplicationRunner implements ApplicationRunner, ApplicationListener<ContextClosedEvent>, ApplicationContextAware {
    private static final Logger logger = LoggerFactory.getLogger(NettyApplicationRunner.class);
    @Value("${server.port}")
    private int port;

    @Resource
    private PfxCertFactory pfxCertFactory;
    @Resource
    private CacheProxyInterceptInitializer cacheProxyInterceptInitializer;
    @Resource
    private CacheProxyExceptionHandle cacheProxyExceptionHandle;

    private ApplicationContext applicationContext;
    HttpProxyServer httpProxyServer;

    @Override
    public void setApplicationContext(@NotNull ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        logger.info("proxy start:{}",port);
        HttpProxyServerConfig config = new HttpProxyServerConfig();
        config.setHandleSsl(true);
        this.httpProxyServer = new HttpProxyServer().serverConfig(config).caCertFactory(pfxCertFactory)
                .proxyInterceptInitializer(cacheProxyInterceptInitializer)
                .httpProxyExceptionHandle(cacheProxyExceptionHandle);
        this.httpProxyServer.start(port);
    }

    @Override
    public void onApplicationEvent(@NotNull ContextClosedEvent event) {
        if (this.httpProxyServer != null) {
            this.httpProxyServer.close();
        }
        logger.info("proxy stop");
    }
}
