package org.timetodo.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Entity(name = "calendar_entity")
@NoArgsConstructor
@AllArgsConstructor
public class CalendarEntity {

    @Column(name = "calendar_id")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long calendarId; // 캘린더 고유 ID (Primary Key)

    //@Column(nullable = false, length = 100)
    //@Column(nullable = false)
    private String title; // 일정 제목

    //@Column(columnDefinition = "TEXT")
    private String description; // 일정 설명

    //@Column(nullable = false)
    private LocalDateTime startTime; // 일정 시작 시간

    @Column(nullable = true)
    private LocalDateTime endTime; // 일정 종료 시간

    //@Column(nullable = false, length = 100)
    //@Column(nullable = false)
    private String location; // 일정 장소

    @Column(nullable = true)
    private String repeatType; // 반복 일정 유형 (예: daily, weekly)

    @ManyToOne // 여러 Calendar가 하나의 Category와 연관 (N:1 관계)
    @JoinColumn(name = "category_id")
    @JsonIgnore
    @ToString.Exclude // Lombok 사용 시 순환 참조 방지
    private CategoryEntity category;

    @ManyToOne(fetch = FetchType.EAGER) // 여러 Calendar가 하나의 User와 연관 (N:1 관계)
    @JoinColumn(name = "user_id")
    @JsonIgnore
    @ToString.Exclude
    private UserEntity user;

    @OneToMany(mappedBy = "calendar")
    @JsonIgnore
    @ToString.Exclude
    private List<ReminderEntity> reminders;

}
