package com.shadego.gbf.entity.param;

import java.util.Map;

public class ResponseData {
    private Integer code=500;
    private Map<String,String> headers;
    private byte[] body;

    public ResponseData() {
    }

    public ResponseData(Integer code, Map<String, String> headers, byte[] body) {
        this.code = code;
        this.headers = headers;
        this.body = body;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public byte[] getBody() {
        return body;
    }

    public void setBody(byte[] body) {
        this.body = body;
    }

    public boolean isSuccessful(){
        return code>=200&&code<300;
    }
}
