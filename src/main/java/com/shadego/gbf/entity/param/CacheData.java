package com.shadego.gbf.entity.param;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;

import java.io.File;

public class CacheData {
    private File file;
    private File fileMapping;
    private HttpHeaders headers;
    private String fullURL;
    private String queryString;
    private boolean alwaysCache;

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

    public HttpHeaders getHeaders() {
        return headers;
    }

    public void setHeaders(HttpHeaders headers) {
        this.headers = headers;
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
}
