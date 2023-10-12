package com.lpvs.entity.dashboard;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.Map;

@Getter @Setter @AllArgsConstructor
public class DashBoardElements {
    private Map<LocalDate, DashboardElementsByDate> dashboardElementsMap;
}
