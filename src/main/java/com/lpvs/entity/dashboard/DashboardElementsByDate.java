package com.lpvs.entity.dashboard;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;
import java.util.Map;

@Getter @AllArgsConstructor
public class DashboardElementsByDate {
    private LocalDate date;
    private int participantCount;
    private int pullRequestCount;
    private Map<Grade, Integer> riskGradeMap;
}
