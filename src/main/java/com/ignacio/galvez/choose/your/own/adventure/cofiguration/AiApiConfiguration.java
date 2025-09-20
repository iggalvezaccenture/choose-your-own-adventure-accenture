package com.ignacio.galvez.choose.your.own.adventure.cofiguration;

import jdk.jfr.Category;
import org.springframework.ai.ollama.api.OllamaApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AiApiConfiguration {


    @Bean
    public OllamaApi ollamaApi(){
        return OllamaApi.builder().build();
    }
}
