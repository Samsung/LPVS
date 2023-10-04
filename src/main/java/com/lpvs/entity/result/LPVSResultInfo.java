package com.lpvs.entity.result;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.List;

@Getter @Setter @AllArgsConstructor
public class LPVSResultInfo {

    private Long id;
    private Date scanDate;
    private String repositoryName;
    private String status;
    private List<String> detectedLicenses;
}
