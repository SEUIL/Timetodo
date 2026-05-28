package org.timetodo.ai;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiCommandService {

    private final ScheduleCommandService scheduleCommandService;

    public void execute(List<Map<String, Object>> items, Long userId) {
        for (Map<String, Object> item : items) {
            String type = safeGet(item, "type", "unknown");
            log.info("파싱 데이터 타입 : Parsed type: {}", type);

            switch (type) {
                case "calendar" -> scheduleCommandService.createCalendar(item, userId);
                case "task" -> scheduleCommandService.createTask(item, userId);
                case "reminder" -> scheduleCommandService.createReminder(item, userId);
                default -> {
                    log.warn("알 수 없는 데이터 타입으로 파싱됨 : Unhandled type: {}", type);
                    log.debug("디버깅용 전체 데이터 출력, Unhandled item: {}", item);
                }
            }
        }
    }

    private String safeGet(Map<String, Object> data, String key, String defaultValue) {
        Object value = data.get(key);
        return value == null ? defaultValue : String.valueOf(value);
    }
}
