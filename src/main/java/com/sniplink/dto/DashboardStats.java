package com.sniplink.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStats {
    private Long totalLinks;
    private Long totalClicks;
    private Long activeLinks;
    private List<URLResponse> topLinks;
    private Map<String, Long> dailyClicks;
}
