package com.lpvs.entity.dashboard;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter @Setter @AllArgsConstructor
public class Dashboard {
    private String name;
    private Map<String, Integer> licenseCountMap;

    private int totalDetectionCount;
    private int highSimilarityCount;
    private int totalIssueCount;
    private int totalParticipantsCount;
    private int totalRepositoryCount;

    private List<DashboardElementsByDate> dashboardElementsByDates;
}
