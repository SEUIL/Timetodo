package org.timetodo.ai;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class OpenAiClient {

    private static final String CHAT_COMPLETIONS_URL = "https://api.openai.com/v1/chat/completions";
    private static final String MODEL = "gpt-3.5-turbo";

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${openai.api.key}")
    private String apiKey;

    public String classify(String userInput) {
        Map<String, Object> requestBody = baseRequest(List.of(
                Map.of("role", "system", "content",
                        "너는 사용자의 입력을 다음 네 가지로 분류하는 시스템이야:\\n\\n" +
                                "1. 새로운 일정(Calendar)을 추가하는 내용.\n" +
                                "2. 새로운 할일(Task)을 추가하는 내용.\n" +
                                "3. 단순히 알람(Alarm)을 추가하는 내용.\n" +
                                "4. 일정(Calendar)이나 할일(Task)에 대한 알림(Reminder)을 추가하는 내용.\n\n" +
                                "위 입력을 네 가지 중 하나로 정확히 분류해서 다음 값 중 하나로만 응답해:\n" +
                                "- \"calendar\": 일정(Calendar)\n" +
                                "- \"task\": 할일(Task)\n" +
                                "- \"alarm\": 단순 알람\n" +
                                "- \"selectionData\": 일정이나 할일에 대한 알림"
                ),
                Map.of("role", "user", "content", userInput)
        ));

        String responseBody = post(requestBody);
        try {
            JsonNode rootNode = objectMapper.readTree(responseBody);
            return rootNode.path("choices").get(0).path("message").path("content").asText().trim();
        } catch (JsonProcessingException e) {
            throw new RuntimeException("OpenAI 분류 응답 파싱 실패", e);
        }
    }

    public String createScheduleJson(String input) {
        Map<String, Object> requestBody = baseRequest(List.of(
                Map.of("role", "system", "content",
                        "사용자의 요청을 JSON으로 처리하세요. 마지막 항목 뒤에 쉼표를 포함하지 마세요. 아래와 같은 구조를 따르십시오:\n" +

                                "- `calendar` 요청의 경우:\n" +
                                "{\n" +
                                "    \"type\": \"calendar\",\n" +
                                "    \"title\": \"\",\n" +
                                "    \"description\": \"\",\n" +
                                "    \"location\": \"\",\n" +
                                "    \"repeatType\": \"\",\n" +
                                "    \"startTime\": \"\",\n" +
                                "    \"endTime\": \"\"\n" +
                                "}\n" +
                                "  * `title`, `description`, `location`, `repeatType`, `startTime`, `endTime`를 반드시 포함하세요.\n" +
                                "  * `repeatType`은 반복 없음, 매일, 매주, 매월, 매년 중 하나로 설정하세요.\n" +
                                "  * `startTime`과 `endTime`은 ISO 8601 LocalDateTime 형식(`yyyy-MM-dd'T'HH:mm:ss`)으로 반환하세요.\n" +
                                "  * `endTime`의 값이 없을 경우 `startTime`에서 1시간을 더한 값을 반환하세요. \n" +

                                "- `task` 요청의 경우:\n" +
                                "{\n" +
                                "    \"type\": \"task\",\n" +
                                "    \"title\": \"\",\n" +
                                "    \"dueDate\": \"\",\n" +
                                "    \"priority\": \"\",\n" +
                                "    \"status\": \"\",\n" +
                                "    \"repeatType\": \"\"\n" +
                                "}\n" +
                                "  * `title`, `dueDate`, `priority`, `status`, `repeatType`를 반드시 포함하세요.\n" +
                                "  * `dueDate`는 ISO 8601 LocalDateTime 형식(`yyyy-MM-dd'T'HH:mm:ss`)으로 반환하세요, 만약 입력이 안되었다면 2000년 01월01일 00시로 설정해주라. \n" +
                                "  * `priority`는 우선순위 없음, 중요, 일반 중 하나로 설정하세요. 기본값은 우선순위 없음 입니다.\n" +
                                "  * `status`는 보류 중, 진행중, 완료 중 하나로 설정하세요, 기본값은 보류 중 입니다.\n" +
                                "  * `repeatType`은 반복 없음, 매일, 매주, 매월, 매년 중 하나로 설정하세요, 기본값은 반복 없음 입니다.\n" +

                                "- `reminder` 요청의 경우:\n" +
                                "{\n" +
                                "    \"type\": \"reminder\",\n" +
                                "    \"id\": \"1\",\n" +
                                "    \"detailsType\": \"task\",\n" +
                                "    \"notificationsEnabled\": true,\n" +
                                "    \"timeBefore\": 10,\n" +
                                "    \"repeats\": false\n" +
                                "}\n" +
                                "  * `id`는 번호를 의미하는 값으로 반환하세요.\n" +
                                "  * `detailsType`는 일정이라는 단어가 있을경우 calendar 로 반환하고, 할일이라는 단어가 있을경우 task 로 반환하세요.\n" +
                                "  * `notificationsEnabled`는 boolean(true/false) 값으로 반환하세요.\n" +
                                "  * `timeBefore`는 정수 값(분 단위)으로 반환하세요.\n" +
                                "  * `repeats`는 boolean(true/false) 값으로 반환하세요.\n" +
                                "사용자가 알림 설정을 요청할 경우 `reminder` 타입(type)으로 처리하고, 필요한 필드 값을 포함하여 응답하세요." +

                                "모든 JSON은 잘 구성된 형식으로 반환해야 하며, 마지막 항목 뒤에 쉼표를 포함하지 마세요."
                ),
                Map.of("role", "user", "content", input)
        ));

        return post(requestBody);
    }

    private Map<String, Object> baseRequest(List<Map<String, String>> messages) {
        return Map.of(
                "model", MODEL,
                "messages", messages,
                "max_tokens", 150,
                "temperature", 0.7
        );
    }

    private String post(Map<String, Object> requestBody) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        HttpEntity<String> request = new HttpEntity<>(toJson(requestBody), headers);
        ResponseEntity<String> response = restTemplate.postForEntity(CHAT_COMPLETIONS_URL, request, String.class);
        return response.getBody();
    }

    private String toJson(Map<String, Object> requestBody) {
        try {
            return objectMapper.writeValueAsString(requestBody);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("OpenAI 요청 JSON 생성 실패", e);
        }
    }
}
