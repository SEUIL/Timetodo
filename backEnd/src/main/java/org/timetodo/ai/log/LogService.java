package org.timetodo.ai.log;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.timetodo.dto.LogDto;
import org.timetodo.entity.LogEntity;
import org.timetodo.repository.LogRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LogService {
    private final LogRepository logRepository;

    public void saveLog(Long userId, String input, String response, String result) {
        LogEntity log = new LogEntity();
        log.setUserId(userId);
        log.setInputText(input);
        log.setOpenaiResponse(response);
        log.setProcessedResult(result);
        logRepository.save(log);
    }

    public List<LogDto> getLogsByUserId(Long userId) {
        return logRepository.findByUserId(userId).stream()
                .map(this::convertToDto)
                .toList();
    }

    private LogDto convertToDto(LogEntity log) {
        return new LogDto(
                log.getLogId(),
                log.getUserId(),
                log.getInputText(),
                log.getOpenaiResponse(),
                log.getProcessedResult(),
                log.getCreatedAt()
        );
    }
}
