package com.lpvs.entity.result;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter @AllArgsConstructor
public class LPVSResultFile {

    private Long id; //dl.id
    private String path; //dl.file_path
    private String componentFileUrl; //dl
    private String matchLine; //dl
    private String matchValue; //dl

    private String status; //license.licenseUsage
    private String licenseSpdx; //license
}
