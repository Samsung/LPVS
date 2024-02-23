/**
 * Copyright 2023 Basaeng, kyudori, hwan5180, quswjdgma83
 *
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */
package com.lpvs.controller;

import com.lpvs.entity.*;
import com.lpvs.entity.auth.LPVSLoginMember;
import com.lpvs.entity.auth.LPVSMember;
import com.lpvs.entity.dashboard.Dashboard;
import com.lpvs.entity.history.HistoryEntity;
import com.lpvs.entity.history.HistoryPageEntity;
import com.lpvs.entity.history.LPVSHistory;
import com.lpvs.entity.result.LPVSResult;
import com.lpvs.entity.result.LPVSResultFile;
import com.lpvs.entity.result.LPVSResultInfo;
import com.lpvs.repository.LPVSDetectedLicenseRepository;
import com.lpvs.repository.LPVSLicenseRepository;
import com.lpvs.repository.LPVSMemberRepository;
import com.lpvs.repository.LPVSPullRequestRepository;
import com.lpvs.service.LPVSLoginCheckService;
import com.lpvs.service.LPVSStatisticsService;
import com.lpvs.util.LPVSWebhookUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.context.annotation.Profile;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.HtmlUtils;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Controller class for handling web-related requests in LPVS.
 * This class manages user information, login details, history, results, and dashboard pages.
 */
@Controller
@Slf4j
public class LPVSWebController implements ErrorController {

    /**
     * Repository for LPVS members.
     */
    private LPVSMemberRepository memberRepository;

    /**
     * Repository for detected licenses.
     */
    private LPVSDetectedLicenseRepository detectedLicenseRepository;

    /**
     * Repository for LPVS pull requests.
     */
    private LPVSPullRequestRepository lpvsPullRequestRepository;

    /**
     * Repository for LPVS licenses.
     */
    private LPVSLicenseRepository licenseRepository;

    /**
     * Service for checking user logins.
     */
    private LPVSLoginCheckService lpvsLoginCheckService;

    /**
     * Service for generating LPVS statistics.
     */
    private LPVSStatisticsService lpvsStatisticsService;

    /**
     * Constructor for LPVSWebController.
     * Initializes repositories and services required for web-related functionality.
     *
     * @param memberRepository           Repository for LPVS members.
     * @param detectedLicenseRepository  Repository for detected licenses.
     * @param lpvsPullRequestRepository  Repository for LPVS pull requests.
     * @param licenseRepository           Repository for LPVS licenses.
     * @param lpvsLoginCheckService       Service for checking user logins.
     * @param lpvsStatisticsService       Service for generating LPVS statistics.
     */
    public LPVSWebController(
            LPVSMemberRepository memberRepository,
            LPVSDetectedLicenseRepository detectedLicenseRepository,
            LPVSPullRequestRepository lpvsPullRequestRepository,
            LPVSLicenseRepository licenseRepository,
            LPVSLoginCheckService lpvsLoginCheckService,
            LPVSStatisticsService lpvsStatisticsService) {
        this.memberRepository = memberRepository;
        this.detectedLicenseRepository = detectedLicenseRepository;
        this.lpvsPullRequestRepository = lpvsPullRequestRepository;
        this.licenseRepository = licenseRepository;
        this.lpvsLoginCheckService = lpvsLoginCheckService;
        this.lpvsStatisticsService = lpvsStatisticsService;
    }

    /**
     * Controller class for managing public web API endpoints in LPVS.
     * This class provides endpoints for retrieving user information, login details, and performing user-related actions.
     */
    @RestController
    @Profile("web")
    @RequestMapping("/api/v1/web")
    class WebApiEndpoints {

        /**
         * Retrieves personal information settings for the authenticated user.
         *
         * @param authentication The authentication object.
         * @return ResponseEntity containing LPVSMember representing personal information settings.
         *         The response includes security headers for enhanced security.
         */
        @GetMapping("/user/info")
        @ResponseBody
        public ResponseEntity<LPVSMember> personalInfoSettings(Authentication authentication) {
            lpvsLoginCheckService.loginVerification(authentication);
            HttpHeaders headers = LPVSWebhookUtil.generateSecurityHeaders();
            LPVSMember member = lpvsLoginCheckService.getMemberFromMemberMap(authentication);
            sanitizeUserInputs(member);
            return ResponseEntity.ok().headers(headers).body(member);
        }

        /**
         * Retrieves login details for the authenticated user.
         *
         * @param authentication The authentication object.
         * @return ResponseEntity containing LPVSLoginMember representing login details.
         *         The response includes security headers for enhanced security.
         */
        @GetMapping("/user/login")
        @ResponseBody
        public ResponseEntity<LPVSLoginMember> loginMember(Authentication authentication) {
            lpvsLoginCheckService.loginVerification(authentication);

            // Include security headers in the response
            HttpHeaders headers = LPVSWebhookUtil.generateSecurityHeaders();

            Map<String, Object> oauthLoginMemberMap =
                    lpvsLoginCheckService.getOauthLoginMemberMap(authentication);
            boolean isLoggedIn = oauthLoginMemberMap == null || oauthLoginMemberMap.isEmpty();

            LPVSLoginMember loginMember;

            if (!isLoggedIn) {
                LPVSMember findMember =
                        lpvsLoginCheckService.getMemberFromMemberMap(authentication);

                // Validate and sanitize user inputs to prevent XSS attacks
                sanitizeUserInputs(findMember);

                loginMember = new LPVSLoginMember(!isLoggedIn, findMember);
            } else {
                loginMember = new LPVSLoginMember(!isLoggedIn, null);
            }

            return ResponseEntity.ok().headers(headers).body(loginMember);
        }

        /**
         * Updates user settings based on the provided map.
         *
         * @param map             Map containing user settings.
         * @param authentication The authentication object.
         * @return ResponseEntity with LPVSMember representing the updated user.
         *         The response includes security headers for enhanced security.
         */
        @PostMapping("/user/update")
        public ResponseEntity<LPVSMember> postSettingTest(
                @RequestBody Map<String, String> map, Authentication authentication) {
            lpvsLoginCheckService.loginVerification(authentication);

            // Include security headers in the response
            HttpHeaders headers = LPVSWebhookUtil.generateSecurityHeaders();

            LPVSMember findMember = lpvsLoginCheckService.getMemberFromMemberMap(authentication);
            try {
                findMember.setNickname(map.get("nickname"));
                findMember.setOrganization(map.get("organization"));

                // Validate and sanitize user inputs to prevent XSS attacks
                sanitizeUserInputs(findMember);
                memberRepository.saveAndFlush(findMember);
            } catch (DataIntegrityViolationException e) {
                throw new IllegalArgumentException("DuplicatedKeyException");
            }
            return ResponseEntity.ok().headers(headers).body(findMember);
        }

        /**
         * Retrieves the history page entity based on the specified type and name.
         *
         * @param type           The type of history (e.g., user, organization).
         * @param name           The name of the user or organization.
         * @param pageable       The pageable object for pagination.
         * @param authentication The authentication object.
         * @return ResponseEntity<HistoryEntity> containing a list of LPVSHistory items and total count.
         *         The response includes security headers for enhanced security.
         */
        @ResponseBody
        @GetMapping("/history/{type}/{name}")
        public ResponseEntity<HistoryEntity> newHistoryPageByUser(
                @PathVariable("type") String type,
                @PathVariable("name") String name,
                @PageableDefault(size = 5, sort = "date", direction = Sort.Direction.DESC)
                        Pageable pageable,
                Authentication authentication) {

            HttpHeaders headers = LPVSWebhookUtil.generateSecurityHeaders();

            HistoryPageEntity historyPageEntity =
                    lpvsLoginCheckService.pathCheck(type, name, pageable, authentication);
            Page<LPVSPullRequest> prPage = historyPageEntity.getPrPage();
            Long count = historyPageEntity.getCount();

            List<LPVSHistory> lpvsHistories = new ArrayList<>();
            List<LPVSPullRequest> lpvsPullRequests = prPage.getContent();

            for (LPVSPullRequest pr : lpvsPullRequests) {
                String[] pullNumberTemp = pr.getPullRequestUrl().split("/");
                LocalDateTime localDateTime =
                        new Timestamp(pr.getDate().getTime()).toLocalDateTime();
                String formattingDateTime = lpvsLoginCheckService.dateTimeFormatting(localDateTime);

                // Validate and sanitize user inputs to prevent XSS attacks
                sanitizeUserInputs(pr);

                Boolean hasIssue = detectedLicenseRepository.existsIssue(pr);

                lpvsHistories.add(
                        new LPVSHistory(
                                formattingDateTime,
                                HtmlUtils.htmlEscape(pr.getRepositoryName()),
                                pr.getId(),
                                HtmlUtils.htmlEscape(pr.getPullRequestUrl()),
                                HtmlUtils.htmlEscape(pr.getStatus()),
                                HtmlUtils.htmlEscape(pr.getSender()),
                                pullNumberTemp[pullNumberTemp.length - 2]
                                        + "/"
                                        + pullNumberTemp[pullNumberTemp.length - 1],
                                hasIssue));
            }

            HistoryEntity historyEntity = new HistoryEntity(lpvsHistories, count);
            return ResponseEntity.ok().headers(headers).body(historyEntity);
        }

        /**
         * Retrieves the LPVSResult for a specific pull request ID.
         *
         * @param prId           The pull request ID.
         * @param pageable       The pageable object for pagination.
         * @param authentication The authentication object.
         * @return ResponseEntity<LPVSResult> containing result details for the specified pull request.
         *         The response includes security headers for enhanced security.
         */
        @ResponseBody
        @GetMapping("/result/{prId}")
        public ResponseEntity<LPVSResult> resultPage(
                @PathVariable("prId") Long prId,
                @PageableDefault(size = 5, sort = "id", direction = Sort.Direction.ASC)
                        Pageable pageable,
                Authentication authentication) {

            lpvsLoginCheckService.loginVerification(authentication);
            HttpHeaders headers = LPVSWebhookUtil.generateSecurityHeaders();

            Optional<LPVSPullRequest> prOpt = lpvsPullRequestRepository.findById(prId);
            if (!prOpt.isPresent()) {
                return ResponseEntity.notFound().headers(headers).build();
            }
            LPVSPullRequest pr = prOpt.get();

            List<LPVSLicense> distinctByLicense =
                    detectedLicenseRepository.findDistinctByLicense(pr);
            List<String> detectedLicenses = new ArrayList<>();
            Map<String, Integer> licenseCountMap = new HashMap<>();

            List<String> allSpdxId = licenseRepository.takeAllSpdxId();
            for (String spdxId : allSpdxId) {
                licenseCountMap.put(spdxId, 0);
            }
            for (LPVSLicense lpvsLicense : distinctByLicense) {
                detectedLicenses.add(lpvsLicense.getSpdxId());
            }

            LPVSResultInfo lpvsResultInfo =
                    new LPVSResultInfo(
                            pr.getId(),
                            pr.getDate(),
                            HtmlUtils.htmlEscape(pr.getRepositoryName()),
                            HtmlUtils.htmlEscape(pr.getStatus()),
                            detectedLicenses);

            Page<LPVSDetectedLicense> dlPage =
                    detectedLicenseRepository.findByPullRequest(pr, pageable);
            List<LPVSDetectedLicense> dlList = detectedLicenseRepository.findByPullRequest(pr);
            List<LPVSResultFile> lpvsResultFileList = new ArrayList<>();
            Boolean hasIssue = detectedLicenseRepository.existsIssue(pr);

            String licenseSpdxId;
            String status;
            for (LPVSDetectedLicense dl : dlPage) {
                if (dl.getLicense() == null) {
                    licenseSpdxId = null;
                    status = null;
                } else {
                    licenseSpdxId = dl.getLicense().getSpdxId();
                    status = dl.getLicense().getAccess();
                }
                lpvsResultFileList.add(
                        new LPVSResultFile(
                                dl.getId(),
                                HtmlUtils.htmlEscape(dl.getFilePath()),
                                HtmlUtils.htmlEscape(dl.getComponentFileUrl()),
                                dl.getLines(),
                                dl.getMatch(),
                                status,
                                licenseSpdxId));
            }

            for (LPVSDetectedLicense dl : dlList) {
                if (dl.getLicense() != null) {
                    licenseSpdxId = dl.getLicense().getSpdxId();
                    licenseCountMap.put(licenseSpdxId, licenseCountMap.get(licenseSpdxId) + 1);
                }
            }

            Long count = detectedLicenseRepository.CountByDetectedLicenseWherePullRequestId(pr);
            String[] tempPullNumber = pr.getPullRequestUrl().split("/");
            LPVSResult lpvsResult =
                    new LPVSResult(
                            lpvsResultFileList,
                            lpvsResultInfo,
                            count,
                            licenseCountMap,
                            tempPullNumber[tempPullNumber.length - 2]
                                    + '/'
                                    + tempPullNumber[tempPullNumber.length - 1],
                            hasIssue);
            return ResponseEntity.ok().headers(headers).body(lpvsResult);
        }

        /**
         * Retrieves the Dashboard entity based on the specified type and name.
         *
         * @param type           The type of the dashboard (e.g., user, organization).
         * @param name           The name of the user or organization.
         * @param authentication The authentication object.
         * @return ResponseEntity<Dashboard> containing statistics and insights.
         *         The response includes security headers for enhanced security.
         */
        @ResponseBody
        @GetMapping("dashboard/{type}/{name}")
        public ResponseEntity<Dashboard> dashboardPage(
                @PathVariable("type") String type,
                @PathVariable("name") String name,
                Authentication authentication) {

            HttpHeaders headers = LPVSWebhookUtil.generateSecurityHeaders();
            Dashboard dashboardEntity =
                    lpvsStatisticsService.getDashboardEntity(
                            type, HtmlUtils.htmlEscape(name), authentication);

            return ResponseEntity.ok().headers(headers).body(dashboardEntity);
        }
    }

    /**
     * Validate and sanitize user inputs to prevent XSS attacks.
     *
     * @param member The LPVSMember object containing user information.
     */
    public static void sanitizeUserInputs(LPVSMember member) {
        if (member != null) {
            // Sanitize user inputs using Spring's HtmlUtils
            if (member.getNickname() != null)
                member.setNickname(HtmlUtils.htmlEscape(member.getNickname()));
            if (member.getOrganization() != null)
                member.setOrganization(HtmlUtils.htmlEscape(member.getOrganization()));
        }
    }

    /**
     * Validate and sanitize user inputs to prevent XSS attacks.
     *
     * @param pr The LPVSPullRequest object containing user information.
     */
    public static void sanitizeUserInputs(LPVSPullRequest pr) {
        if (pr != null) {
            // Sanitize user inputs using Spring's HtmlUtils
            if (pr.getRepositoryName() != null)
                pr.setRepositoryName(HtmlUtils.htmlEscape(pr.getRepositoryName()));
            if (pr.getPullRequestUrl() != null)
                pr.setPullRequestUrl(HtmlUtils.htmlEscape(pr.getPullRequestUrl()));
            if (pr.getStatus() != null) pr.setStatus(HtmlUtils.htmlEscape(pr.getStatus()));
            if (pr.getSender() != null) pr.setSender(HtmlUtils.htmlEscape(pr.getSender()));
        }
    }
}
