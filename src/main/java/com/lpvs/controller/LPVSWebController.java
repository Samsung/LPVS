/**
 * Copyright 2023 Basaeng, kyudori, hwan5180, quswjdgma83
 *
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */

package com.lpvs.controller;

import com.lpvs.entity.*;
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

    public LPVSWebController(LPVSMemberRepository memberRepository, LPVSDetectedLicenseRepository detectedLicenseRepository,
                             LPVSPullRequestRepository lpvsPullRequestRepository, LPVSLicenseRepository licenseRepository,
                             LPVSLoginCheckService LPVSLoginCheckService) {
        this.memberRepository = memberRepository;
        this.detectedLicenseRepository = detectedLicenseRepository;
        this.lpvsPullRequestRepository = lpvsPullRequestRepository;
        this.licenseRepository = licenseRepository;
        this.lpvsLoginCheckService = LPVSLoginCheckService;
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
                        pullNumberTemp[pullNumberTemp.length-2] + "/" +
                                pullNumberTemp[pullNumberTemp.length-1], hasIssue));
            }

            HistoryEntity historyEntity = new HistoryEntity(lpvsHistories, count);
            return historyEntity;
        }
    }

    @GetMapping("/error")
    public String redirect(){
        return "index.html";
    }
}
