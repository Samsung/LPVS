/**
 * Copyright (c) 2022, Samsung Electronics Co., Ltd. All rights reserved.
 *
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */

package com.lpvs.service;

import com.lpvs.entity.LPVSFile;
import com.lpvs.entity.LPVSLicense;
import com.lpvs.entity.config.WebhookConfig;
import com.lpvs.entity.enums.PullRequestAction;
import com.lpvs.util.FileUtil;
import org.kohsuke.github.GitHub;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GHPullRequest;
import org.kohsuke.github.GHLicense;
import org.kohsuke.github.GHIssueState;
import org.kohsuke.github.GHCommitState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Service
public class GitHubService {

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
    ApplicationContext applicationContext;

    @Autowired
    public GitHubService(@Value("${" + GITHUB_LOGIN_PROP_NAME + "}") String GITHUB_LOGIN,
                         @Value("${" + GITHUB_AUTH_TOKEN_PROP_NAME + "}") String GITHUB_AUTH_TOKEN,
                         @Value("${" + GITHUB_API_URL_PROP_NAME + "}") String GITHUB_API_URL) {
        this.GITHUB_LOGIN = Optional.ofNullable(GITHUB_LOGIN).filter(s -> !s.isEmpty())
                .orElse(Optional.ofNullable(System.getenv(GITHUB_LOGIN_ENV_VAR_NAME)).orElse(""));
        this.GITHUB_AUTH_TOKEN = Optional.ofNullable(GITHUB_AUTH_TOKEN).filter(s -> !s.isEmpty())
                .orElse(Optional.ofNullable(System.getenv(GITHUB_AUTH_TOKEN_ENV_VAR_NAME)).orElse(""));
        this.GITHUB_API_URL = Optional.ofNullable(GITHUB_API_URL).filter(s -> !s.isEmpty())
                .orElse(Optional.ofNullable(System.getenv(GITHUB_API_URL_ENV_VAR_NAME)).orElse(""));
    }

    @PostConstruct
    @Profile("!test")
    private void checks() throws Exception {
        if (this.GITHUB_AUTH_TOKEN.isEmpty()) {
            LOG.error(GITHUB_AUTH_TOKEN_ENV_VAR_NAME + "(" + GITHUB_AUTH_TOKEN_PROP_NAME + ") is not set.");
            System.exit(SpringApplication.exit(applicationContext, () -> -1));
        }
    }

    private static final Logger LOG = LoggerFactory.getLogger(GitHubService.class);

    private static GitHub gitHub;

    public String getPullRequestFiles (WebhookConfig webhookConfig) {
        if (webhookConfig.getAction().equals(PullRequestAction.RESCAN)) {
            webhookConfig.setPullRequestAPIUrl(GITHUB_API_URL + "/repos/" + webhookConfig.getRepositoryOrganization() + "/" +
                    webhookConfig.getRepositoryName() + "/pulls/" + webhookConfig.getPullRequestId());
        }
        try {
            if (GITHUB_AUTH_TOKEN.isEmpty()) setGithubTokenFromEnv();
            if (GITHUB_API_URL.isEmpty()) gitHub = GitHub.connect(GITHUB_LOGIN, GITHUB_AUTH_TOKEN);
            else gitHub = GitHub.connectToEnterpriseWithOAuth(GITHUB_API_URL, GITHUB_LOGIN, GITHUB_AUTH_TOKEN);
            GHRepository repository = gitHub.getRepository(webhookConfig.getRepositoryOrganization()+"/"
                                                                            +webhookConfig.getRepositoryName());
            GHPullRequest pullRequest = getPullRequest(webhookConfig, repository);
            if (pullRequest == null){
                LOG.error("Can't find pull request " + webhookConfig.getPullRequestAPIUrl());
                return null;
            }
            webhookConfig.setPullRequestName(pullRequest.getTitle());
            if (webhookConfig.getAction().equals(PullRequestAction.RESCAN)) {
                webhookConfig.setHeadCommitSHA(pullRequest.getHead().getSha());
            }
            return FileUtil.saveFiles(pullRequest.listFiles(),webhookConfig.getRepositoryOrganization()+"/"+webhookConfig.getRepositoryName(),
                                        webhookConfig.getHeadCommitSHA(), pullRequest.getDeletions());
        } catch (IOException e){
            LOG.error("Can't authorize getPullRequestFiles() " + e);
        }
        return null;
    }

    private GHPullRequest getPullRequest(WebhookConfig webhookConfig, GHRepository repository){
        try {
            List<GHPullRequest> pullRequests = repository.getPullRequests(GHIssueState.OPEN);
            for (GHPullRequest pullRequest : pullRequests) {
                if (pullRequest.getUrl().toString().equals(webhookConfig.getPullRequestAPIUrl())){
                    return pullRequest;
                }
            }
        } catch (IOException e){
            LOG.error("Can't authorize getPullRequest() " + e);
        }
        return null;
    }

    public void setPendingCheck(WebhookConfig webhookConfig) {
        try {
            if (GITHUB_AUTH_TOKEN.isEmpty()) setGithubTokenFromEnv();
            if (GITHUB_API_URL.isEmpty()) gitHub = GitHub.connect(GITHUB_LOGIN, GITHUB_AUTH_TOKEN);
            else gitHub = GitHub.connectToEnterpriseWithOAuth(GITHUB_API_URL, GITHUB_LOGIN, GITHUB_AUTH_TOKEN);
            GHRepository repository = gitHub.getRepository(webhookConfig.getRepositoryOrganization() + "/"
                                                                                + webhookConfig.getRepositoryName());
            repository.createCommitStatus(webhookConfig.getHeadCommitSHA(), GHCommitState.PENDING, null,
                                            "Scanning opensource licenses", "[Open Source License Validation]");
        } catch (IOException e) {
            LOG.error("Can't authorize setPendingCheck()" + e);
        }
    }

    public void setErrorCheck(WebhookConfig webhookConfig) {
        try {
            if (GITHUB_AUTH_TOKEN.isEmpty()) setGithubTokenFromEnv();
            if (GITHUB_API_URL.isEmpty()) gitHub = GitHub.connect(GITHUB_LOGIN, GITHUB_AUTH_TOKEN);
            else gitHub = GitHub.connectToEnterpriseWithOAuth(GITHUB_API_URL, GITHUB_LOGIN, GITHUB_AUTH_TOKEN);
            GHRepository repository = gitHub.getRepository(webhookConfig.getRepositoryOrganization() + "/"
                    + webhookConfig.getRepositoryName());
            repository.createCommitStatus(webhookConfig.getHeadCommitSHA(), GHCommitState.ERROR, null,
                    "Scanning process failed", "[Open Source License Validation]");
        } catch (IOException e) {
            LOG.error("Can't authorize setErrorCheck() " + e);
        }
    }

    public void commentResults(WebhookConfig webhookConfig, List<LPVSFile> scanResults, List<LicenseService.Conflict<String, String>> conflicts) {
        try {
            GHRepository repository = gitHub.getRepository(webhookConfig.getRepositoryOrganization() + "/"
                    + webhookConfig.getRepositoryName());
            GHPullRequest pullRequest = getPullRequest(webhookConfig, repository);

            if (pullRequest == null){
                LOG.error("Can't find pull request " + webhookConfig.getPullRequestAPIUrl());
                return;
            }
            if (scanResults.isEmpty()) {
                pullRequest.comment("**\\[Open Source License Validation\\]**  No license issue detected \n");
                repository.createCommitStatus(webhookConfig.getHeadCommitSHA(), GHCommitState.SUCCESS, null,
                        "No license issue detected", "[Open Source License Validation]");
            } else {
                boolean hasProhibitedOrRestricted = false;
                boolean hasConflicts = false;
                String commitComment = "**Detected licenses:**\n\n\n";
                for (LPVSFile file : scanResults) {
                    commitComment += "**File:** " + file.getFilePath() + "\n";
                    commitComment += "**License(s):** " + file.convertLicensesToString() + "\n";
                    commitComment += "**Component:** " + file.getComponent() + " (" + file.getFileUrl() + ")\n";
                    commitComment += "**Matched Lines:** " + getMatchedLinesAsLink(webhookConfig, file) + "\n";
                    commitComment += "**Snippet Match:** " + file.getSnippetMatch() + "\n\n\n\n";
                    for (LPVSLicense license : file.getLicenses()) {
                        if (license.getAccess().equalsIgnoreCase("prohibited")
                                || license.getAccess().equalsIgnoreCase("restricted")
                                || license.getAccess().equalsIgnoreCase("unreviewed")) {
                            hasProhibitedOrRestricted = true;
                        }
                    }
                }

                if (conflicts.size() > 0) {
                    hasConflicts = true;
                    commitComment += "**Detected license conflicts:**\n\n\n";
                    commitComment += "<ul>";
                    for (LicenseService.Conflict<String, String> conflict : conflicts) {

                        commitComment += "<li>" + conflict.l1 + " and " + conflict.l2 + "</li>";
                    }
                    commitComment += "</ul>";
                }

                if (hasProhibitedOrRestricted || hasConflicts) {
                    pullRequest.comment("**\\[Open Source License Validation\\]** Potential license problem(s) detected \n\n" +
                            commitComment + "\n");
                    repository.createCommitStatus(webhookConfig.getHeadCommitSHA(), GHCommitState.FAILURE, null,
                            "Potential license problem(s) detected", "[Open Source License Validation]");
                } else {
                    pullRequest.comment("**\\[Open Source License Validation\\]**  No license issue detected \n\n" +
                            commitComment + "\n");
                    repository.createCommitStatus(webhookConfig.getHeadCommitSHA(), GHCommitState.SUCCESS, null,
                            "No license issue detected", "[Open Source License Validation]");
                }
            }
        } catch (IOException e) {
            LOG.error("Can't authorize commentResults() " + e);
        } catch (NullPointerException e) {
            LOG.error("There is no commit in pull request");
        }
    }

    public String getRepositoryLicense(WebhookConfig webhookConfig) {
        try {
            String repositoryName = webhookConfig.getRepositoryName();
            String repositoryOrganization = webhookConfig.getRepositoryOrganization();
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
            LOG.error("Can't authorize getRepositoryLicense() " + e);
        }
        return "Proprietary";
    }

    public String getMatchedLinesAsLink(WebhookConfig webhookConfig, LPVSFile file) {
        String prefix = webhookConfig.getRepositoryUrl() + "/blob/" + webhookConfig.getHeadCommitSHA() + "/" + file.getFilePath();
        String matchedLines = new String();
        if (file.getMatchedLines().equals("all")) {
            return "<a target=\"_blank\" href=\"" + prefix + "\">" + file.getMatchedLines() + "</a>";
        }
        prefix = prefix.concat("#L");
        for (String lineInfo : file.getMatchedLines().split(",")){
            String link = prefix+lineInfo.replace('-','L');
            matchedLines = matchedLines.concat("<a target=\"_blank\" href=\"" + link + "\">" + lineInfo + "</a>");
        }
        LOG.debug("MatchedLines: " + matchedLines);
        return matchedLines;
    }

    public void setGithubTokenFromEnv() {
            if (System.getenv("LPVS_GITHUB_TOKEN") != null) GITHUB_AUTH_TOKEN = System.getenv("LPVS_GITHUB_TOKEN");
    }

}
