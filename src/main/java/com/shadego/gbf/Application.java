package com.shadego.gbf;

import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
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
        new SpringApplicationBuilder(Application.class).web(WebApplicationType.NONE).run(args);
    	//SpringApplication.run(Application.class);
    }
}
