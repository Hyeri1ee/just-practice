package com.springai.chat.config;

import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LLMConfig {

    @Value("${spring.ai.openai.api-key}")
    private String openaiApiKey;

    @Bean
    public OpenAiApi openAiApi() {
        return new OpenAiApi(openaiApiKey);
    }

    @Bean
    public OpenAiChatOptions openAiChatOptions() {
        return OpenAiChatOptions.builder()
                .withModel("gpt-4o-mini")
                .withTemperature(0.0)//RAG를 사용할때는 temperature(생성시 다양성 낮추기) 낮춰야함
                .withMaxCompletionTokens(1024)
                .withTopP(1.0)//높을수록 반응이 다양하며. 낮을 수록 정확 사실적.
                .withFrequencyPenalty(0.0)
                .withPresencePenalty(0.0)
                .build();
    }

    @Bean
    public OpenAiChatModel chatModel(OpenAiApi openAiApi, OpenAiChatOptions openAiChatOptions) {
        return new OpenAiChatModel(openAiApi, openAiChatOptions);
    }
}