package com.shadego.gbf.proxy;

import com.github.monkeywie.proxyee.intercept.HttpProxyIntercept;
import com.github.monkeywie.proxyee.intercept.HttpProxyInterceptPipeline;
import com.shadego.gbf.entity.param.CacheData;
import com.shadego.gbf.entity.param.ResponseData;
import com.shadego.gbf.entity.param.UrlProperties;
import com.shadego.gbf.service.CacheService;
import com.shadego.gbf.service.OkHttpService;
import com.shadego.gbf.utils.HeaderUtil;
import com.shadego.gbf.utils.UrlUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class CacheProxyIntercept extends HttpProxyIntercept {
    private static final Logger logger = LoggerFactory.getLogger(HttpProxyIntercept.class);

    @Resource
    private UrlProperties urlProperties;
    @Resource
    private CacheService cacheService;
    @Resource
    private OkHttpService okHttpService;

    @Override
    public void beforeRequest(Channel clientChannel, HttpRequest httpRequest, HttpProxyInterceptPipeline pipeline) throws Exception {
        logger.debug("beforeRequest httpRequest");
        String uri = httpRequest.uri();
        String url=UrlUtil.getURL(pipeline);
        logger.debug("netty url:{},uri:{}",url,uri);
        //判断是否block
        if(UrlUtil.isCompile(url,urlProperties.getBlockPattern())){
            HttpResponseStatus code = HttpResponseStatus.INTERNAL_SERVER_ERROR;
            logger.info("[{}]block:{}",code.code(),url);
            HttpResponse hookResponse = new DefaultHttpResponse(HttpVersion.HTTP_1_1, code);
            clientChannel.writeAndFlush(hookResponse);
            clientChannel.close();
            return;
        }
        org.springframework.http.HttpHeaders springHeaders = HeaderUtil.toSpringHeader(httpRequest.headers());
        Pattern pat=Pattern.compile("[\\w]+[\\.]("+urlProperties.getSuffix()+")");//正则判断
        Matcher mc=pat.matcher(uri);//条件匹配
        byte[] result = null;
        if(mc.find()){
            CacheData data = cacheService.download(url, uri, springHeaders);
            HttpResponse hookResponse = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.valueOf(data.getHttpCode()), HeaderUtil.toNettyHeader(data.getResponseHeaders()));
            clientChannel.writeAndFlush(hookResponse);
            Path path = Paths.get(data.getPath());
            result= Files.readAllBytes(path);
        }else{
            //高并发下存在丢包,原因未知,因此接管默认方式
            logger.info("Direct:{}",url);
            ResponseData response = okHttpService.getBytes(url, springHeaders);
            HttpHeaders headers = HeaderUtil.toNettyHeader(response.getHeaders());
            headers.set("Access-Control-Allow-Origin", "*");
            HttpResponse hookResponse = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.valueOf(response.getCode()),headers);
            clientChannel.writeAndFlush(hookResponse);
            result = response.getBody();
        }
        ByteBuf byteBufN = ByteBufAllocator.DEFAULT.heapBuffer(result==null?0:result.length);
        byteBufN.writeBytes(result);
        HttpContent lastContent = new DefaultLastHttpContent(byteBufN);
        clientChannel.writeAndFlush(lastContent);
        clientChannel.close();
        //super.beforeRequest(clientChannel, httpRequest, pipeline);
    }

    @Override
    public void beforeRequest(Channel clientChannel, HttpContent httpContent, HttpProxyInterceptPipeline pipeline) throws Exception {
        logger.debug("beforeRequest httpContent");
        super.beforeRequest(clientChannel, httpContent, pipeline);
    }

    @Override
    public void afterResponse(Channel clientChannel, Channel proxyChannel, HttpResponse httpResponse, HttpProxyInterceptPipeline pipeline) throws Exception {
        logger.debug("afterResponse httpResponse");
        logger.info("Direct:{}",UrlUtil.getURL(pipeline));
        super.afterResponse(clientChannel, proxyChannel, httpResponse, pipeline);
    }

    @Override
    public void afterResponse(Channel clientChannel, Channel proxyChannel, HttpContent httpContent, HttpProxyInterceptPipeline pipeline) throws Exception {
        logger.debug("afterResponse httpContent");
        super.afterResponse(clientChannel, proxyChannel, httpContent, pipeline);
    }
}
