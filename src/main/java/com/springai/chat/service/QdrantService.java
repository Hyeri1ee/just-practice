package com.springai.chat.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import java.util.*;
/*
메서드 요약

createCollectionIfNotExists	컬렉션 생성 (없으면 새로 만듦)
upsertPoints	벡터와 메타데이터를 컬렉션에 넣음
search	입력 벡터와 가장 유사한 벡터를 검색
 */

@Service
@Transactional
public class QdrantService {

    @Value("${qdrant.base-url}")
    private String qdrantUrl;

    @Value("${qdrant.collection-name}")
    private String collectionName;

    @Value("${qdrant.vector-size}")
    private int vectorSize;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public QdrantService(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    /**
     * 컬렉션(벡터 저장소) 생성
     */
    public void createCollectionIfNotExists() {
        try {
            // GET /collections 로 확인 or 그냥 PUT 시도
            String url = qdrantUrl + "/collections/" + collectionName;

            // {
            //   "vectors": {
            //       "size": 1536,
            //       "distance": "Cosine"
            //   }
            // }
            Map<String, Object> vectorsMap = new HashMap<>();
            vectorsMap.put("size", vectorSize);
            vectorsMap.put("distance", "Cosine");

            Map<String, Object> body = new HashMap<>();
            body.put("vectors", vectorsMap);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
            restTemplate.put(url, entity);  //PUT 요청
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Qdrant 업서트(데이터 넣음)
     * points: (id, vector, payload)
     */
    public void upsertPoints(List<Map<String, Object>> points) {
        try {
            String url = qdrantUrl + "/collections/" + collectionName + "/points?wait=true";

            // body = {
            //   "points": [
            //      { "id": 1, "vector": [...], "payload": {...} },
            //      ...
            //   ]
            // }
            Map<String, Object> body = new HashMap<>();
            body.put("points", points);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
            restTemplate.put(url, entity, String.class);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 검색
     * @param queryVector 검색할 벡터
     * @param limit 상위 개수
     * @return 검색 결과 JSON
     */
    public String search(List<Double> queryVector, int limit) {
        try {
            String url = qdrantUrl + "/collections/" + collectionName + "/points/search";

            Map<String, Object> body = new HashMap<>();
            body.put("vector", queryVector);
            body.put("limit", limit);
            body.put("with_payload", true);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
            ResponseEntity<String> resp = restTemplate.postForEntity(url, entity, String.class);
            return resp.getBody(); //JSON 스트링
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public Set<String> getAllPointIds() {
        try {
            String url = qdrantUrl + "/collections/" + collectionName + "/points?limit=10000"; //필요시 조정
            ResponseEntity<String> resp = restTemplate.getForEntity(url, String.class);

            Set<String> ids = new HashSet<>();
            if (resp.getBody() != null) {
                var root = objectMapper.readTree(resp.getBody());
                var pointsNode = root.path("result").path("points");
                if (pointsNode.isArray()) {
                    for (var pointNode : pointsNode) {
                        ids.add(pointNode.path("id").asText());
                    }
                }
            }
            return ids;
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptySet();
        }
    }
}
