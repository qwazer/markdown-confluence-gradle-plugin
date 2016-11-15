package com.github.qwazer.gradle.markdown.confluence.core;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;


/**
 * Created by Anton Reshetnikov on 15 Nov 2016.
 */
@Configuration
@ComponentScan(basePackages = "com.github.qwazer.gradle.markdown.confluence")
public class SpringConfig {

    @Bean
    public RestTemplate restTemplate(){
        return new RestTemplate();
    }


}
