package org.timetodo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LogDto {
    private Long logId;
    private Long userId;
    private String inputText;
    private String openaiResponse;
    private String processedResult;
    private LocalDateTime createdAt;
}
