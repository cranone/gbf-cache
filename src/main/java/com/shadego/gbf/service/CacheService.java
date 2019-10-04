package com.shadego.gbf.service;

import java.io.File;
import java.io.FileOutputStream;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.shadego.gbf.utils.RetrofitFactory;

import okhttp3.ResponseBody;

@Service
public class CacheService {
    private static final Logger logger = LoggerFactory.getLogger(CacheService.class);
    
    @Value("${cache.path}")
    private String cachePath;
    
    public ResponseEntity<byte[]> createResponse(HttpServletRequest request){
        ResponseEntity<byte[]> response=null;
        try {
            URL url = new URL(request.getRequestURL().toString());
            String fullURL=url.toURI().toString();
            String queryString=request.getQueryString();
            String fileURL=fullURL;
            if(!StringUtils.isEmpty(queryString)) {
                String temp=fullURL.substring(0,fullURL.lastIndexOf("."))+"_"+queryString+fullURL.substring(fullURL.lastIndexOf("."));
                fileURL=temp;
                fullURL=fullURL+"?"+queryString;
            }
            HttpHeaders headers = new HttpHeaders();
            File file = this.download(fullURL,fileURL,headers);
            Path path = Paths.get(file.getPath());
            //自动判断Content-Type(Mime-Type)
            String mediaType=Files.probeContentType(path);
            if(file.getPath().endsWith(".js")) {
                mediaType="text/javascript;charset=UTF-8";
            }else if(file.getPath().endsWith(".css")) {
                mediaType="text/css;charset=UTF-8";
            }
            headers.setContentType(MediaType.valueOf(mediaType));
            headers.setConnection("keep-alive");
            headers.setLastModified(file.lastModified());
            headers.set("Server", "Apache");
            headers.set("Access-Control-Allow-Origin","http://game.granbluefantasy.jp");
            response=new ResponseEntity<>(Files.readAllBytes(path),headers, HttpStatus.OK);
        } catch (Exception e) {
           logger.error(e.getMessage(),e);
        }
        return response;
    }
    
    private File download(String fullURL,String fileURL,HttpHeaders headers) {
      //删除域名保留路径
        String path = fileURL.replaceFirst("http://\\S+?\\.\\S+?(?=/)", "");
        File file=new File(cachePath+"/GBF"+path);
        if(file.exists()&&file.length()>0) {
            logger.info("Cache:{}",fileURL);
            return file;
        }
        file.getParentFile().mkdirs();
        logger.info("Create:{}",fileURL);
        RetrofitFactory.getInstance().getApiService().download(fullURL)
        .retry(3)
        .subscribe(result->{
            result.headers().names().forEach(item->{
                headers.set(item, result.headers().get(item));
            });
            try (FileOutputStream fs = new FileOutputStream(file);
                    FileChannel fc = fs.getChannel();
                    ResponseBody body = result.body();) {
                byte[] bytes = body.bytes();
                ByteBuffer bb = ByteBuffer.wrap(bytes);
                bb.put(bytes);
                bb.flip();
                fc.write(bb);
            }
            SimpleDateFormat sdfHeader = new SimpleDateFormat("EEE,dd MMM yyyy HH:mm:ss Z", Locale.US);
            boolean setLastModified = file.setLastModified(sdfHeader.parse(result.headers().get("Last-Modified")).getTime());
            logger.info("Complete:{},status:{}",path,setLastModified);
        }, throwable -> {
            Files.delete(file.toPath());
            logger.error(throwable.getMessage(),throwable);
        });
        return file;
    }
}
