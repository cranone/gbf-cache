package com.shadego.gbf.service;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.JSONPath;
import com.alibaba.fastjson2.JSONReader;
import com.shadego.gbf.entity.param.CacheData;
import com.shadego.gbf.entity.param.DownloadData;
import com.shadego.gbf.entity.param.UrlProperties;
import com.shadego.gbf.exception.CacheException;
import com.shadego.gbf.utils.GZIPCompression;
import com.shadego.gbf.utils.JSONPathExtra;
import com.shadego.gbf.utils.UrlUtil;
import okhttp3.Response;
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

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

@Service
public class CacheService {

    private static final Logger logger = LoggerFactory.getLogger(CacheService.class);
    private static final DateTimeFormatter DF_RFC = DateTimeFormatter.RFC_1123_DATE_TIME.withZone(ZoneId.of("GMT"));

    @Value("${cache.path}")
    private String cachePath;
    @Value("${cache.refreshDate}")
    private Boolean refreshDate;
    @Resource
    private UrlProperties urlProperties;
    @Resource
    private NetService netService;
    @Resource
    private OkHttpService okHttpService;

    public ResponseEntity<byte[]> createResponseString(HttpServletRequest request){
        ResponseEntity<byte[]> response=null;
        try {
            DownloadData data = this.download(request);
            HttpHeaders headers = data.getResponseHeader();
            JSON json=(JSON) JSON.parse(FileUtils.readFileToString(new File(data.getPath()),StandardCharsets.UTF_8), JSONReader.Feature.UseNativeObject);
            String str=JSON.toJSONString(json);
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
            DownloadData data = this.download(request);
            HttpHeaders headers = data.getResponseHeader();
            byte[] result=null;
            if(data.getSuccess()){
                Path path = Paths.get(data.getPath());
                result=Files.readAllBytes(path);
            }
            response=new ResponseEntity<>(result,headers, HttpStatus.valueOf(data.getHttpCode()));
        } catch (Exception e) {
            logger.error("Error:{}",request.getRequestURL().toString());
            logger.error(e.getMessage(),e);
        }
        return response;
    }

    public DownloadData download(HttpServletRequest request) throws IOException {
        String uri= request.getRequestURI();
        String url=request.getRequestURL().toString();
        String queryString=request.getQueryString();
        if(StringUtils.isNotBlank(queryString)){
            url+="?"+queryString;
        }
        Enumeration<String> headerNames = request.getHeaderNames();
        HttpHeaders requestHeader=new HttpHeaders();
        while(headerNames.hasMoreElements()){
            String str = headerNames.nextElement();
            requestHeader.put(str, Collections.singletonList(request.getHeader(str)));
        }
        return this.download(url,uri,requestHeader);
    }

    public DownloadData download(String url,String uri,HttpHeaders requestHeader) throws IOException{
        DownloadData data=new DownloadData();
        HttpHeaders responseHeaders=new HttpHeaders();
        data.setResponseHeader(responseHeaders);
        //获取URL
        String fullURL=url;
        if(!CollectionUtils.isEmpty(requestHeader.get("my-https"))){
            fullURL=fullURL.replaceAll("http://","https://");
        }
        //获取顶级域名
        String topDomain=fullURL.replaceAll("http[s]?://.*?([\\w|\\-]+\\.\\w+)/.*", "$1");
        String fileName=cachePath+"/"+topDomain+uri;
        //获取请求参数
        String queryString=null;
        if(uri.indexOf("?")>0){
            queryString=uri.substring(uri.lastIndexOf("?")+1);
            fileName=fileName.substring(0,fileName.indexOf("?"));
        }
        File file=new File(fileName);
        File fileMapping=new File(fileName+".mapping");
        data.setPath(file.getPath());
        boolean alwaysCache = this.isAlwaysCache(url,requestHeader);
        CacheData cacheData=new CacheData();
        cacheData.setFile(file);
        cacheData.setFileMapping(fileMapping);
        cacheData.setHeaders(responseHeaders);
        cacheData.setFullURL(fullURL);
        cacheData.setQueryString(queryString);
        cacheData.setAlwaysCache(alwaysCache);
        //从本地读取缓存
        if(this.hasCache(cacheData)){
            data.setSuccess(!alwaysCache);
            data.setHttpCode(cacheData.getHttpStatus().value());
            data.setCached(true);
            return data;
        }
        //无文件或匹配不一致则重写文件
        boolean mkdirs = file.getParentFile().mkdirs();
        logger.info("Create:{}",fullURL);
        JSONObject mapping=new JSONObject();
        JSONPath.set(mapping,"$.request.queryString",queryString);
        requestHeader.remove("my-https");
        try (Response result = okHttpService.getBytes(fullURL, requestHeader)){
            data.setHttpCode(result.code());
            if(!result.isSuccessful()){
                throw new CacheException("服务器响应状态错误:"+data.getHttpCode());
            }
            JSONObject headerJson = new JSONObject();
            responseHeaders.set("Access-Control-Allow-Origin", "*");
            headerJson.put("Access-Control-Allow-Origin", "*");
            result.headers().forEach(pair -> {
                responseHeaders.set(pair.getFirst(), pair.getSecond());
                headerJson.put(pair.getFirst(), pair.getSecond());
            });
            JSONPath.set(mapping, "$.response.headers", headerJson);
            //写入映射文件
            FileUtils.writeStringToFile(fileMapping, mapping.toJSONString(), StandardCharsets.UTF_8);
//            byte[] body = result.getBody();
//            byte[] body = result.body();
            ResponseBody resultBody = result.body();
            byte[] body = resultBody ==null?null: resultBody.bytes();
            if(body==null){
                throw new CacheException("服务器无响应数据");
            }
            FileUtils.writeByteArrayToFile(file,body);
            logger.info("Complete:{}", uri);
            data.setSuccess(true);
        } catch (CacheException e){
            logger.error("下载异常:{}",e.getMessage());
            if(file.exists())
                FileUtils.delete(file);
        }catch (Exception e){
            logger.error("下载异常",e);
            if(file.exists())
                FileUtils.delete(file);
        }
        data.setCached(false);
        return data;
    }

    private boolean hasCache(CacheData cacheData) {
        try {
            File fileMapping = cacheData.getFileMapping();
            String queryString = cacheData.getQueryString();
            File file = cacheData.getFile();
            if(fileMapping.exists()&&fileMapping.length()>0&&!UrlUtil.isCompile(cacheData.getFullURL(), urlProperties.getExcludePattern())){
                //获取文件参数
                String fileStr = FileUtils.readFileToString(fileMapping, StandardCharsets.UTF_8);
                JSONObject mapping = JSONObject.parseObject(fileStr);
                //判断是否存在QueryString并匹配
                if(StringUtils.isNotBlank(queryString)&&!JSONPathExtra.containsValue(mapping,"$.request.queryString",queryString)){
                    //不匹配
                    return false;
                }
                //匹配一致则使用缓存
                if(file.exists()&&file.length()>0) {
                    logger.info("[{}]Cache:{}",cacheData.getHttpStatus().value(),cacheData.getFullURL());
                    //回写header
                    JSONObject headerJson= (JSONObject) JSONPath.eval(mapping,"$.response.headers");
                    for (String header : headerJson.keySet()) {
                        String value = headerJson.getString(header);
                        if("Date".equals(header)&&refreshDate){
                            value=DF_RFC.format(Instant.now());
                        }
                        cacheData.getHeaders().set(header, value);
                    }
                    return true;
                }
            }
            return false;
        }catch (Exception e){
            //缓存异常则认为不存在缓存
            logger.warn("cache error[{}]:{}",e.getMessage(),cacheData.getFullURL());
            return false;
        }

    }

    private boolean isAlwaysCache(String url,HttpHeaders requestHeader){
        if(UrlUtil.isCompile(url, urlProperties.getExcludeModifiedPattern())){
            return false;
        }
        return !CollectionUtils.isEmpty(requestHeader.get("If-Modified-Since"))
                ||!CollectionUtils.isEmpty(requestHeader.get("If-None-Match"));
    }

}
