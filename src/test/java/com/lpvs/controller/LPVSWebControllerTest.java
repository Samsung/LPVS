/**
 * Copyright (c) 2023-2024, Samsung Electronics Co., Ltd. All rights reserved.
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
import com.lpvs.repository.LPVSDetectedLicenseRepository;
import com.lpvs.repository.LPVSLicenseRepository;
import com.lpvs.repository.LPVSMemberRepository;
import com.lpvs.repository.LPVSPullRequestRepository;
import com.lpvs.service.LPVSLoginCheckService;
import com.lpvs.service.LPVSStatisticsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class LPVSWebControllerTest {

    @InjectMocks private LPVSWebController webController;

    @Mock private LPVSMemberRepository memberRepository;

    @Mock private LPVSDetectedLicenseRepository detectedLicenseRepository;

    @Mock private LPVSPullRequestRepository lpvsPullRequestRepository;

    @Mock private LPVSLicenseRepository licenseRepository;

    @Mock private LPVSLoginCheckService loginCheckService;

    @Mock private LPVSStatisticsService statisticsService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testPersonalInfoSettings() {
        Authentication authentication = mock(Authentication.class);
        LPVSMember member =
                new LPVSMember(1L, "testUser", "test@example.com", "provider", "nickName");

        when(loginCheckService.getMemberFromMemberMap(authentication)).thenReturn(member);

        LPVSMember result =
                webController.new WebApiEndpoints().personalInfoSettings(authentication).getBody();

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("test@example.com", result.getEmail());
        assertEquals("testUser", result.getName());
        assertEquals("provider", result.getProvider());
        assertEquals("nickName", result.getNickname());
    }

    @Test
    public void testLoginMemberLoggedIn() {
        Authentication authentication = mock(Authentication.class);
        LPVSMember member = new LPVSMember(1L, "testUser", "test@example.com", "OAuth", "nickName");
        Map<String, Object> oauthLoginMemberMap = new HashMap<>();
        oauthLoginMemberMap.put("email", "test@example.com");
        oauthLoginMemberMap.put("provider", "OAuth");
        when(loginCheckService.getOauthLoginMemberMap(authentication))
                .thenReturn(oauthLoginMemberMap);
        when(loginCheckService.getMemberFromMemberMap(authentication)).thenReturn(member);
        LPVSLoginMember result =
                webController.new WebApiEndpoints().loginMember(authentication).getBody();
        assertNotNull(result);
        assertNotNull(result.getMember());
        assertEquals(1L, result.getMember().getId());
        assertEquals("test@example.com", result.getMember().getEmail());
        assertEquals("testUser", result.getMember().getName());
        assertEquals("nickName", result.getMember().getNickname());
        assertEquals("OAuth", result.getMember().getProvider());
    }

    @Test
    public void testLoginMemberNotLoggedIn() {
        Authentication authentication = mock(Authentication.class);
        when(loginCheckService.getOauthLoginMemberMap(authentication)).thenReturn(null);
        LPVSLoginMember result =
                webController.new WebApiEndpoints().loginMember(authentication).getBody();
        assertNotNull(result);
        assertNull(result.getMember());
    }

    @Test
    public void testPostSettingTestSuccess() {
        Authentication authentication = mock(Authentication.class);
        LPVSMember member = new LPVSMember(1L, "testUser", "test@example.com", "OAuth", "nickName");
        member.setNickname("testUser");
        member.setOrganization("TestOrg");
        Map<String, String> map = new HashMap<>();
        map.put("nickname", "UpdatedUser");
        map.put("organization", "UpdatedOrg");
        when(loginCheckService.getMemberFromMemberMap(authentication)).thenReturn(member);
        when(memberRepository.saveAndFlush(member)).thenReturn(member);
        ResponseEntity<LPVSMember> responseEntity =
                webController.new WebApiEndpoints().postSettingTest(map, authentication);
        assertNotNull(responseEntity);
        assertEquals(responseEntity.getBody(), member);
        assertEquals("UpdatedUser", member.getNickname());
        assertEquals("UpdatedOrg", member.getOrganization());
    }

    @Test
    public void testPostSettingTestDataIntegrityViolationException() {
        Authentication authentication = mock(Authentication.class);
        LPVSMember member = new LPVSMember(1L, "testUser", "test@example.com", "OAuth", "nickName");
        member.setNickname("testUser");
        member.setOrganization("TestOrg");
        Map<String, String> map = new HashMap<>();
        map.put("nickname", "UpdatedUser");
        map.put("organization", "UpdatedOrg");
        when(loginCheckService.getMemberFromMemberMap(authentication)).thenReturn(member);
        when(memberRepository.saveAndFlush(member))
                .thenThrow(new DataIntegrityViolationException("DuplicatedKeyException"));
        assertThrows(
                IllegalArgumentException.class,
                () -> webController.new WebApiEndpoints().postSettingTest(map, authentication));
    }

    @Test
    public void testNewHistoryPageByUser() {
        Authentication authentication = mock(Authentication.class);
        Pageable pageable = PageRequest.of(0, 5, Sort.by(Sort.Order.desc("date")));
        String type = "own";
        String name = "testUser";

        LPVSPullRequest pullRequest = new LPVSPullRequest();
        pullRequest.setId(1L);
        pullRequest.setPullRequestUrl("https://github.com/testUser/testRepo/pull/1");
        pullRequest.setRepositoryName("testUser/testRepo");
        pullRequest.setDate(Timestamp.valueOf(LocalDateTime.now()));
        pullRequest.setStatus("Open");
        pullRequest.setSender("testUser");

        List<LPVSPullRequest> pullRequests = new ArrayList<>();
        pullRequests.add(pullRequest);
        Page<LPVSPullRequest> prPage = new PageImpl<>(pullRequests);

        when(loginCheckService.pathCheck(type, name, pageable, authentication))
                .thenReturn(new HistoryPageEntity(prPage, 1L));
        when(detectedLicenseRepository.existsByIssueIsTrueAndPullRequest(pullRequest))
                .thenReturn(false);

        HistoryEntity result =
                webController.new WebApiEndpoints()
                        .newHistoryPageByUser(type, name, pageable, authentication)
                        .getBody();

        assertNotNull(result);
        assertEquals(1, result.getCount());
        assertNotNull(result.getLpvsHistories());
        assertEquals(1, result.getLpvsHistories().size());

        LPVSHistory history = result.getLpvsHistories().get(0);
        assertEquals("Open", history.getStatus());
        assertEquals("testUser/testRepo", history.getRepositoryName());
        assertEquals("testUser", history.getSender());
    }

    @Test
    public void testResultPage() {
        Authentication authentication = mock(Authentication.class);
        Pageable pageable = PageRequest.of(0, 5, Sort.by(Sort.Order.asc("id")));
        Long prId = 1L;

        LPVSPullRequest pullRequest = new LPVSPullRequest();
        pullRequest.setId(1L);
        pullRequest.setPullRequestUrl("https://github.com/testUser/testRepo/pull/1");
        pullRequest.setRepositoryName("testUser/testRepo");
        pullRequest.setStatus("Open");
        pullRequest.setDate(Timestamp.valueOf(LocalDateTime.now()));

        LPVSDetectedLicense detectedLicense1 = new LPVSDetectedLicense();
        detectedLicense1.setId(1L);
        detectedLicense1.setFilePath("file1.txt");
        detectedLicense1.setComponentFileUrl("https://github.com/testUser/testRepo/file1.txt");
        detectedLicense1.setLines("10");
        detectedLicense1.setMatch("90%");
        detectedLicense1.setLicense(new LPVSLicense());
        detectedLicense1.getLicense().setSpdxId("MIT");
        detectedLicense1.getLicense().setAccess("Allowed");

        LPVSDetectedLicense detectedLicense2 = new LPVSDetectedLicense();
        detectedLicense2.setId(2L);
        detectedLicense2.setFilePath("file2.txt");
        detectedLicense2.setComponentFileUrl("https://github.com/testUser/testRepo/file2.txt");
        detectedLicense2.setLines("20");
        detectedLicense2.setMatch("80%");
        detectedLicense2.setLicense(null);

        List<LPVSDetectedLicense> detectedLicenses = new ArrayList<>();
        detectedLicenses.add(detectedLicense1);
        detectedLicenses.add(detectedLicense2);

        LPVSLicense lic1 = new LPVSLicense(1L, "MIT", "MIT", "PERMITTED", "mit", "");
        LPVSLicense lic2 =
                new LPVSLicense(2L, "Apache-2.0", "Apache-2.0", "PERMITTED", "apache", "");
        List<LPVSLicense> licenses = new ArrayList<>();
        licenses.add(lic1);
        licenses.add(lic2);

        when(loginCheckService.getMemberFromMemberMap(authentication)).thenReturn(new LPVSMember());
        when(lpvsPullRequestRepository.findById(prId)).thenReturn(Optional.of(pullRequest));
        when(detectedLicenseRepository.findDistinctLicenseByPullRequest(pullRequest))
                .thenReturn(licenses);
        when(licenseRepository.findAllSpdxId()).thenReturn(List.of("MIT", "Apache-2.0"));
        when(detectedLicenseRepository.findByPullRequest(pullRequest, pageable))
                .thenReturn(new PageImpl<>(detectedLicenses));
        when(detectedLicenseRepository.findByPullRequest(pullRequest)).thenReturn(detectedLicenses);
        when(detectedLicenseRepository.countByPullRequestAndLicenseIsNotNull(pullRequest))
                .thenReturn(1L);

        LPVSResult result =
                webController.new WebApiEndpoints()
                        .resultPage(prId, pageable, authentication)
                        .getBody();

        assertNotNull(result);
        assertNotNull(result.getLpvsResultInfo());
        assertNotNull(result.getLpvsResultFileList());
        assertEquals(2, result.getLpvsResultFileList().size());
    }

    @Test
    public void testResultPageNull() {
        Authentication authentication = mock(Authentication.class);
        Pageable pageable = PageRequest.of(0, 5, Sort.by(Sort.Order.asc("id")));

        when(loginCheckService.getMemberFromMemberMap(authentication)).thenReturn(new LPVSMember());
        when(lpvsPullRequestRepository.findById(1L)).thenReturn(Optional.empty());

        ResponseEntity<LPVSResult> responseEntity =
                webController.new WebApiEndpoints().resultPage(1L, pageable, authentication);

        assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode());
        assertNull(responseEntity.getBody());
    }

    @Test
    public void testDashboardPage() {
        Authentication authentication = mock(Authentication.class);
        String type = "own";
        String name = "testUser";
        Map<String, Integer> licenseCountMap = new HashMap<>();
        licenseCountMap.put("License1", 10);
        licenseCountMap.put("License2", 5);
        Dashboard mockDashboard =
                new Dashboard("Test Dashboard", licenseCountMap, 100, 20, 30, 50, 10, null);
        when(statisticsService.getDashboardEntity(type, name, authentication))
                .thenReturn(mockDashboard);
        Dashboard dashboard =
                webController.new WebApiEndpoints()
                        .dashboardPage(type, name, authentication)
                        .getBody();
        assertNotNull(dashboard);
    }

    @Test
    void testSanitizeUserInputsForMember() {
        LPVSMember member = new LPVSMember();
        member.setNickname("<script>alert('XSS')</script>");
        member.setOrganization("<div>LPVS</div>");

        LPVSWebController.sanitizeUserInputs(member);

        assertEquals("&lt;script&gt;alert(&#39;XSS&#39;)&lt;/script&gt;", member.getNickname());
        assertEquals("&lt;div&gt;LPVS&lt;/div&gt;", member.getOrganization());
    }

    @Test
    void testSanitizeUserInputsForMemberWithNullValues() {
        LPVSMember member = new LPVSMember();
        LPVSWebController.sanitizeUserInputs(member);

        assertNull(member.getNickname());
        assertNull(member.getOrganization());

        LPVSMember memberNull = null;
        LPVSWebController.sanitizeUserInputs(memberNull);
        assertNull(memberNull);
    }

    @Test
    void testSanitizeUserInputsForPullRequest() {
        LPVSPullRequest pr = new LPVSPullRequest();
        pr.setRepositoryName("<script>alert('XSS')</script>");
        pr.setPullRequestUrl("<a href='malicious-link'>Click me</a>");
        pr.setStatus("<img src='invalid-image' onerror='alert(\"XSS\")'>");
        pr.setSender("<iframe src='malicious-site'></iframe>");

        LPVSWebController.sanitizeUserInputs(pr);

        assertEquals("&lt;script&gt;alert(&#39;XSS&#39;)&lt;/script&gt;", pr.getRepositoryName());
        assertEquals(
                "&lt;a href=&#39;malicious-link&#39;&gt;Click me&lt;/a&gt;",
                pr.getPullRequestUrl());
        assertEquals(
                "&lt;img src=&#39;invalid-image&#39; onerror=&#39;alert(&quot;XSS&quot;)&#39;&gt;",
                pr.getStatus());
        assertEquals("&lt;iframe src=&#39;malicious-site&#39;&gt;&lt;/iframe&gt;", pr.getSender());
    }

    @Test
    void testSanitizeUserInputsForPullRequestWithNullValues() {
        LPVSPullRequest pr = new LPVSPullRequest();
        LPVSWebController.sanitizeUserInputs(pr);

        assertNull(pr.getRepositoryName());
        assertNull(pr.getPullRequestUrl());
        assertNull(pr.getStatus());
        assertNull(pr.getSender());

        LPVSPullRequest prNull = null;
        LPVSWebController.sanitizeUserInputs(prNull);
        assertNull(prNull);
    }
}
