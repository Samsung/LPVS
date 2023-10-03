package com.lpvs.entity.result;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter @AllArgsConstructor
public class LPVSResultFile {

    private Long id;
    private String path;
    private String componentFileUrl;
    private String matchLine;
    private String matchValue;

    private String status; //license.licenseUsage
    private String licenseSpdx; //license
}
