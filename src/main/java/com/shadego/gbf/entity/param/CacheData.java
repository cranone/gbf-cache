package com.shadego.gbf.entity.param;

import org.springframework.http.HttpStatus;

import java.io.File;
import java.util.Map;

public class CacheData {
    private File file;
    private File fileMapping;
    private Map<String,String> responseHeaders;
    private String fullURL;
    private String queryString;
    private boolean alwaysCache;
    private String path;
    private boolean isCached;
    private Integer httpCode=HttpStatus.INTERNAL_SERVER_ERROR.value();
    private boolean isSuccess;

    public HttpStatus getHttpStatus(){
        if(alwaysCache){
            return HttpStatus.NOT_MODIFIED;
        }else {
            return HttpStatus.OK;
        }
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public File getFileMapping() {
        return fileMapping;
    }

    public void setFileMapping(File fileMapping) {
        this.fileMapping = fileMapping;
    }

    public Map<String,String> getResponseHeaders() {
        return responseHeaders;
    }

    public void setResponseHeaders(Map<String,String> responseHeaders) {
        this.responseHeaders = responseHeaders;
    }

    public String getFullURL() {
        return fullURL;
    }

    public void setFullURL(String fullURL) {
        this.fullURL = fullURL;
    }

    public String getQueryString() {
        return queryString;
    }

    public void setQueryString(String queryString) {
        this.queryString = queryString;
    }

    public boolean isAlwaysCache() {
        return alwaysCache;
    }

    public void setAlwaysCache(boolean alwaysCache) {
        this.alwaysCache = alwaysCache;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public boolean isCached() {
        return isCached;
    }

    public void setCached(boolean cached) {
        isCached = cached;
    }

    public Integer getHttpCode() {
        return httpCode;
    }

    public void setHttpCode(Integer httpCode) {
        this.httpCode = httpCode;
    }

    public boolean isSuccess() {
        return isSuccess;
    }

    public void setSuccess(boolean success) {
        isSuccess = success;
    }
}
