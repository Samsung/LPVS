package com.lpvs.entity.result;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.List;

@Getter @Setter @AllArgsConstructor
public class LPVSResultInfo {

    private Long id; //pr.id
    private Date scanDate; //pr
    private String repositoryName; //pr
    private String status; //pr
    private List<String> detectedLicenses;
}
