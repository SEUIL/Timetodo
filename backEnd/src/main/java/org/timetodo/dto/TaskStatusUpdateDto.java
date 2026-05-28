package org.timetodo.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TaskStatusUpdateDto {
    private Long taskId;

    @NotBlank(message = "상태는 필수입니다.")
    private String status;
}
