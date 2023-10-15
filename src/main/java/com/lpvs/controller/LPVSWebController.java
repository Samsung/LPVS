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

@Controller
@Slf4j
public class LPVSWebController implements ErrorController {
    private LPVSMemberRepository memberRepository;
    private LPVSDetectedLicenseRepository detectedLicenseRepository;
    private LPVSPullRequestRepository lpvsPullRequestRepository;
    private LPVSLicenseRepository licenseRepository;
    private LPVSLoginCheckService lpvsLoginCheckService;

    private LPVSStatisticsService lpvsStatisticsService;

    public LPVSWebController(LPVSMemberRepository memberRepository, LPVSDetectedLicenseRepository detectedLicenseRepository,
                             LPVSPullRequestRepository lpvsPullRequestRepository, LPVSLicenseRepository licenseRepository,
                             LPVSLoginCheckService lpvsLoginCheckService, LPVSStatisticsService lpvsStatisticsService) {
        this.memberRepository = memberRepository;
        this.detectedLicenseRepository = detectedLicenseRepository;
        this.lpvsPullRequestRepository = lpvsPullRequestRepository;
        this.licenseRepository = licenseRepository;
        this.lpvsLoginCheckService = lpvsLoginCheckService;
        this.lpvsStatisticsService = lpvsStatisticsService;
    }

    @RequestMapping("/api/v1/web")
    @RestController
    class PublicInterface {
        @GetMapping("/user/info")
        @ResponseBody
        public LPVSMember personalInfoSettings(Authentication authentication) {
            lpvsLoginCheckService.loginVerification(authentication);
            return lpvsLoginCheckService.getMemberFromMemberMap(authentication);
        }

        @GetMapping("/user/login")
        @ResponseBody
        public LPVSLoginMember loginMember(Authentication authentication) {
            Map<String, Object> oauthLoginMemberMap = lpvsLoginCheckService.getOauthLoginMemberMap(authentication);
            boolean isLoggedIn = oauthLoginMemberMap == null || oauthLoginMemberMap.isEmpty();
            if (!isLoggedIn) {
                LPVSMember findMember = lpvsLoginCheckService.getMemberFromMemberMap(authentication);
                return new LPVSLoginMember(!isLoggedIn, findMember);
            } else {
                return new LPVSLoginMember(!isLoggedIn, null);
            }
        }

        @PostMapping("/user/update")
        public ResponseEntity<LPVSMember> postSettingTest(@RequestBody Map<String, String> map, Authentication authentication) {
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

        @ResponseBody
        @GetMapping("/history/{type}/{name}")
        public HistoryEntity newHistoryPageByUser(@PathVariable("type") String type,
                                                  @PathVariable("name") String name,
                                                  @PageableDefault(size = 5, sort = "date",
                                                          direction = Sort.Direction.DESC) Pageable pageable, Authentication authentication) {

            HistoryPageEntity historyPageEntity = lpvsLoginCheckService.pathCheck(type, name, pageable, authentication);
            Page<LPVSPullRequest> prPage = historyPageEntity.getPrPage();
            Long count = historyPageEntity.getCount();

            List<LPVSHistory> lpvsHistories = new ArrayList<>();
            List<LPVSPullRequest> lpvsPullRequests = prPage.getContent();

            for (LPVSPullRequest pr : lpvsPullRequests) {
                String[] pullNumberTemp = pr.getPullRequestUrl().split("/");
                LocalDateTime localDateTime = new Timestamp(pr.getDate().getTime()).toLocalDateTime();
                String formattingDateTime = lpvsLoginCheckService.dateTimeFormatting(localDateTime);

                Boolean hasIssue = detectedLicenseRepository.existsIssue(pr);

                lpvsHistories.add(new LPVSHistory(formattingDateTime, pr.getRepositoryName(), pr.getId(),
                        pr.getPullRequestUrl(), pr.getStatus(), pr.getSender(),
                        pullNumberTemp[pullNumberTemp.length - 2] + "/" +
                                pullNumberTemp[pullNumberTemp.length - 1], hasIssue));
            }

            HistoryEntity historyEntity = new HistoryEntity(lpvsHistories, count);
            return historyEntity;
        }

        @ResponseBody
        @GetMapping("/result/{prId}")
        public LPVSResult resultPage(@PathVariable("prId") Long prId, @PageableDefault(size = 5, sort = "id",
                direction = Sort.Direction.ASC) Pageable pageable, Authentication authentication) {

            lpvsLoginCheckService.loginVerification(authentication);
            //LPVSMember findMember = lpvsLoginCheckService.getMemberFromMemberMap(authentication);

            LPVSPullRequest pr = lpvsPullRequestRepository.findById(prId).get();
            List<LPVSLicense> distinctByLicense = detectedLicenseRepository.findDistinctByLicense(pr);
            List<String> detectedLicenses = new ArrayList<>();
            Map<String, Integer> licenseCountMap = new HashMap<>();

            List<String> allSpdxId = licenseRepository.takeAllSpdxId();
            for (String spdxId : allSpdxId) {
                licenseCountMap.put(spdxId, 0);
            }
            for (LPVSLicense lpvsLicense : distinctByLicense) {
                detectedLicenses.add(lpvsLicense.getSpdxId());
            }

            LPVSResultInfo lpvsResultInfo = new LPVSResultInfo(pr.getId(), pr.getDate(), pr.getRepositoryName(),
                    pr.getStatus(), detectedLicenses);

            Page<LPVSDetectedLicense> dlPage = detectedLicenseRepository.findByPullRequest(pr, pageable);
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
                lpvsResultFileList.add(new LPVSResultFile(dl.getId(), dl.getFilePath(),
                        dl.getComponentFileUrl(), dl.getLines(), dl.getMatch(),
                        status, licenseSpdxId));
            }

            for (LPVSDetectedLicense dl : dlList) {
                if (dl.getLicense() != null) {
                    licenseSpdxId = dl.getLicense().getSpdxId();
                    licenseCountMap.put(licenseSpdxId,
                            licenseCountMap.get(licenseSpdxId) + 1);
                }
            }

            Long count = detectedLicenseRepository.CountByDetectedLicenseWherePullRequestId(pr);
            String[] tempPullNumber = pr.getPullRequestUrl().split("/");
            LPVSResult lpvsResult = new LPVSResult(lpvsResultFileList, lpvsResultInfo, count, licenseCountMap,
                    tempPullNumber[tempPullNumber.length-2] + '/' +
                            tempPullNumber[tempPullNumber.length-1], hasIssue);
            return lpvsResult;
        }

        @ResponseBody
        @GetMapping("dashboard/{type}/{name}")
        public Dashboard dashboardPage(@PathVariable("type") String type,
                                       @PathVariable("name") String name,
                                       Authentication authentication) {

            Dashboard dashboardEntity = lpvsStatisticsService.getDashboardEntity(type, name, authentication);

            return dashboardEntity;
        }
    }

    @GetMapping("/error")
    public String redirect(){
        return "index.html";
    }
}
