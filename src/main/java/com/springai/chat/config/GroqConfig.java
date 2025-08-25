package com.springai.chat.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
@Slf4j
public class GroqConfig {

    @Value("${spring.ai.groq.api-key}")
    private String groqApiKey;

    @Bean
    @Primary
    public OpenAiChatModel groqChatModel() {
        if (groqApiKey == null || groqApiKey.isEmpty()) {
            throw new IllegalStateException("GROQ_API_KEY 환경변수가 설정되지 않았습니다.");
        }

        log.info("Groq ChatModel 생성 중");
        log.info("API Key 시작: {}...", groqApiKey.substring(0, Math.min(10, groqApiKey.length())));
        log.info("Base URL: https://api.groq.com/openai/v1");

        var openAiApi = new OpenAiApi("https://api.groq.com/openai", groqApiKey);

        return new OpenAiChatModel(openAiApi);
    }
}