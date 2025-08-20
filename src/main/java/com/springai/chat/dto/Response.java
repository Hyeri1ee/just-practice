package com.springai.chat.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@Schema(description = "API 응답 포맷")
public class Response<T> {

    @Schema(description = "요청 처리 성공 여부")
    private boolean success;

    @Schema(description = "성공 응답 데이터")
    private T data;

    @Schema(description = "실패 오류 메시지")
    private String error;
}
