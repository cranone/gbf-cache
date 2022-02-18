package com.shadego.gbf.entity.param;

import org.springframework.http.HttpStatus;

public class DownloadData {
    private String path;
    private boolean isCached;
    private Integer httpCode;
    private boolean isSuccess;

    public DownloadData(){
        //默认500
        httpCode= HttpStatus.INTERNAL_SERVER_ERROR.value();
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

    public boolean getSuccess() {
        return isSuccess;
    }

    public void setSuccess(boolean success) {
        isSuccess = success;
    }
}
