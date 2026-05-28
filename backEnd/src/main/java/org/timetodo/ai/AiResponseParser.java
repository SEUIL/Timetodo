package org.timetodo.ai;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class AiResponseParser {

    private final ObjectMapper mapper = new ObjectMapper();

    public List<Map<String, Object>> parseScheduleItems(String response) {
        try {
            String sanitizedResponse = sanitizeJson(response);

            JsonNode rootNode = mapper.readTree(sanitizedResponse);
            JsonNode choicesNode = rootNode.path("choices");

            if (!choicesNode.isArray() || choicesNode.isEmpty()) {
                log.error("OpenAI 응답이 비어 있음: {}", response);
                throw new RuntimeException("OpenAI 응답이 비어있습니다.");
            }

            String content = choicesNode.get(0).path("message").path("content").asText();
            log.info("추출된 content: {}", content);

            JsonNode contentNode = mapper.readTree(sanitizeJson(content));
            if (contentNode.isArray()) {
                return mapper.convertValue(contentNode, new TypeReference<List<Map<String, Object>>>() {});
            }
            if (contentNode.isObject()) {
                Map<String, Object> singleResult = mapper.convertValue(contentNode, new TypeReference<Map<String, Object>>() {});
                return List.of(singleResult);
            }
            throw new RuntimeException("응답 파싱 실패: 예상치 못한 데이터 구조입니다.");
        } catch (Exception e) {
            log.error("응답 파싱 실패: {}", response, e);
            throw new RuntimeException("응답 파싱 실패: " + e.getMessage(), e);
        }
    }

    private String sanitizeJson(String json) {
        return json.replaceAll(",\\s*}", "}").replaceAll(",\\s*]", "]");
    }
}
