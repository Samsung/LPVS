/**
 * Copyright (c) 2022, Samsung Electronics Co., Ltd. All rights reserved.
 *
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */

package com.lpvs.service;

import com.lpvs.entity.*;
import com.lpvs.entity.LPVSQueue;
import com.lpvs.entity.enums.LPVSPullRequestStatus;
import com.lpvs.entity.enums.LPVSVcs;
import com.lpvs.repository.LPVSDetectedLicenseRepository;
import com.lpvs.repository.LPVSLicenseConflictRepository;
import com.lpvs.repository.LPVSLicenseRepository;
import com.lpvs.repository.LPVSPullRequestRepository;
import com.lpvs.util.LPVSCommentUtil;
import com.lpvs.util.LPVSExitHandler;
import com.lpvs.util.LPVSFileUtil;
import com.lpvs.util.LPVSWebhookUtil;
import lombok.extern.slf4j.Slf4j;
import org.kohsuke.github.GitHub;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GHPullRequest;
import org.kohsuke.github.GHLicense;
import org.kohsuke.github.GHIssueState;
import org.kohsuke.github.GHCommitState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class LPVSGitHubService {

    private String GITHUB_LOGIN;
    private String GITHUB_AUTH_TOKEN;
    private String GITHUB_API_URL;

    private final static String GITHUB_LOGIN_PROP_NAME = "github.login";
    private final static String GITHUB_AUTH_TOKEN_PROP_NAME = "github.token";
    private final static String GITHUB_API_URL_PROP_NAME = "github.api.url";

    private final static String GITHUB_LOGIN_ENV_VAR_NAME = "LPVS_GITHUB_LOGIN";
    private final static String GITHUB_AUTH_TOKEN_ENV_VAR_NAME = "LPVS_GITHUB_TOKEN";
    private final static String GITHUB_API_URL_ENV_VAR_NAME = "LPVS_GITHUB_API_URL";

    @Autowired
    private LPVSPullRequestRepository pullRequestRepository;

    @Autowired
    private LPVSDetectedLicenseRepository lpvsDetectedLicenseRepository;

    @Autowired
    private LPVSLicenseRepository lpvsLicenseRepository;

    @Autowired
    private LPVSLicenseConflictRepository lpvsLicenseConflictRepository;

    private LPVSExitHandler exitHandler;

    public LPVSGitHubService(@Value("${" + GITHUB_LOGIN_PROP_NAME + "}") String GITHUB_LOGIN,
                             @Value("${" + GITHUB_AUTH_TOKEN_PROP_NAME + "}") String GITHUB_AUTH_TOKEN,
                             @Value("${" + GITHUB_API_URL_PROP_NAME + "}") String GITHUB_API_URL,
                             LPVSPullRequestRepository pullRequestRepository,
                             LPVSDetectedLicenseRepository lpvsDetectedLicenseRepository,
                             LPVSLicenseRepository lpvsLicenseRepository,
                             LPVSLicenseConflictRepository lpvsLicenseConflictRepository,
                             LPVSExitHandler exitHandler) {
        this.GITHUB_LOGIN = Optional.ofNullable(GITHUB_LOGIN).filter(s -> !s.isEmpty())
                .orElse(Optional.ofNullable(System.getenv(GITHUB_LOGIN_ENV_VAR_NAME)).orElse(""));
        this.GITHUB_AUTH_TOKEN = Optional.ofNullable(GITHUB_AUTH_TOKEN).filter(s -> !s.isEmpty())
                .orElse(Optional.ofNullable(System.getenv(GITHUB_AUTH_TOKEN_ENV_VAR_NAME)).orElse(""));
        this.GITHUB_API_URL = Optional.ofNullable(GITHUB_API_URL).filter(s -> !s.isEmpty())
                .orElse(Optional.ofNullable(System.getenv(GITHUB_API_URL_ENV_VAR_NAME)).orElse(""));
        this.exitHandler = exitHandler;
        this.pullRequestRepository = pullRequestRepository;
        this.lpvsDetectedLicenseRepository = lpvsDetectedLicenseRepository;
        this.lpvsLicenseRepository = lpvsLicenseRepository;
        this.lpvsLicenseConflictRepository = lpvsLicenseConflictRepository;
    }

    @PostConstruct
    private void checks() throws Exception {
        if (this.GITHUB_AUTH_TOKEN.isEmpty()) {
            log.error(GITHUB_AUTH_TOKEN_ENV_VAR_NAME + "(" + GITHUB_AUTH_TOKEN_PROP_NAME + ") is not set.");
            exitHandler.exit(-1);
        }
    }

    private static GitHub gitHub;

    public String getPullRequestFiles (LPVSQueue webhookConfig) {
        try {
            if (GITHUB_AUTH_TOKEN.isEmpty()) setGithubTokenFromEnv();
            if (GITHUB_API_URL.isEmpty()) gitHub = GitHub.connect(GITHUB_LOGIN, GITHUB_AUTH_TOKEN);
            else gitHub = GitHub.connectToEnterpriseWithOAuth(GITHUB_API_URL, GITHUB_LOGIN, GITHUB_AUTH_TOKEN);
            log.debug("Repository Info: " + LPVSWebhookUtil.getRepositoryOrganization(webhookConfig)+"/" + LPVSWebhookUtil.getRepositoryName(webhookConfig));
            GHRepository repository = gitHub.getRepository(LPVSWebhookUtil.getRepositoryOrganization(webhookConfig)+"/" + LPVSWebhookUtil.getRepositoryName(webhookConfig));

            GHPullRequest pullRequest = getPullRequest(webhookConfig, repository);
            if (pullRequest == null){
                log.error("Can't find pull request " + webhookConfig.getPullRequestUrl());
                return null;
            }
            log.debug("Saving files...");
            return LPVSFileUtil.saveGithubDiffs(pullRequest.listFiles(), webhookConfig);
        } catch (IOException e){
            log.error("Can't authorize getPullRequestFiles() " + e);
        }
        return null;
    }

    private GHPullRequest getPullRequest(LPVSQueue webhookConfig, GHRepository repository){
        try {
            List<GHPullRequest> pullRequests = repository.getPullRequests(GHIssueState.OPEN);
            for (GHPullRequest pullRequest : pullRequests) {
                if (null != pullRequest.getUrl()) {
                    log.debug("Pull request check: " + pullRequest.getUrl().toString() + " / " + webhookConfig.getPullRequestAPIUrl());
                    if (pullRequest.getUrl().toString().equals(webhookConfig.getPullRequestAPIUrl())){
                        log.debug("Return pull request " + pullRequest.getDiffUrl());
                        return pullRequest;
                    }
                } else {
                    log.warn("Failed to get pull request URL");
                }
            }
        } catch (IOException e){
            log.error("Can't authorize getPullRequest() " + e);
        }
        return null;
    }

    public void setPendingCheck(LPVSQueue webhookConfig) {
        try {
            if (GITHUB_AUTH_TOKEN.isEmpty()) setGithubTokenFromEnv();
            if (GITHUB_API_URL.isEmpty()) gitHub = GitHub.connect(GITHUB_LOGIN, GITHUB_AUTH_TOKEN);
            else gitHub = GitHub.connectToEnterpriseWithOAuth(GITHUB_API_URL, GITHUB_LOGIN, GITHUB_AUTH_TOKEN);
            GHRepository repository = gitHub.getRepository(LPVSWebhookUtil.getRepositoryOrganization(webhookConfig) + "/" + LPVSWebhookUtil.getRepositoryName(webhookConfig));
            repository.createCommitStatus(webhookConfig.getHeadCommitSHA(), GHCommitState.PENDING, null,
                                            "Scanning opensource licenses", "[License Pre-Validation Service]");
        } catch (IOException e) {
            log.error("Can't authorize setPendingCheck()" + e);
        }
    }

    public void setErrorCheck(LPVSQueue webhookConfig) {
        try {
            if (GITHUB_AUTH_TOKEN.isEmpty()) setGithubTokenFromEnv();
            if (GITHUB_API_URL.isEmpty()) gitHub = GitHub.connect(GITHUB_LOGIN, GITHUB_AUTH_TOKEN);
            else gitHub = GitHub.connectToEnterpriseWithOAuth(GITHUB_API_URL, GITHUB_LOGIN, GITHUB_AUTH_TOKEN);
            GHRepository repository = gitHub.getRepository(LPVSWebhookUtil.getRepositoryOrganization(webhookConfig) + "/"
                    + LPVSWebhookUtil.getRepositoryName(webhookConfig));
            repository.createCommitStatus(webhookConfig.getHeadCommitSHA(), GHCommitState.ERROR, null,
                    "Scanning process failed", "[License Pre-Validation Service]");
        } catch (IOException e) {
            log.error("Can't authorize setErrorCheck() " + e);
        }
    }

    public void commentResults(LPVSQueue webhookConfig, List<LPVSFile> scanResults, List<LPVSLicenseService.Conflict<String, String>> conflicts, LPVSPullRequest lpvsPullRequest) throws IOException {

        try {
            GHRepository repository = gitHub.getRepository(LPVSWebhookUtil.getRepositoryOrganization(webhookConfig) + "/"
                    + LPVSWebhookUtil.getRepositoryName(webhookConfig));
            GHPullRequest pullRequest = getPullRequest(webhookConfig, repository);

            if (pullRequest == null){
                log.error("Can't find pull request " + webhookConfig.getPullRequestUrl());
                lpvsPullRequest.setStatus(LPVSPullRequestStatus.NO_ACCESS.toString());
                pullRequestRepository.saveAndFlush(lpvsPullRequest);
                return;
            }
            if (scanResults == null && conflicts == null) {
                log.error("Files are not found in pull request " + webhookConfig.getPullRequestUrl());
                lpvsPullRequest.setStatus(LPVSPullRequestStatus.COMPLETED.toString());
                pullRequestRepository.saveAndFlush(lpvsPullRequest);
                repository.createCommitStatus(webhookConfig.getHeadCommitSHA(), GHCommitState.SUCCESS, null,
                        "Files are not found", "[License Pre-Validation Service]");
                return;
            }

            boolean hasProhibitedOrRestricted = false;
            boolean hasConflicts = false;
            String commitComment = "";

            if (scanResults != null && scanResults.size() != 0) {
                commitComment = "**Detected licenses:**\n\n\n";
                for (LPVSFile file : scanResults) {
                    commitComment += "**File:** " + file.getFilePath() + "\n";
                    commitComment += "**License(s):** " + file.convertLicensesToString(LPVSVcs.GITHUB) + "\n";
                    commitComment += "**Component:** " + file.getComponentName() + " (" + file.getComponentFilePath() + ")\n";
                    commitComment += "**Matched Lines:** " + LPVSCommentUtil.getMatchedLinesAsLink(webhookConfig, file, LPVSVcs.GITHUB) + "\n";
                    commitComment += "**Snippet Match:** " + file.getSnippetMatch() + "\n\n\n\n";
                    for (LPVSLicense license : file.getLicenses()) {
                        LPVSDetectedLicense detectedIssue = new LPVSDetectedLicense();
                        detectedIssue.setPullRequest(lpvsPullRequest);
                        detectedIssue.setLicense(license);
                        detectedIssue.setFilePath(file.getFilePath());
                        detectedIssue.setType(file.getSnippetType());
                        detectedIssue.setMatch(file.getSnippetMatch());
                        detectedIssue.setLines(file.getMatchedLines());
                        detectedIssue.setComponentFilePath(file.getComponentFilePath());
                        detectedIssue.setComponentFileUrl(file.getComponentFileUrl());
                        detectedIssue.setComponentName(file.getComponentName());
                        detectedIssue.setComponentLines(file.getComponentLines());
                        detectedIssue.setComponentUrl(file.getComponentUrl());
                        detectedIssue.setComponentVersion(file.getComponentVersion());
                        detectedIssue.setComponentVendor(file.getComponentVendor());
                        if (license.getAccess().isEmpty()
                                || license.getAccess().equalsIgnoreCase("prohibited")
                                || license.getAccess().equalsIgnoreCase("restricted")
                                || license.getAccess().equalsIgnoreCase("unreviewed")) {
                            hasProhibitedOrRestricted = true;
                            detectedIssue.setIssue(true);
                        } else {
                            detectedIssue.setIssue(false);
                        }
                        lpvsDetectedLicenseRepository.saveAndFlush(detectedIssue);
                    }
                }
            }

            if (conflicts != null && conflicts.size() > 0) {
                hasConflicts = true;
                StringBuilder commitCommentBuilder = new StringBuilder();
                commitCommentBuilder.append("**Detected license conflicts:**\n\n\n");
                commitCommentBuilder.append("<ul>");
                for (LPVSLicenseService.Conflict<String, String> conflict : conflicts) {
                    commitCommentBuilder.append("<li>" + conflict.l1 + " and " + conflict.l2 + "</li>");
                    LPVSDetectedLicense detectedIssue = new LPVSDetectedLicense();
                    detectedIssue.setPullRequest(lpvsPullRequest);
                    Long l1 = lpvsLicenseRepository.searchBySpdxId(conflict.l1).getLicenseId();
                    Long l2 = lpvsLicenseRepository.searchBySpdxId(conflict.l2).getLicenseId();
                    detectedIssue.setLicenseConflict(lpvsLicenseConflictRepository.findLicenseConflict(l1, l2));
                    if (webhookConfig.getRepositoryLicense() != null) {
                        LPVSLicense repoLicense = lpvsLicenseRepository.searchBySpdxId(webhookConfig.getRepositoryLicense());
                        if (repoLicense == null) {
                            repoLicense = lpvsLicenseRepository.searchByAlternativeLicenseNames(webhookConfig.getRepositoryLicense());
                        }
                        detectedIssue.setRepositoryLicense(repoLicense);
                    }
                    detectedIssue.setIssue(true);
                    lpvsDetectedLicenseRepository.saveAndFlush(detectedIssue);
                }
                commitCommentBuilder.append("</ul>");
                if (null != webhookConfig.getHubLink()) {
                    commitCommentBuilder.append("(");
                    commitCommentBuilder.append(webhookConfig.getHubLink());
                    commitCommentBuilder.append(")");
                }
                commitComment += commitCommentBuilder.toString();
            }

            if (hasProhibitedOrRestricted || hasConflicts) {
                lpvsPullRequest.setStatus(LPVSPullRequestStatus.ISSUES_DETECTED.toString());
                pullRequestRepository.save(lpvsPullRequest);
                pullRequest.comment("**\\[License Pre-Validation Service\\]** Potential license problem(s) detected \n\n" + 
                        commitComment + "</p>");
                repository.createCommitStatus(webhookConfig.getHeadCommitSHA(), GHCommitState.FAILURE, null,
                        "Potential license problem(s) detected", "[License Pre-Validation Service]");
            } else {
                lpvsPullRequest.setStatus(LPVSPullRequestStatus.COMPLETED.toString());
                pullRequestRepository.save(lpvsPullRequest);
                pullRequest.comment("**\\[License Pre-Validation Service\\]**  No license issue detected \n\n" + 
                        commitComment + "</p>");
                repository.createCommitStatus(webhookConfig.getHeadCommitSHA(), GHCommitState.SUCCESS, null,
                        "No license issue detected", "[License Pre-Validation Service]");
            }
        } catch (IOException | NullPointerException e) {
            lpvsPullRequest.setStatus(LPVSPullRequestStatus.INTERNAL_ERROR.toString());
            pullRequestRepository.saveAndFlush(lpvsPullRequest);
            log.error("Can't authorize commentResults() " + e);
            try {
                GHRepository repository = gitHub.getRepository(LPVSWebhookUtil.getRepositoryOrganization(webhookConfig) + "/"
                        + LPVSWebhookUtil.getRepositoryName(webhookConfig));
                repository.createCommitStatus(webhookConfig.getHeadCommitSHA(), GHCommitState.ERROR, null,
                        "Internal scan error occurs", "[License Pre-Validation Service]");
            } catch (Exception ex) {
                log.error("Can't authorize " + ex);
            }
        }
    }

    public String getRepositoryLicense(LPVSQueue webhookConfig) {
        try {
            String repositoryName = LPVSWebhookUtil.getRepositoryName(webhookConfig);
            String repositoryOrganization = LPVSWebhookUtil.getRepositoryOrganization(webhookConfig);
            if (GITHUB_AUTH_TOKEN.isEmpty()) setGithubTokenFromEnv();
            if (GITHUB_API_URL.isEmpty()) gitHub = GitHub.connect(GITHUB_LOGIN, GITHUB_AUTH_TOKEN);
            else gitHub = GitHub.connectToEnterpriseWithOAuth(GITHUB_API_URL, GITHUB_LOGIN, GITHUB_AUTH_TOKEN);
            GHRepository repository = gitHub.getRepository(repositoryOrganization + "/" + repositoryName);
            GHLicense license = repository.getLicense();
            if (license == null) {
                return "Proprietary";
            } else {
                return license.getKey();
            }
        } catch (IOException e) {
            log.error("Can't authorize getRepositoryLicense() " + e);
        }
        return "Proprietary";
    }



    public void setGithubTokenFromEnv() {
        if (System.getenv("LPVS_GITHUB_TOKEN") != null) GITHUB_AUTH_TOKEN = System.getenv("LPVS_GITHUB_TOKEN");
    }

}
