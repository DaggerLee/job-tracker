package com.jobtracker.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jobtracker.dto.AiAnalysisRequest;
import com.jobtracker.dto.AiAnalysisResponse;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ai")
@CrossOrigin(origins = "http://localhost:5173")
public class AiController {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @PostMapping("/analyze")
    public ResponseEntity<?> analyze(@RequestBody AiAnalysisRequest request) {
        try {
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.set("x-api-key", request.getApiKey());
            headers.set("anthropic-version", "2023-06-01");
            headers.setContentType(MediaType.APPLICATION_JSON);

            String prompt = String.format(
                "Analyze the match between the resume and job description below.\n\n" +
                "RESUME:\n%s\n\nJOB DESCRIPTION:\n%s\n\n" +
                "Respond with ONLY a valid JSON object (no markdown code blocks, no explanation, raw JSON only):\n" +
                "{\"matchedSkills\": [\"skill1\", \"skill2\"], \"missingSkills\": [\"skill3\"], \"score\": 75, \"summary\": \"Brief analysis\"}",
                request.getResumeText(), request.getJdText()
            );

            Map<String, Object> message = new HashMap<>();
            message.put("role", "user");
            message.put("content", prompt);

            Map<String, Object> body = new HashMap<>();
            body.put("model", "claude-sonnet-4-20250514");
            body.put("max_tokens", 1024);
            body.put("messages", List.of(message));

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
            ResponseEntity<Map> response = restTemplate.postForEntity(
                "https://api.anthropic.com/v1/messages", entity, Map.class);

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> content = (List<Map<String, Object>>) response.getBody().get("content");
            String jsonText = ((String) content.get(0).get("text")).strip();

            // Strip markdown code blocks if present
            if (jsonText.startsWith("```")) {
                jsonText = jsonText.replaceAll("(?s)```[a-z]*\\n?", "").strip();
            }

            AiAnalysisResponse result = objectMapper.readValue(jsonText, AiAnalysisResponse.class);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }
}
