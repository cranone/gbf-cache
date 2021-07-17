package com.shadego.gbf.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSONPath;
import com.alibaba.fastjson.parser.Feature;
import com.shadego.gbf.entity.param.DownloadData;
import com.shadego.gbf.utils.GZIPCompression;
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
import org.springframework.util.CollectionUtils;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CacheService {

    private static final Logger logger = LoggerFactory.getLogger(CacheService.class);

    @Value("${cache.path}")
    private String cachePath;

    public ResponseEntity<byte[]> createResponseString(HttpServletRequest request){
        ResponseEntity<byte[]> response=null;
        try {
            HttpHeaders headers = new HttpHeaders();
            DownloadData data = this.download(request,headers);
            JSON json=(JSON) JSON.parse(FileUtils.readFileToString(new File(data.getPath()),StandardCharsets.UTF_8), Feature.OrderedField);
            String str=json.toJSONString();
            byte[] result=str.getBytes(StandardCharsets.UTF_8);
            List<String> encoding = headers.get("content-encoding");
            if(!CollectionUtils.isEmpty(encoding)&&encoding.contains("gzip")){
                result=GZIPCompression.compress(str);
            }
            response=new ResponseEntity<>(result,headers, HttpStatus.OK);
        } catch (Exception e) {
            logger.error(e.getMessage(),e);
        }
        return response;
    }

    public ResponseEntity<byte[]> createResponse(HttpServletRequest request){
        ResponseEntity<byte[]> response=null;
        try {
            HttpHeaders headers = new HttpHeaders();
            DownloadData data = this.download(request,headers);
            byte[] result=null;
            if(data.getSuccess()){
                Path path = Paths.get(data.getPath());
                result=Files.readAllBytes(path);
            }
            response=new ResponseEntity<>(result,headers, HttpStatus.valueOf(data.getHttpCode()));
        } catch (Exception e) {
            logger.error(e.getMessage(),e);
        }
        return response;
    }

    private DownloadData download(HttpServletRequest request, HttpHeaders headers) throws IOException {
        DownloadData data=new DownloadData();
        //获取URL
        String fullURL=request.getRequestURL().toString();
        if(StringUtils.isNotBlank(request.getHeader("my-https"))){
            fullURL=fullURL.replaceAll("http://","https://");
        }
        //获取URI
        String uri= request.getRequestURI();
        //获取顶级域名
        String topDomain=fullURL.replaceAll("http[s]?://.*?([\\w|\\-]+\\.\\w+)/.*", "$1");
        //GET参数
        String queryString=request.getQueryString();
        String requestUrl=StringUtils.isBlank(queryString)?fullURL:fullURL+"?"+queryString;
        String fileName=cachePath+"/"+topDomain+uri;
        File file=new File(fileName);
        File fileMapping=new File(fileName+".mapping");
        data.setPath(file.getPath());
        //从本地读取缓存
        if(this.hasCache(file,fileMapping,headers,fullURL,queryString)){
            if(this.isAlwaysCache(request)){
                data.setSuccess(false);
                data.setHttpCode(HttpStatus.NOT_MODIFIED.value());
            }else{
                data.setSuccess(true);
                data.setHttpCode(HttpStatus.OK.value());
            }
            data.setCached(true);
            return data;
        }
        //无文件或匹配不一致则重写文件
        boolean mkdirs = file.getParentFile().mkdirs();
        logger.info("Create:{}",fullURL);
        JSONObject mapping=new JSONObject();
        JSONPath.set(mapping,"$.request.queryString",queryString);
        Enumeration<String> headerNames = request.getHeaderNames();
        Map<String, String> requestHeaders=new HashMap<>();
        while(headerNames.hasMoreElements()){
            String str = headerNames.nextElement();
            //https请求
            if("my-https".equals(str)){
                continue;
            }
            requestHeaders.put(str,request.getHeader(str));
        }
        Disposable subscribe = RetrofitFactory.getInstance().getApiService().download(requestUrl,requestHeaders)
                .retry(3)
                .subscribe(result -> {
                    data.setHttpCode(result.code());
                    if(!result.isSuccessful()){
                        throw new RuntimeException("服务器响应状态错误:"+data.getHttpCode());
                    }
                    JSONObject headerJson = new JSONObject();
                    headers.set("Access-Control-Allow-Origin", "*");
                    headerJson.put("Access-Control-Allow-Origin", "*");
                    result.headers().names().forEach(item -> {
                        String headerValue = result.headers().get(item);
                        headers.set(item, headerValue);
                        headerJson.put(item, headerValue);
                    });
                    JSONPath.set(mapping, "$.response.headers", headerJson);
                    //写入映射文件
                    FileUtils.writeStringToFile(fileMapping, mapping.toJSONString(), StandardCharsets.UTF_8);

//                    if("gzip".equalsIgnoreCase(result.headers().get("content-encoding"))){
//                        FileUtils.writeByteArrayToFile(file,GZIPCompression.decompress(result.body().bytes()).getBytes(StandardCharsets.UTF_8));
//                    }else{
//                        FileUtils.writeByteArrayToFile(file,result.body().bytes());
//                    }
                    try (ResponseBody body = result.body()){
                        if(body==null){
                            throw new RuntimeException("服务器无响应数据");
                        }
                        FileUtils.writeByteArrayToFile(file,body.bytes());
                    }
//                    FileUtils.writeStringToFile(file,result.body().string(),StandardCharsets.UTF_8);
//                    try (FileOutputStream fs = new FileOutputStream(file);
//                         FileChannel fc = fs.getChannel();
//                         ResponseBody body = result.body()) {
//                        assert body != null;
//                        byte[] bytes = body.bytes();
//                        ByteBuffer bb = ByteBuffer.wrap(bytes);
//                        bb.put(bytes);
//                        bb.flip();
//                        fc.write(bb);
//                    }
                    logger.info("Complete:{}", uri);
                    data.setSuccess(true);
                }, throwable -> {
                    logger.error("下载异常:{}",throwable.getMessage());
                    if(file.exists())
                        FileUtils.delete(file);
                });
        data.setCached(false);
        return data;
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

    private boolean isAlwaysCache(HttpServletRequest request){
        return StringUtils.isNotBlank(request.getHeader("If-Modified-Since"))
                ||StringUtils.isNotBlank(request.getHeader("If-None-Match"));
    }
}
