package org.timetodo.ai;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.timetodo.ai.log.LogService;

import java.util.List;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@Service
public class OpenAIService { //사용자 입력 파실 및 처리

    private final LogService logService;
    private final OpenAiClient openAiClient;
    private final AiResponseParser aiResponseParser;
    private final AiCommandService aiCommandService;
    private final ScheduleCommandService scheduleCommandService;

    /**
     * OpenAI API를 호출해 사용자의 입력을 다음 네 가지로 분류하는 메서드
     * @param userInput
     */
    public String OpenAIAPI_SelectProcessing(String userInput, Long id) {
        String response = "";
        try {
            String content = openAiClient.classify(userInput);

            if (content.contains("calendar") || content.contains("task") || content.contains("selectionData")) {
                response = processChatInput(userInput,id);
                return response;
            } else if (content.contains("alarm")) {
                response = handleReminderSelectionFlow(userInput, id);
                return response;
            }
        } catch (Exception e) {
            // 에러 발생 시 로깅 및 기본 응답
            log.error(e.getMessage());
            return ("OpenAIAPI_SelectProcessing 실행 실패");
        }
        return response;
    }


    /**
     * 사용자의 자연어 입력을 처리하는 메서드
     * @param userInput 사용자가 입력한 텍스트
     * @param userId 사용자 고유 ID
     * @return 처리 결과 메시지
     */
    public String processChatInput(String userInput, Long userId) {
        log.info("User input: {}", userInput);

        // OpenAI API 호출
        String openAIResponse = openAiClient.createScheduleJson(userInput);
        log.info("OpenAI response: {}", openAIResponse);

        String resultMessage = "";

        try {
            // OpenAI 응답 파싱 : 응답 데이터를 리스트 형태로 변환
            List<Map<String, Object>> responseList = aiResponseParser.parseScheduleItems(openAIResponse);

            aiCommandService.execute(responseList, userId);
            resultMessage = "요청이 성공적으로 처리되었습니다.";
        } catch (Exception e) {
            log.error("Error processing input", e);
            resultMessage = "처리 중 오류가 발생했습니다.";
        }

        // 처리 결과 로그 저장
        logService.saveLog(userId, userInput, openAIResponse, resultMessage);

        return resultMessage;
    }


    /**
     * Calendar와 Task를 가시성 좋게 불러오는 메서드
     * @param data
     * @param userId
     */
    public String handleReminderSelectionFlow(String data, Long userId) {
        return scheduleCommandService.buildReminderSelection(userId);
    }

    /**
     * Reminder 데이터를 처리하고 저장하는 메서드
     * @param selectionData 파싱된 Reminder 데이터
     * @param userId 사용자 고유 ID
     * @return 처리 결과 메시지
     */
    public String handleReminder(Map<String, Object> selectionData, Long userId) {
        return scheduleCommandService.createReminder(selectionData, userId);
    }
}
