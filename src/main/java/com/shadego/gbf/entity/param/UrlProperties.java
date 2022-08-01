package com.shadego.gbf.entity.param;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

@Component
@ConfigurationProperties(prefix = "cache.url")
public class UrlProperties {
    //代理后缀
    private String suffix;
    // 缓存排除地址
    private List<String> exclude;
    //缓存排除表达式
    private List<Pattern> excludePattern;
    // 403排除地址
    private List<String> excludeModified;
    //403排除表达式
    private List<Pattern> excludeModifiedPattern;
    // 阻止请求
    private List<String> block;

    private List<Pattern> blockPattern;

    public List<String> getExclude() {
        return exclude;
    }

    public void setExclude(List<String> exclude) {
        this.exclude = exclude;
        this.excludePattern =new ArrayList<>();
        for (String item : this.exclude) {
            this.excludePattern.add(Pattern.compile(item));
        }
    }

    public List<Pattern> getExcludePattern() {
        return this.excludePattern;
    }

    public List<String> getExcludeModified() {
        return excludeModified;
    }

    public void setExcludeModified(List<String> excludeModified) {
        this.excludeModified = excludeModified;
    }

    public List<Pattern> getExcludeModifiedPattern() {
        //初始化
        if(CollectionUtils.isEmpty(this.excludeModified)){
            return Collections.emptyList();
        }
        if(this.excludeModifiedPattern ==null){
            this.excludeModifiedPattern =new ArrayList<>();
            for (String exclude : this.excludeModified) {
                this.excludeModifiedPattern.add(Pattern.compile(exclude));
            }
        }
        return this.excludeModifiedPattern;
    }

    public List<String> getBlock() {
        return block;
    }

    public void setBlock(List<String> block) {
        this.block = block;
        this.blockPattern=new ArrayList<>();
        for (String item : this.block) {
            this.blockPattern.add(Pattern.compile(item));
        }

    }

    public String getSuffix() {
        return suffix;
    }

    public void setSuffix(String suffix) {
        this.suffix = suffix;
    }

    public List<Pattern> getBlockPattern() {
        return blockPattern;
    }

    public void setBlockPattern(List<Pattern> blockPattern) {
        this.blockPattern = blockPattern;
    }
}
