package com.sniplink.repository;

import com.sniplink.entity.ClickLog;
import com.sniplink.entity.URL;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ClickLogRepository extends JpaRepository<ClickLog, Long> {
    
    List<ClickLog> findByUrl(URL url);
    
    List<ClickLog> findByUrlOrderByTimestampDesc(URL url);
    
    @Query("SELECT c FROM ClickLog c WHERE c.url.user.id = :userId AND c.timestamp BETWEEN :startDate AND :endDate")
    List<ClickLog> findClicksByUserAndDateRange(Long userId, LocalDateTime startDate, LocalDateTime endDate);
    
    @Query("SELECT DATE(c.timestamp) as date, COUNT(c) as count FROM ClickLog c WHERE c.url.user.id = :userId GROUP BY DATE(c.timestamp) ORDER BY DATE(c.timestamp) DESC")
    List<Object[]> getDailyClicksByUser(Long userId);
}
