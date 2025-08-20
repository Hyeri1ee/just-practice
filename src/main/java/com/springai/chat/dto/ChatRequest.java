package com.springai.chat.dto;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Schema(description = "채팅 요청 데이터 모델")
public class ChatRequest {

    @Schema(description = "사용자 질문", example = "안녕하세요")
    private String query;

    @Schema(description = "사용할 LLM 모델", example = "llama3-70b-8192", defaultValue = "llama3-70b-8192")
    private String model = "llama3-70b-8192";
}