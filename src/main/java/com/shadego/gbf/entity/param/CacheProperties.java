package com.shadego.gbf.entity.param;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

@Component
@ConfigurationProperties(prefix = "cache")
public class CacheProperties {
    // 缓存路径
    private String path;
    // 缓存排除地址
    private List<String> urlExclude;
    //缓存排除表达式
    private List<Pattern> excludePattern;
    // 403排除地址
    private List<String> urlExcludeModified;
    //403排除表达式
    private List<Pattern> excludeModifiedPattern;

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public List<String> getUrlExclude() {
        return urlExclude;
    }

    public void setUrlExclude(List<String> urlExclude) {
        this.urlExclude = urlExclude;
    }

    public List<Pattern> getExcludePattern() {
        //初始化
        if(CollectionUtils.isEmpty(this.urlExclude)){
            return Collections.emptyList();
        }
        if(this.excludePattern ==null){
            this.excludePattern =new ArrayList<>();
            for (String exclude : this.urlExclude) {
                this.excludePattern.add(Pattern.compile(exclude));
            }
        }
        return this.excludePattern;
    }


    public List<String> getUrlExcludeModified() {
        return urlExcludeModified;
    }

    public void setUrlExcludeModified(List<String> urlExcludeModified) {
        this.urlExcludeModified = urlExcludeModified;
    }

    public List<Pattern> getExcludeModifiedPattern() {
        //初始化
        if(CollectionUtils.isEmpty(this.urlExcludeModified)){
            return Collections.emptyList();
        }
        if(this.excludeModifiedPattern ==null){
            this.excludeModifiedPattern =new ArrayList<>();
            for (String exclude : this.urlExcludeModified) {
                this.excludeModifiedPattern.add(Pattern.compile(exclude));
            }
        }
        return this.excludeModifiedPattern;
    }
}
