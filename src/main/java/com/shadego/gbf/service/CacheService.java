package com.shadego.gbf.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSONPath;
import com.alibaba.fastjson.parser.Feature;
import com.shadego.gbf.entity.param.CacheData;
import com.shadego.gbf.entity.param.CacheProperties;
import com.shadego.gbf.entity.param.DownloadData;
import com.shadego.gbf.utils.GZIPCompression;
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
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

@Service
public class CacheService {

    private static final Logger logger = LoggerFactory.getLogger(CacheService.class);
    private static final DateTimeFormatter DF_RFC = DateTimeFormatter.RFC_1123_DATE_TIME.withZone(ZoneId.of("GMT"));

    @Value("${cache.path}")
    private String cachePath;
    @Value("${cache.refreshDate}")
    private Boolean refreshDate;
    @Resource
    private CacheProperties cacheProperties;
    @Resource
    private NetService netService;

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
        boolean alwaysCache = this.isAlwaysCache(request);
        CacheData cacheData=new CacheData();
        cacheData.setFile(file);
        cacheData.setFileMapping(fileMapping);
        cacheData.setHeaders(headers);
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

        try {
            ResponseEntity<byte[]> result = netService.getBytes(requestUrl, requestHeaders);
            data.setHttpCode(result.getStatusCodeValue());
            if(!result.getStatusCode().is2xxSuccessful()){
                throw new RuntimeException("服务器响应状态错误:"+data.getHttpCode());
            }
            JSONObject headerJson = new JSONObject();
            headers.set("Access-Control-Allow-Origin", "*");
            headerJson.put("Access-Control-Allow-Origin", "*");
            result.getHeaders().forEach((key, value) -> {
                String headerValue = value.get(0);
                headers.set(key, headerValue);
                headerJson.put(key, headerValue);
            });
            JSONPath.set(mapping, "$.response.headers", headerJson);
            //写入映射文件
            FileUtils.writeStringToFile(fileMapping, mapping.toJSONString(), StandardCharsets.UTF_8);
            byte[] body = result.getBody();
            if(body==null){
                throw new RuntimeException("服务器无响应数据");
            }
            FileUtils.writeByteArrayToFile(file,body);
            logger.info("Complete:{}", uri);
            data.setSuccess(true);
        }catch (Exception e){
            logger.error("下载异常",e);
            if(file.exists())
                FileUtils.delete(file);
        }
        data.setCached(false);
        return data;
    }

    private boolean hasCache(CacheData cacheData) throws IOException {
        File fileMapping = cacheData.getFileMapping();
        String queryString = cacheData.getQueryString();
        File file = cacheData.getFile();
        if(fileMapping.exists()&&fileMapping.length()>0&&!isExclude(cacheData.getFullURL(),cacheProperties.getExcludePattern())){
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
    }

    private boolean isExclude(String uri,List<Pattern> patternList){
        //判断是否被排除
        for (Pattern pattern : patternList) {
            if(pattern.matcher(uri).find()){
                return true;
            }
        }
        return false;
    }

    private boolean isAlwaysCache(HttpServletRequest request){
        if(isExclude(request.getRequestURL().toString(), cacheProperties.getExcludeModifiedPattern())){
            return false;
        }
        return StringUtils.isNotBlank(request.getHeader("If-Modified-Since"))
                ||StringUtils.isNotBlank(request.getHeader("If-None-Match"));
    }

}
