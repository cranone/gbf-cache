package com.shadego.gbf.service;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSONPath;
import com.shadego.gbf.utils.RetrofitFactory;
import io.reactivex.disposables.Disposable;
import okhttp3.ResponseBody;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class CacheService {

    private static final Logger logger = LoggerFactory.getLogger(CacheService.class);

    @Value("${cache.path}")
    private String cachePath;

    public ResponseEntity<byte[]> createResponse(HttpServletRequest request){
        ResponseEntity<byte[]> response=null;
        try {
            HttpHeaders headers = new HttpHeaders();
            File file = this.download(request,headers);
            Path path = Paths.get(file.getPath());
            response=new ResponseEntity<>(Files.readAllBytes(path),headers, HttpStatus.OK);
        } catch (Exception e) {
            logger.error(e.getMessage(),e);
        }
        return response;
    }

    private File download(HttpServletRequest request,HttpHeaders headers) throws IOException {
        //获取URL
        String fullURL=request.getRequestURL().toString();
        //获取URI
        String uri= request.getRequestURI();
        //获取顶级域名
        String topDomain=fullURL.replaceAll("http[s]?://.*?(\\w+\\.\\w+)/.*", "$1");
        //GET参数
        String queryString=request.getQueryString();
        String requestUrl=StringUtils.isBlank(queryString)?fullURL:fullURL+"?"+queryString;
        String fileName=cachePath+"/"+topDomain+uri;
        File file=new File(fileName);
        File fileMapping=new File(fileName+".mapping");
        //从本地读取缓存
        if(this.hasCache(file,fileMapping,headers,fullURL,queryString)){
            return file;
        }
        //无文件或匹配不一致则重写文件
        boolean mkdirs = file.getParentFile().mkdirs();
        logger.info("Create:{}",fullURL);
        JSONObject mapping=new JSONObject();
        JSONPath.set(mapping,"$.request.queryString",queryString);
        Disposable subscribe = RetrofitFactory.getInstance().getApiService().download(requestUrl)
                .retry(3)
                .subscribe(result -> {
                    JSONObject headerJson = new JSONObject();
                    result.headers().names().forEach(item -> {
                        String headerValue = result.headers().get(item);
                        headers.set(item, headerValue);
                        headerJson.put(item, headerValue);
                    });
                    JSONPath.set(mapping, "$.response.headers", headerJson);
                    //写入映射文件
                    FileUtils.writeStringToFile(fileMapping, mapping.toJSONString(), StandardCharsets.UTF_8);
                    try (FileOutputStream fs = new FileOutputStream(file);
                         FileChannel fc = fs.getChannel();
                         ResponseBody body = result.body()) {
                        assert body != null;
                        byte[] bytes = body.bytes();
                        ByteBuffer bb = ByteBuffer.wrap(bytes);
                        bb.put(bytes);
                        bb.flip();
                        fc.write(bb);
                    }
                    logger.info("Complete:{}", uri);
                }, throwable -> {
                    Files.delete(file.toPath());
                    logger.error(throwable.getMessage(), throwable);
                });
        return file;
    }

    private boolean hasCache(File file,File fileMapping,HttpHeaders headers,String fullURL,String queryString) throws IOException {
        if(fileMapping.exists()&&fileMapping.length()>0){
            //获取文件参数
            String fileStr = FileUtils.readFileToString(fileMapping, StandardCharsets.UTF_8);
            JSONObject mapping = JSONObject.parseObject(fileStr);
            //判断是否存在QueryString并匹配
            if(StringUtils.isNotBlank(queryString)&&!JSONPath.containsValue(mapping,"$.request.queryString",queryString)){
                //不匹配
                return false;
            }
            //匹配一致则使用缓存
            if(file.exists()&&file.length()>0) {
                logger.info("Cache:{}",fullURL);
                //回写header
                JSONObject headerJson= (JSONObject) JSONPath.eval(mapping,"$.response.headers");
                for (String header : headerJson.keySet()) {
                    headers.set(header,headerJson.getString(header));
                }
                return true;
            }
        }
        return false;
    }
}
