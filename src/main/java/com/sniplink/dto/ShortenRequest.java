package com.sniplink.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.hibernate.validator.constraints.URL;

import java.time.LocalDateTime;

@Data
public class ShortenRequest {
    
    @NotBlank(message = "URL is required")
    @URL(message = "Invalid URL format")
    private String url;
    
    private String customSlug;
    
    private LocalDateTime expiresAt;
    
    private String password;
    
    private String tags;

    public void setTags(Object tags) {
        if (tags instanceof String) {
            this.tags = (String) tags;
        } else if (tags instanceof java.util.List) {
            this.tags = String.join(", ", (java.util.List<String>) tags);
        }
    }
}
