/**
 * Copyright (c) 2022-2024, Samsung Electronics Co., Ltd. All rights reserved.
 *
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */
package com.lpvs.controller;

import com.lpvs.entity.LPVSQueue;
import com.lpvs.entity.enums.LPVSPullRequestAction;
import com.lpvs.repository.LPVSQueueRepository;
import com.lpvs.service.LPVSGitHubConnectionService;
import com.lpvs.service.LPVSGitHubService;
import com.lpvs.service.LPVSQueueService;
import com.lpvs.util.LPVSExitHandler;
import com.lpvs.util.LPVSPayloadUtil;
import com.lpvs.entity.LPVSResponseWrapper;
import lombok.extern.slf4j.Slf4j;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import org.apache.commons.codec.binary.Hex;
import org.kohsuke.github.GHPullRequest;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.HtmlUtils;

import java.io.IOException;
import java.util.Date;
import java.util.Optional;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Controller class for handling GitHub webhook events and single scan requests.
 * This class is responsible for processing GitHub webhook payloads and triggering relevant actions.
 */
@RestController
@Slf4j
public class GitHubController {

    /**
     * The GitHub secret used for validating webhook payloads.
     * It is set from the LPVS_GITHUB_SECRET environment variable or the application property.
     */
    private String GITHUB_SECRET;

    /**
     * Initializes the GitHub secret from the LPVS_GITHUB_SECRET environment variable or the application property.
     * Exits the application if the secret is not set.
     */
    @PostConstruct
    public void initializeGitHubController() {
        this.GITHUB_SECRET =
                Optional.ofNullable(this.GITHUB_SECRET)
                        .filter(s -> !s.isEmpty())
                        .orElse(
                                Optional.ofNullable(System.getenv("LPVS_GITHUB_SECRET"))
                                        .orElse(""));
        if (this.GITHUB_SECRET.isEmpty()) {
            log.error("LPVS_GITHUB_SECRET (github.secret) is not set.");
            exitHandler.exit(-1);
        }
    }

    /**
     * LPVSQueueService for handling user-related business logic.
     */
    @Autowired private LPVSQueueService queueService;

    /**
     * LPVSQueueRepository for accessing and managing LPVSQueue entities.
     */
    @Autowired private LPVSQueueRepository queueRepository;

    /**
     * LPVSGitHubService for handling GitHub-related actions.
     */
    private LPVSGitHubService gitHubService;

    /**
     * Service for establishing and managing connections to the GitHub API.
     */
    private LPVSGitHubConnectionService gitHubConnectionService;

    /**
     * LPVSExitHandler for handling application exit scenarios.
     */
    private LPVSExitHandler exitHandler;

    private static final String SIGNATURE = "X-Hub-Signature-256";
    private static final String SUCCESS = "Success";
    private static final String ERROR = "Error";
    private static final String ALGORITHM = "HmacSHA256";

    /**
     * Constructor for GitHubController.
     * Initializes LPVSQueueService, LPVSGitHubService, LPVSQueueRepository, GitHub secret, and LPVSExitHandler.
     *
     * @param queueService      LPVSQueueService for handling user-related business logic.
     * @param gitHubService     LPVSGitHubService for handling GitHub-related actions.
     * @param gitHubConnectionService        Service for establishing and managing connections to the GitHub API.
     * @param queueRepository   LPVSQueueRepository for accessing and managing LPVSQueue entities.
     * @param GITHUB_SECRET     The GitHub secret used for validating webhook payloads.
     * @param exitHandler       LPVSExitHandler for handling application exit scenarios.
     */
    public GitHubController(
            LPVSQueueService queueService,
            LPVSGitHubService gitHubService,
            LPVSGitHubConnectionService gitHubConnectionService,
            LPVSQueueRepository queueRepository,
            @Value("${github.secret:}") String GITHUB_SECRET,
            LPVSExitHandler exitHandler) {
        this.queueService = queueService;
        this.gitHubService = gitHubService;
        this.gitHubConnectionService = gitHubConnectionService;
        this.queueRepository = queueRepository;
        this.GITHUB_SECRET = GITHUB_SECRET;
        this.exitHandler = exitHandler;
    }

    /**
     * Default endpoint that forwards POST requests to the `/webhooks` endpoint.
     * This method serves as a "default" POST endpoint that doesn't have any specific path assigned.
     * It forwards any requests to the `/webhooks` endpoint using Spring's `RequestDispatcher`.
     * Implemented to simplify GitHub webhook configuration.
     *
     * @param request The HttpServletRequest object representing the incoming request.
     * @param response The HttpServletResponse object for the outgoing response.
     * @throws ServletException If an error occurs during request forwarding.
     * @throws IOException If an input or output error occurs during request forwarding.
     */
    @RequestMapping(method = RequestMethod.POST)
    public void forwardToWebhook(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Forward the request to the `/webhooks` endpoint
        RequestDispatcher dispatcher = request.getRequestDispatcher("/webhooks");
        dispatcher.forward(request, response);
    }

    /**
     * Endpoint for handling GitHub webhook events and processing the payload.
     *
     * @param signature The signature of the webhook event.
     * @param payload   The payload of the webhook event.
     * @return The response entity indicating the status of the processing.
     * @throws Exception if an error occurs during processing.
     */
    @RequestMapping(value = "/webhooks", method = RequestMethod.POST)
    public ResponseEntity<LPVSResponseWrapper> gitHubWebhooks(
            @RequestHeader(SIGNATURE) String signature, @RequestBody String payload)
            throws Exception {
        log.debug("New GitHub webhook request received");

        // Validate and sanitize user inputs to prevent XSS attacks
        // if signature is empty - return 401
        if (!StringUtils.hasText(signature) || signature.length() > 72) {
            log.error("Received empty or too long signature");
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .headers(LPVSPayloadUtil.generateSecurityHeaders())
                    .body(new LPVSResponseWrapper(ERROR));
        }
        if (!GITHUB_SECRET.trim().isEmpty() && wrongSecret(signature, payload)) {
            log.error("Received empty or incorrect GITHUB_SECRET");
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .headers(LPVSPayloadUtil.generateSecurityHeaders())
                    .body(new LPVSResponseWrapper(ERROR));
        }

        // if payload is empty, don't do anything
        if (!StringUtils.hasText(payload)) {
            log.debug("Response to empty payload sent");
            // Implement Content Security Policy (CSP) headers
            return ResponseEntity.ok()
                    .headers(LPVSPayloadUtil.generateSecurityHeaders())
                    .body(new LPVSResponseWrapper(SUCCESS));
        } else if (LPVSPayloadUtil.checkPayload(payload)) {
            LPVSQueue webhookConfig = LPVSPayloadUtil.getGitHubWebhookConfig(payload);
            webhookConfig.setDate(new Date());
            webhookConfig.setReviewSystemType("github");
            queueRepository.save(webhookConfig);
            log.debug("Pull request scanning is enabled");
            gitHubService.setPendingCheck(webhookConfig);
            log.debug("Set status to Pending done");
            queueService.addFirst(webhookConfig);
            log.debug("Put Webhook config to the queue done");
        }

        log.debug("Response sent");
        return ResponseEntity.ok()
                .headers(LPVSPayloadUtil.generateSecurityHeaders())
                .body(new LPVSResponseWrapper(SUCCESS));
    }

    /**
     * Handles a GitHub single scan request.
     *
     * This endpoint performs a single scan operation based on the GitHub organization, repository,
     * and pull request number provided in the path variables. The method validates
     * the input parameters and performs necessary security checks.
     *
     * @param gitHubOrg The GitHub organization name. Must not be empty and should be a valid string.
     * @param gitHubRepo The GitHub repository name. Must not be empty and should be a valid string.
     * @param prNumber The pull request number. Must be a positive integer greater than or equal to 1.
     * @return ResponseEntity with LPVSResponseWrapper containing the result of the scan.
     *         If successful, returns HTTP 200 OK with the success message.
     *         If there are validation errors or security issues, returns HTTP 403 FORBIDDEN.
     */
    @RequestMapping(
            value = "/scan/{gitHubOrg}/{gitHubRepo}/{prNumber}",
            method = RequestMethod.POST)
    public ResponseEntity<LPVSResponseWrapper> gitHubSingleScan(
            @PathVariable("gitHubOrg") @NotEmpty @Valid String gitHubOrg,
            @PathVariable("gitHubRepo") @NotEmpty @Valid String gitHubRepo,
            @PathVariable("prNumber") @Min(1) @Valid Integer prNumber) {
        log.debug("New GitHub single scan request received");

        if (GITHUB_SECRET.trim().isEmpty()) {
            log.error("Received empty GITHUB_SECRET");
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .headers(LPVSPayloadUtil.generateSecurityHeaders())
                    .body(new LPVSResponseWrapper(ERROR));
        }

        // Validate and sanitize user inputs to prevent XSS attacks
        gitHubOrg = HtmlUtils.htmlEscape(gitHubOrg);
        gitHubRepo = HtmlUtils.htmlEscape(gitHubRepo);

        try {
            GitHub gitHub = gitHubConnectionService.connectToGitHubApi();
            GHRepository repository = gitHub.getRepository(gitHubOrg + "/" + gitHubRepo);
            GHPullRequest pullRequest = repository.getPullRequest(prNumber);
            LPVSQueue scanConfig = LPVSPayloadUtil.getGitHubWebhookConfig(repository, pullRequest);
            scanConfig.setAction(LPVSPullRequestAction.SINGLE_SCAN);
            scanConfig.setAttempts(0);
            scanConfig.setDate(new Date());
            scanConfig.setReviewSystemType("github");
            queueRepository.save(scanConfig);
            log.debug("Pull request scanning is enabled");
            gitHubService.setPendingCheck(scanConfig);
            log.debug("Set status to Pending done");
            queueService.addFirst(scanConfig);
            log.debug("Put Scan config to the queue done");
            log.debug("Response sent");
            return ResponseEntity.ok()
                    .headers(LPVSPayloadUtil.generateSecurityHeaders())
                    .body(new LPVSResponseWrapper(SUCCESS));
        } catch (Exception e) {
            log.error("Can't authorize single pull request scan " + e.getMessage());
        }
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .headers(LPVSPayloadUtil.generateSecurityHeaders())
                .body(new LPVSResponseWrapper(ERROR));
    }

    /**
     * Verifies if the signature matches the calculated signature using the GitHub secret.
     *
     * @param signature The signature to verify.
     * @param payload   The payload to calculate the signature.
     * @return true if the signature is valid, false otherwise.
     * @throws Exception if an error occurs during signature verification.
     */
    public boolean wrongSecret(String signature, String payload) throws Exception {
        String lpvsSecret = signature.split("=", 2)[1];

        SecretKeySpec key = new SecretKeySpec(GITHUB_SECRET.getBytes("utf-8"), ALGORITHM);
        Mac mac = Mac.getInstance(ALGORITHM);
        mac.init(key);
        String githubSecret = Hex.encodeHexString(mac.doFinal(payload.getBytes("utf-8")));

        log.debug("lpvs   signature: " + lpvsSecret);
        log.debug("github signature: " + githubSecret);

        return !lpvsSecret.equals(githubSecret);
    }
}
