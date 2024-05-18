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
import com.lpvs.util.LPVSFileUtil;
import com.lpvs.util.LPVSPayloadUtil;
import lombok.extern.slf4j.Slf4j;
import org.kohsuke.github.GitHub;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GHPullRequest;
import org.kohsuke.github.GHLicense;
import org.kohsuke.github.GHIssueState;
import org.kohsuke.github.GHCommitState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * Service class for interacting with GitHub repositories and managing license-related actions.
 */
@Service
@Slf4j
public class LPVSGitHubService {

    /**
     * Repository for managing LPVS pull requests.
     */
    private LPVSPullRequestRepository pullRequestRepository;

    /**
     * Repository for managing detected licenses.
     */
    private LPVSDetectedLicenseRepository lpvsDetectedLicenseRepository;

    /**
     * Repository for managing licenses.
     */
    private LPVSLicenseRepository lpvsLicenseRepository;

    /**
     * Repository for managing license conflicts.
     */
    private LPVSLicenseConflictRepository lpvsLicenseConflictRepository;

    /**
     * Service for establishing and managing connections to the GitHub API.
     */
    private LPVSGitHubConnectionService gitHubConnectionService;

    /**
     * The GitHub instance for interacting with the GitHub API.
     */
    private static GitHub gitHub;

    /**
     * Constructs an instance of LPVSGitHubService with the specified repositories and connection service.
     *
     * @param pullRequestRepository         Repository for managing LPVS pull requests.
     * @param lpvsDetectedLicenseRepository Repository for managing detected licenses.
     * @param lpvsLicenseRepository         Repository for managing licenses.
     * @param lpvsLicenseConflictRepository Repository for managing license conflicts.
     * @param gitHubConnectionService        Service for establishing and managing connections to the GitHub API.
     */
    @Autowired
    public LPVSGitHubService(
            LPVSPullRequestRepository pullRequestRepository,
            LPVSDetectedLicenseRepository lpvsDetectedLicenseRepository,
            LPVSLicenseRepository lpvsLicenseRepository,
            LPVSLicenseConflictRepository lpvsLicenseConflictRepository,
            LPVSGitHubConnectionService gitHubConnectionService) {
        this.pullRequestRepository = pullRequestRepository;
        this.lpvsDetectedLicenseRepository = lpvsDetectedLicenseRepository;
        this.lpvsLicenseRepository = lpvsLicenseRepository;
        this.lpvsLicenseConflictRepository = lpvsLicenseConflictRepository;
        this.gitHubConnectionService = gitHubConnectionService;
    }

    /**
     * Retrieves the file differences for a given pull request from GitHub.
     *
     * @param webhookConfig LPVSQueue configuration for the pull request.
     * @return String representation of the pull request files or null if an error occurs.
     */
    public String getPullRequestFiles(LPVSQueue webhookConfig) {
        try {
            gitHub = gitHubConnectionService.connectToGitHubApi();
            log.debug(
                    "Repository Info: "
                            + LPVSPayloadUtil.getRepositoryOrganization(webhookConfig)
                            + "/"
                            + LPVSPayloadUtil.getRepositoryName(webhookConfig));
            GHRepository repository =
                    gitHub.getRepository(
                            LPVSPayloadUtil.getRepositoryOrganization(webhookConfig)
                                    + "/"
                                    + LPVSPayloadUtil.getRepositoryName(webhookConfig));

            GHPullRequest pullRequest = getPullRequest(webhookConfig, repository);
            if (pullRequest == null) {
                log.error("Can't find pull request " + webhookConfig.getPullRequestUrl());
                return null;
            }
            log.debug("Saving files...");
            return LPVSFileUtil.saveGithubDiffs(pullRequest.listFiles(), webhookConfig);
        } catch (IOException | IllegalArgumentException e) {
            log.error("Can't authorize getPullRequestFiles(): " + e.getMessage());
        }
        return null;
    }

    /**
     * Retrieves the GitHub pull request associated with the provided LPVSQueue configuration.
     *
     * @param webhookConfig LPVSQueue configuration for the pull request.
     * @param repository    The GitHub repository.
     * @return The GitHub pull request or null if not found or an error occurs.
     */
    private GHPullRequest getPullRequest(LPVSQueue webhookConfig, GHRepository repository) {
        try {
            List<GHPullRequest> pullRequests = repository.getPullRequests(GHIssueState.OPEN);
            for (GHPullRequest pullRequest : pullRequests) {
                if (null != pullRequest.getUrl()) {
                    log.debug(
                            "Pull request check: "
                                    + pullRequest.getUrl().toString()
                                    + " / "
                                    + webhookConfig.getPullRequestAPIUrl());
                    if (pullRequest
                            .getUrl()
                            .toString()
                            .equals(webhookConfig.getPullRequestAPIUrl())) {
                        log.debug("Return pull request " + pullRequest.getDiffUrl());
                        return pullRequest;
                    }
                } else {
                    log.warn("Failed to get pull request URL");
                }
            }
        } catch (IOException e) {
            log.error("Can't authorize getPullRequest() " + e);
        }
        return null;
    }

    /**
     * Sets the commit status to pending for the specified pull request.
     *
     * @param webhookConfig LPVSQueue configuration for the pull request.
     */
    public void setPendingCheck(LPVSQueue webhookConfig) {
        try {
            gitHub = gitHubConnectionService.connectToGitHubApi();
            GHRepository repository =
                    gitHub.getRepository(
                            LPVSPayloadUtil.getRepositoryOrganization(webhookConfig)
                                    + "/"
                                    + LPVSPayloadUtil.getRepositoryName(webhookConfig));
            repository.createCommitStatus(
                    webhookConfig.getHeadCommitSHA(),
                    GHCommitState.PENDING,
                    null,
                    "Scanning opensource licenses",
                    "[License Pre-Validation Service]");
        } catch (IOException | IllegalArgumentException e) {
            log.error("Can't authorize setPendingCheck(): " + e.getMessage());
        }
    }

    /**
     * Sets the commit status to error for the specified pull request.
     *
     * @param webhookConfig LPVSQueue configuration for the pull request.
     */
    public void setErrorCheck(LPVSQueue webhookConfig) {
        try {
            gitHub = gitHubConnectionService.connectToGitHubApi();
            GHRepository repository =
                    gitHub.getRepository(
                            LPVSPayloadUtil.getRepositoryOrganization(webhookConfig)
                                    + "/"
                                    + LPVSPayloadUtil.getRepositoryName(webhookConfig));
            repository.createCommitStatus(
                    webhookConfig.getHeadCommitSHA(),
                    GHCommitState.ERROR,
                    null,
                    "Scanning process failed",
                    "[License Pre-Validation Service]");
        } catch (IOException | IllegalArgumentException e) {
            log.error("Can't authorize setErrorCheck(): " + e.getMessage());
        }
    }

    /**
     * Comments on the pull request with the scan results and detected issues.
     *
     * @param webhookConfig  LPVSQueue configuration for the pull request.
     * @param scanResults    List of detected files and licenses.
     * @param conflicts      List of license conflicts.
     * @param lpvsPullRequest LPVS entity representing the pull request.
     */
    public void commentResults(
            LPVSQueue webhookConfig,
            List<LPVSFile> scanResults,
            List<LPVSLicenseService.Conflict<String, String>> conflicts,
            LPVSPullRequest lpvsPullRequest)
            throws Exception {

        GHRepository repository =
                gitHub.getRepository(
                        LPVSPayloadUtil.getRepositoryOrganization(webhookConfig)
                                + "/"
                                + LPVSPayloadUtil.getRepositoryName(webhookConfig));
        GHPullRequest pullRequest = getPullRequest(webhookConfig, repository);

        if (pullRequest == null) {
            log.error("Pull request is not found " + webhookConfig.getPullRequestUrl());
            lpvsPullRequest.setStatus(LPVSPullRequestStatus.NO_ACCESS.toString());
            pullRequestRepository.saveAndFlush(lpvsPullRequest);
            return;
        }
        if (scanResults == null && conflicts == null) {
            log.error("Files are not found in pull request " + webhookConfig.getPullRequestUrl());
            lpvsPullRequest.setStatus(LPVSPullRequestStatus.COMPLETED.toString());
            pullRequestRepository.saveAndFlush(lpvsPullRequest);
            repository.createCommitStatus(
                    webhookConfig.getHeadCommitSHA(),
                    GHCommitState.SUCCESS,
                    null,
                    "Files are not found",
                    "[License Pre-Validation Service]");
            return;
        }

        boolean hasProhibitedOrRestricted = false;
        boolean hasConflicts = false;
        String commitComment = "";

        if (scanResults != null && scanResults.size() != 0) {
            commitComment = "**Detected licenses:**\n\n\n";
            for (LPVSFile file : scanResults) {
                commitComment += "**File:** " + file.getFilePath() + "\n";
                commitComment +=
                        "**License(s):** " + file.convertLicensesToString(LPVSVcs.GITHUB) + "\n";
                commitComment +=
                        "**Component:** "
                                + file.getComponentName()
                                + " ("
                                + file.getComponentFilePath()
                                + ")\n";
                commitComment +=
                        "**Matched Lines:** "
                                + LPVSCommentUtil.getMatchedLinesAsLink(
                                        webhookConfig, file, LPVSVcs.GITHUB)
                                + "\n";
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

        StringBuilder commitCommentBuilder = new StringBuilder();
        if (conflicts != null && conflicts.size() > 0) {
            hasConflicts = true;
            commitCommentBuilder.append("**Detected license conflicts:**\n\n\n");
            commitCommentBuilder.append("<ul>");
            for (LPVSLicenseService.Conflict<String, String> conflict : conflicts) {
                commitCommentBuilder.append("<li>" + conflict.l1 + " and " + conflict.l2 + "</li>");
                LPVSDetectedLicense detectedIssue = new LPVSDetectedLicense();
                detectedIssue.setPullRequest(lpvsPullRequest);
                Long l1 = lpvsLicenseRepository.searchBySpdxId(conflict.l1).getLicenseId();
                Long l2 = lpvsLicenseRepository.searchBySpdxId(conflict.l2).getLicenseId();
                detectedIssue.setLicenseConflict(
                        lpvsLicenseConflictRepository.findLicenseConflict(l1, l2));
                if (webhookConfig.getRepositoryLicense() != null) {
                    LPVSLicense repoLicense =
                            lpvsLicenseRepository.searchBySpdxId(
                                    webhookConfig.getRepositoryLicense());
                    if (repoLicense == null) {
                        repoLicense =
                                lpvsLicenseRepository.searchByAlternativeLicenseNames(
                                        webhookConfig.getRepositoryLicense());
                    }
                    detectedIssue.setRepositoryLicense(repoLicense);
                }
                detectedIssue.setIssue(true);
                lpvsDetectedLicenseRepository.saveAndFlush(detectedIssue);
            }
            commitCommentBuilder.append("</ul>");
        }
        if (null != webhookConfig.getHubLink()) {
            commitCommentBuilder.append(
                    "\n\n###### <p align='right'>Check the validation details at the [link](");
            commitCommentBuilder.append(webhookConfig.getHubLink());
            commitCommentBuilder.append(")</p>");
        }
        commitComment += commitCommentBuilder.toString();

        if (hasProhibitedOrRestricted || hasConflicts) {
            lpvsPullRequest.setStatus(LPVSPullRequestStatus.ISSUES_DETECTED.toString());
            pullRequestRepository.save(lpvsPullRequest);
            pullRequest.comment(
                    "**\\[License Pre-Validation Service\\]** Potential license problem(s) detected \n\n"
                            + commitComment);
            repository.createCommitStatus(
                    webhookConfig.getHeadCommitSHA(),
                    GHCommitState.FAILURE,
                    null,
                    "Potential license problem(s) detected",
                    "[License Pre-Validation Service]");
        } else {
            lpvsPullRequest.setStatus(LPVSPullRequestStatus.COMPLETED.toString());
            pullRequestRepository.save(lpvsPullRequest);
            pullRequest.comment(
                    "**\\[License Pre-Validation Service\\]**  No license issue detected \n\n"
                            + commitComment);
            repository.createCommitStatus(
                    webhookConfig.getHeadCommitSHA(),
                    GHCommitState.SUCCESS,
                    null,
                    "No license issue detected",
                    "[License Pre-Validation Service]");
        }
    }

    /**
     * Retrieves the license of the GitHub repository associated with the pull request.
     *
     * @param webhookConfig LPVSQueue configuration for the pull request.
     * @return License key of the GitHub repository or null if not available.
     */
    public String getRepositoryLicense(LPVSQueue webhookConfig) {
        try {
            String repositoryName = LPVSPayloadUtil.getRepositoryName(webhookConfig);
            String repositoryOrganization =
                    LPVSPayloadUtil.getRepositoryOrganization(webhookConfig);
            gitHub = gitHubConnectionService.connectToGitHubApi();
            GHRepository repository =
                    gitHub.getRepository(repositoryOrganization + "/" + repositoryName);
            GHLicense license = repository.getLicense();
            if (license == null) {
                return null;
            } else {
                return license.getKey();
            }
        } catch (IOException | IllegalArgumentException e) {
            log.error("Can't authorize getRepositoryLicense(): " + e.getMessage());
        }
        return null;
    }

    /**
     * Retrieves the LPVSQueue configuration for a given GitHub pull request URL.
     *
     * @param pullRequest The GitHub pull request URL.
     * @return LPVSQueue configuration for the given pull request.
     */
    public LPVSQueue getInternalQueueByPullRequest(String pullRequest) {
        try {
            if (pullRequest == null) {
                return null;
            }
            String[] pullRequestSplit = pullRequest.split("/");
            if (pullRequestSplit.length < 5) return null;
            String pullRequestRepo =
                    String.join(
                            "/",
                            Arrays.asList(pullRequestSplit)
                                    .subList(
                                            pullRequestSplit.length - 4,
                                            pullRequestSplit.length - 2));
            int pullRequestNum = Integer.parseInt(pullRequestSplit[pullRequestSplit.length - 1]);
            gitHub = gitHubConnectionService.connectToGitHubApi();
            GHRepository repo = gitHub.getRepository(pullRequestRepo);
            GHPullRequest pR = repo.getPullRequest(pullRequestNum);
            return LPVSPayloadUtil.getGitHubWebhookConfig(repo, pR);
        } catch (IOException e) {
            log.error("Can't set up github client: " + e);
        }
        return null;
    }
}
