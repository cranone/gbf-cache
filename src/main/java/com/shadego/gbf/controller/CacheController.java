package com.shadego.gbf.controller;

import com.shadego.gbf.service.CacheService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


@Controller
@RequestMapping
public class CacheController {
    @Resource
    private CacheService cacheService;

    @ResponseBody
    //@GetMapping(value="/**/*.json", produces =MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<byte[]> cacheString(HttpServletRequest request, HttpServletResponse response) {
        return cacheService.createResponseString(request);
    }

    @GetMapping(value="/**", produces =MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<byte[]> cache(HttpServletRequest request,HttpServletResponse response) {
        return cacheService.createResponse(request);
    }
}
