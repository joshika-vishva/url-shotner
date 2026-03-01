package com.sniplink.repository;

import com.sniplink.entity.URL;
import com.sniplink.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface URLRepository extends JpaRepository<URL, Long> {
    
    Optional<URL> findByShortCode(String shortCode);
    
    Boolean existsByShortCode(String shortCode);
    
    List<URL> findByUser(User user);
    
    List<URL> findByUserOrderByCreatedAtDesc(User user);
    
    @Query("SELECT u FROM URL u WHERE u.user = :user ORDER BY u.clickCount DESC")
    List<URL> findTopUrlsByUser(User user);
    
    Long countByUser(User user);
    
    @Query("SELECT SUM(u.clickCount) FROM URL u WHERE u.user = :user")
    Long getTotalClicksByUser(User user);
}
