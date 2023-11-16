/**
 * Copyright 2023 Basaeng, kyudori, hwan5180, quswjdgma83
 *
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */
package com.lpvs.controller;

import com.lpvs.entity.*;
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
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    @RequestMapping("/api/v1/web")
    @RestController
    class WebApiEndpoints {

        /**
         * Retrieves personal information settings for the authenticated user.
         *
         * @param authentication The authentication object.
         * @return LPVSMember object representing personal information settings.
         */
        @GetMapping("/user/info")
        @ResponseBody
        public LPVSMember personalInfoSettings(Authentication authentication) {
            lpvsLoginCheckService.loginVerification(authentication);
            return lpvsLoginCheckService.getMemberFromMemberMap(authentication);
        }

        /**
         * Retrieves login details for the authenticated user.
         *
         * @param authentication The authentication object.
         * @return LPVSLoginMember object representing login details.
         */
        @GetMapping("/user/login")
        @ResponseBody
        public LPVSLoginMember loginMember(Authentication authentication) {
            Map<String, Object> oauthLoginMemberMap =
                    lpvsLoginCheckService.getOauthLoginMemberMap(authentication);
            boolean isLoggedIn = oauthLoginMemberMap == null || oauthLoginMemberMap.isEmpty();
            if (!isLoggedIn) {
                LPVSMember findMember =
                        lpvsLoginCheckService.getMemberFromMemberMap(authentication);
                return new LPVSLoginMember(!isLoggedIn, findMember);
            } else {
                return new LPVSLoginMember(!isLoggedIn, null);
            }
        }

        /**
         * Updates user settings based on the provided map.
         *
         * @param map             Map containing user settings.
         * @param authentication The authentication object.
         * @return ResponseEntity with LPVSMember representing the updated user.
         */
        @PostMapping("/user/update")
        public ResponseEntity<LPVSMember> postSettingTest(
                @RequestBody Map<String, String> map, Authentication authentication) {
            lpvsLoginCheckService.loginVerification(authentication);
            LPVSMember findMember = lpvsLoginCheckService.getMemberFromMemberMap(authentication);
            try {
                findMember.setNickname(map.get("nickname"));
                findMember.setOrganization(map.get("organization"));
                memberRepository.saveAndFlush(findMember);
            } catch (DataIntegrityViolationException e) {
                throw new IllegalArgumentException("DuplicatedKeyException");
            }
            return ResponseEntity.ok().body(findMember);
        }

        /**
         * Retrieves the history page entity based on the specified type and name.
         *
         * @param type           The type of history (e.g., user, organization).
         * @param name           The name of the user or organization.
         * @param pageable       The pageable object for pagination.
         * @param authentication The authentication object.
         * @return HistoryEntity containing a list of LPVSHistory items and total count.
         */
        @ResponseBody
        @GetMapping("/history/{type}/{name}")
        public HistoryEntity newHistoryPageByUser(
                @PathVariable("type") String type,
                @PathVariable("name") String name,
                @PageableDefault(size = 5, sort = "date", direction = Sort.Direction.DESC)
                        Pageable pageable,
                Authentication authentication) {

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

                Boolean hasIssue = detectedLicenseRepository.existsIssue(pr);

                lpvsHistories.add(
                        new LPVSHistory(
                                formattingDateTime,
                                pr.getRepositoryName(),
                                pr.getId(),
                                pr.getPullRequestUrl(),
                                pr.getStatus(),
                                pr.getSender(),
                                pullNumberTemp[pullNumberTemp.length - 2]
                                        + "/"
                                        + pullNumberTemp[pullNumberTemp.length - 1],
                                hasIssue));
            }

            HistoryEntity historyEntity = new HistoryEntity(lpvsHistories, count);
            return historyEntity;
        }

        /**
         * Retrieves the LPVSResult for a specific pull request ID.
         *
         * @param prId           The pull request ID.
         * @param pageable       The pageable object for pagination.
         * @param authentication The authentication object.
         * @return LPVSResult containing result details for the specified pull request.
         */
        @ResponseBody
        @GetMapping("/result/{prId}")
        public LPVSResult resultPage(
                @PathVariable("prId") Long prId,
                @PageableDefault(size = 5, sort = "id", direction = Sort.Direction.ASC)
                        Pageable pageable,
                Authentication authentication) {

            lpvsLoginCheckService.loginVerification(authentication);
            // LPVSMember findMember = lpvsLoginCheckService.getMemberFromMemberMap(authentication);

            LPVSPullRequest pr = lpvsPullRequestRepository.findById(prId).get();
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
                            pr.getRepositoryName(),
                            pr.getStatus(),
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
                                dl.getFilePath(),
                                dl.getComponentFileUrl(),
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
            return lpvsResult;
        }

        /**
         * Retrieves the Dashboard entity based on the specified type and name.
         *
         * @param type           The type of the dashboard (e.g., user, organization).
         * @param name           The name of the user or organization.
         * @param authentication The authentication object.
         * @return Dashboard entity containing statistics and insights.
         */
        @ResponseBody
        @GetMapping("dashboard/{type}/{name}")
        public Dashboard dashboardPage(
                @PathVariable("type") String type,
                @PathVariable("name") String name,
                Authentication authentication) {

            Dashboard dashboardEntity =
                    lpvsStatisticsService.getDashboardEntity(type, name, authentication);

            return dashboardEntity;
        }
    }

    /**
     * Redirects to the default error page.
     *
     * @return String representing the path to the error page.
     */
    @GetMapping("/error")
    public String redirect() {
        return "index.html";
    }
}
