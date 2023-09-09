package com.lpvs.controller;

import com.lpvs.entity.LPVSLoginMember;
import com.lpvs.entity.LPVSMember;
import com.lpvs.repository.LPVSDetectedLicenseRepository;
import com.lpvs.repository.LPVSLicenseRepository;
import com.lpvs.repository.LPVSMemberRepository;
import com.lpvs.repository.LPVSPullRequestRepository;
import com.lpvs.service.LoginCheckService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@Slf4j
public class LPVSWebController {

    private LPVSMemberRepository memberRepository;
    private LPVSDetectedLicenseRepository detectedLicenseRepository;
    private LPVSPullRequestRepository lpvsPullRequestRepository;
    private LPVSLicenseRepository licenseRepository;
    private LoginCheckService loginCheckService;

    public LPVSWebController(LPVSMemberRepository memberRepository, LPVSDetectedLicenseRepository detectedLicenseRepository,
                             LPVSPullRequestRepository lpvsPullRequestRepository, LPVSLicenseRepository licenseRepository,
                             LoginCheckService loginCheckService) {
        this.memberRepository = memberRepository;
        this.detectedLicenseRepository = detectedLicenseRepository;
        this.lpvsPullRequestRepository = lpvsPullRequestRepository;
        this.licenseRepository = licenseRepository;
        this.loginCheckService = loginCheckService;
    }

    @GetMapping("user/info")
    @ResponseBody
    public LPVSMember personalInfoSettings(Authentication authentication) {

        loginCheckService.loginVerification(authentication);

        return loginCheckService.getMemberFromMemberMap(authentication);
    }


    @GetMapping("login/check")
    @ResponseBody
    public LPVSLoginMember loginMember(Authentication authentication) {
        Boolean isLoggedIn = loginCheckService.oauthLoginStatus(authentication);
        if (isLoggedIn) {
            LPVSMember findMember = loginCheckService.getMemberFromMemberMap(authentication);
            return new LPVSLoginMember(isLoggedIn, findMember);
        } else {
            return new LPVSLoginMember(isLoggedIn, null);
        }
    }

}
