package com.lpvs.entity.result;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Setter
@Getter
@AllArgsConstructor
public class LPVSResult {
    private List<LPVSResultFile> lpvsResultFileList;
    private LPVSResultInfo lpvsResultInfo;
    private Long count;

    private Map<String, Integer> licenseCountMap;

    private String pullNumber;
    
    private Boolean hasIssue;
}
