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
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Map;

@Controller
public class LPVSWebController {
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

}
