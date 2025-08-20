package com.springai.chat.service;

import java.util.Arrays;
import java.util.List;

import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatService {

    private final OpenAiChatModel chatModel;

    public ChatResponse openAiChat(String userInput, String systemMessage, String model) {
        log.debug("Groq 챗 호출 시작 - 모델: {}", model);

        try {
            String groqModel = validateAndConvertModel(model);
            log.info("변환된 모델: {} -> {}", model, groqModel);

            // 메시지 구성
            List<Message> messages = Arrays.asList(
                    new SystemMessage(systemMessage),
                    new UserMessage(userInput)
            );

            // 챗 옵션 구성
            OpenAiChatOptions chatOptions = OpenAiChatOptions.builder()
                    .withModel(groqModel)
                    .withTemperature(0.7)
                    .withMaxTokens(1000)
                    .build();

            // 프롬프트 생성
            Prompt prompt = new Prompt(messages, chatOptions);

            return chatModel.call(prompt);

        } catch (Exception e) {
            log.error("Groq 챗 호출 중 오류 발생: {}", e.getMessage(), e);
            return null;
        }
    }

    private String validateAndConvertModel(String model) {
        log.info("요청된 모델: {}", model);  // 로그 추가

        // Groq에서 사용 가능한 모델들
        switch (model.toLowerCase()) {
            case "gpt-3.5-turbo":
            case "llama3-8b":
            case "llama3-8b-8192":
                return "llama3-8b-8192";
            case "gpt-4":
            case "llama3-70b":
            case "llama3-70b-8192":
                return "llama3-70b-8192";
            case "mixtral":
            case "mixtral-8x7b-32768":
                return "mixtral-8x7b-32768";
            default:
                log.warn("알 수 없는 모델: {}. 기본 모델 사용", model);
                return "llama3-8b-8192";
        }
    }
}