package com.springai.chat.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@Transactional
public class RagService {

    private final OpenAiEmbeddingService embeddingService;
    private final QdrantService qdrantService;

    public RagService(OpenAiEmbeddingService embeddingService,
                      QdrantService qdrantService) {
        this.embeddingService = embeddingService;
        this.qdrantService = qdrantService;
    }

    /**
     * MongoDB에서 가져온 PR 데이터를 업로드
     * @param prDocuments MongoDB에서 가져온 PR JSON 문서 리스트
     */
    public void uploadPRs(List<Map<String, Object>> prDocuments) {
        List<Map<String, Object>> points = new ArrayList<>();
        AtomicInteger idCounter = new AtomicInteger(1);

        //1) Qdrant에 이미 존재하는 PR ID 조회
        Set<String> existingIds = qdrantService.getAllPointIds();

        for (Map<String, Object> prDoc : prDocuments) {
            String prId = Optional.ofNullable(prDoc.get("id")).map(Object::toString).orElse(null);
            if (prId == null || existingIds.contains(prId)) {
                continue; //이미 존재하거나 id가 없는 경우 제외
            }

            //PR 문서에서 검색용 텍스트 추출
            String combinedText = extractTextFromPR(prDoc);

            //임베딩
            List<Double> vector = embeddingService.getEmbedding(combinedText);
            if (vector == null) continue;

            Map<String, Object> point = new HashMap<>();
            point.put("id", prId);
            point.put("vector", vector);

            //payload: 원본 PR 문서와 combined_text 포함
            Map<String, Object> payload = new HashMap<>();
            payload.put("combined_text", combinedText);
            payload.put("pr_data", prDoc);
            point.put("payload", payload);

            points.add(point);
        }

        // Qdrant 업서트
        if (!points.isEmpty()) {
            qdrantService.upsertPoints(points);
        }
    }

    /**
     * PR 문서에서 텍스트 추출
     * MongoDB에 저장된 비정형 JSON에서 필요한 필드만 합치기
     */
    private String extractTextFromPR(Map<String, Object> prDoc) {
        // 예: "title", "body", "status" 필드 사용
        String title = Optional.ofNullable(prDoc.get("title")).map(Object::toString).orElse("");
        String body = Optional.ofNullable(prDoc.get("body")).map(Object::toString).orElse("");
        String status = Optional.ofNullable(prDoc.get("status")).map(Object::toString).orElse("");

        return String.join(" ", title, body, status).trim();
    }

    /**
     * 질문 검색
     */
    public String askQuestion(String question, int topK) {
        List<Double> queryVec = embeddingService.getEmbedding(question);
        if (queryVec == null) return "Embedding failed or null";

        return qdrantService.search(queryVec, topK);
    }
}
