package com.springai.chat.controller;

import java.util.Map;

import com.springai.chat.dto.ChatRequest;
import com.springai.chat.dto.Response;
import com.springai.chat.service.ChatService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/chat")
@RequiredArgsConstructor
@Tag(name = "Chat API", description = "OpenAI API를 통한 채팅 기능")
public class ChatController {
    private final ChatService chatService;
    private static final Logger logger = LoggerFactory.getLogger(ChatController.class);

    @Operation(
            summary = "LLM 채팅 메시지 전송",
            description = "사용자의 메시지를 받아 OpenAI API를 통해 응답을 생성합니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "LLM 응답 성공",
                            content = @Content(schema = @Schema(implementation = ApiResponse.class))
                    ),
                    @ApiResponse(responseCode = "400", description = "잘못된 요청"),
                    @ApiResponse(responseCode = "500", description = "서버 오류")
            }
    )
    @PostMapping("/generate")
    public ResponseEntity<Response<Map<String, Object>>> sendMessage(
            @Parameter(description = "채팅 요청 객체", required = true)
            @RequestBody ChatRequest request) {

        logger.info("Chat API 요청 받음: model={}", request.getModel());

        if (request.getQuery() == null || request.getQuery().isBlank()) {
            logger.warn("빈 질의가 요청됨");
            return ResponseEntity.badRequest().body(
                    new Response<>(false, null, "질의가 비어있습니다.")
            );
        }

        try {
            String systemMessage = "You are a helpful AI assistant.";

            var response = chatService.openAiChat(
                    request.getQuery(),
                    systemMessage,
                    request.getModel()
            );

            logger.debug("LLM 응답 생성: {}", response);

            if (response != null) {
                return ResponseEntity.ok(
                        // getText() 대신 getContent() 사용
                        new Response<>(true, Map.of("answer", response.getResult().getOutput().getContent()), null)
                );
            } else {
                logger.error("LLM 응답 생성 실패");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                        new Response<>(false, null, "LLM 응답 생성 중 오류 발생")
                );
            }
        } catch (Exception e) {
            logger.error("Chat API 처리 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    new Response<>(false, null, e.getMessage() != null ? e.getMessage() : "알 수 없는 오류 발생")
            );
        }
    }
}