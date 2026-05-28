package org.timetodo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.timetodo.entity.LogEntity;

import java.util.List;

@Repository
public interface LogRepository extends JpaRepository<LogEntity, Long> {
    List<LogEntity> findByUserId(Long userId);
}
