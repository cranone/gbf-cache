package com.shadego.gbf;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.annotation.EnableRetry;

@Configuration  
@ComponentScan  
@EnableAutoConfiguration
@EnableRetry(proxyTargetClass = true)
public class Application 
{
    public static void main( String[] args )
    {
    	SpringApplication.run(Application.class);
    }
}
