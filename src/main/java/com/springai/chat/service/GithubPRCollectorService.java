package com.springai.chat.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
@Slf4j
public class GithubPRCollectorService {

    @Value("${github.token}")
    private String githubToken;

    @Value("${github.org}")
    private String org;

    @Value("${github.repo}")
    private String repo;

    private final RestTemplate restTemplate;
    private final MongoTemplate mongoTemplate;

    public GithubPRCollectorService(RestTemplate restTemplate, MongoTemplate mongoTemplate) {
        this.restTemplate = restTemplate;
        this.mongoTemplate = mongoTemplate;
    }

    /**
     * 자정시각마다 PR 수집 예시
     */
    @Scheduled(cron = "0 0 0 * * *") // 매일 00:00:00 실행
    public void collectPRs() {
        List<Map<String, Object>> prs = fetchOpenPRs();
        if (prs.isEmpty()) return;

        //MongoDB에 저장 (업데이트는 _id 기준으로 중복 방지)
        for (Map<String, Object> pr : prs) {
            // GitHub PR의 고유 ID를 MongoDB _id로 사용
            pr.put("_id", pr.get("id"));
            // 수집 시각 기록 (옵션)
            pr.put("collectedAt", new Date());

            mongoTemplate.save(pr, "meetnow");
        }

        log.info("GitHub PRs updated: " + prs.size());
    }

    /**
     * GitHub API 호출
     */
    private List<Map<String, Object>> fetchOpenPRs() {
        try {
            String url = "https://api.github.com/repos/" + org + "/" + repo + "/pulls?state=open&per_page=5";//열려 있는 PR만 조회, 한번에 가져올 pr 수 = 5
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(githubToken);
            headers.setAccept(List.of(MediaType.APPLICATION_JSON));

            HttpEntity<Void> entity = new HttpEntity<>(headers);
            ResponseEntity<List> response = restTemplate.exchange(url, HttpMethod.GET, entity, List.class);

            return response.getBody() != null ? response.getBody() : Collections.emptyList();
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }
}