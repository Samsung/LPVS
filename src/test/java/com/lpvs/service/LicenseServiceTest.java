package com.lpvs.service;

import com.lpvs.entity.LPVSFile;
import com.lpvs.entity.LPVSLicense;
import com.lpvs.entity.config.WebhookConfig;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class LicenseServiceTest {
    @Test
    public void testFindConflicts() {
        WebhookConfig webhookConfig = new WebhookConfig();
        webhookConfig.setRepositoryName("LPVS");
        webhookConfig.setRepositoryOrganization("Samsung");
        webhookConfig.setRepositoryLicense("Apache-2.0");
        webhookConfig.setPullRequestAPIUrl("http://url.com");
        LPVSFile file = new LPVSFile();
        LPVSLicense license = new LPVSLicense(){{
            setChecklist_url("");
            setAccess("unrviewed");
            setSpdxId("LPGL-2.1-or-later");
        }};
        file.setLicenses(new HashSet<LPVSLicense>(){{
            add(license);
        }});
        file.setFilePath("");
        file.setComponent("");
        file.setSnippetMatch("");
        file.setMatchedLines("");

        LPVSFile file1 = new LPVSFile();
        LPVSLicense license1 = new LPVSLicense(){{
            setChecklist_url("");
            setAccess("unrviewed");
            setSpdxId("Apache-2.0");
        }};
        file1.setLicenses(new HashSet<LPVSLicense>(){{
            add(license1);
        }});
        file1.setFilePath("");
        file1.setComponent("");
        file1.setSnippetMatch("");
        file1.setMatchedLines("");

        LPVSFile file2 = new LPVSFile();
        LPVSLicense license2 = new LPVSLicense(){{
            setChecklist_url("");
            setAccess("unrviewed");
            setSpdxId("MIT");
        }};
        file2.setLicenses(new HashSet<LPVSLicense>(){{
            add(license2);
        }});
        file2.setFilePath("");
        file2.setComponent("");
        file2.setSnippetMatch("");
        file2.setMatchedLines("");

        List<LPVSFile> fileList = new ArrayList<LPVSFile>(){{
            add(file);
            add(file1);
            add(file2);
        }};
        LicenseService licenseService = new LicenseService("", "");
        ReflectionTestUtils.setField(licenseService, "licenseConflicts",
                new ArrayList<LicenseService.Conflict<String, String>>()
                {{ add(new LicenseService.Conflict<>("", "")); }});
        Assertions.assertNotNull(licenseService.findConflicts(webhookConfig, fileList));
    }
}
