/**
 * Copyright 2023 Basaeng, kyudori, hwan5180, quswjdgma83
 *
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */

package com.lpvs.controller;

import com.lpvs.entity.LPVSLoginMember;
import com.lpvs.entity.LPVSMember;
import com.lpvs.repository.LPVSDetectedLicenseRepository;
import com.lpvs.repository.LPVSLicenseRepository;
import com.lpvs.repository.LPVSMemberRepository;
import com.lpvs.repository.LPVSPullRequestRepository;
import com.lpvs.service.LPVSLoginCheckService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Controller
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

    @GetMapping("user/info")
    @ResponseBody
    public LPVSMember personalInfoSettings(Authentication authentication) {
        lpvsLoginCheckService.loginVerification(authentication);
        return lpvsLoginCheckService.getMemberFromMemberMap(authentication);
    }

    @GetMapping("login/check")
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

    @PostMapping("user/update")
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

    @GetMapping("error")
    public String redirect(){
        return "index.html";
    }
}
