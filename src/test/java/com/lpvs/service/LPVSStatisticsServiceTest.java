/**
 * Copyright (c) 2023-2024, Samsung Electronics Co., Ltd. All rights reserved.
 *
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */
package com.lpvs.service;

import com.lpvs.entity.LPVSDetectedLicense;
import com.lpvs.entity.LPVSLicense;
import com.lpvs.entity.auth.LPVSMember;
import com.lpvs.entity.LPVSPullRequest;
import com.lpvs.entity.dashboard.Dashboard;
import com.lpvs.entity.enums.Grade;
import com.lpvs.exception.WrongAccessException;
import com.lpvs.repository.LPVSDetectedLicenseRepository;
import com.lpvs.repository.LPVSLicenseRepository;
import com.lpvs.repository.LPVSMemberRepository;
import com.lpvs.repository.LPVSPullRequestRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.Authentication;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class LPVSStatisticsServiceTest {

    @InjectMocks private LPVSStatisticsService statisticsService;

    @Mock private LPVSPullRequestRepository pullRequestRepository;

    @Mock private LPVSLoginCheckService loginCheckService;

    @Mock private LPVSLicenseRepository licenseRepository;

    @Mock private LPVSDetectedLicenseRepository detectedLicenseRepository;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testPathCheckOwnType() {
        Authentication authentication = mock(Authentication.class);
        LPVSMember member = new LPVSMember();
        member.setNickname("testNickname");
        member.setOrganization("testOrgName");
        when(loginCheckService.getMemberFromMemberMap(authentication)).thenReturn(member);
        when(pullRequestRepository.findByPullRequestBase("testNickname"))
                .thenReturn(new ArrayList<>());
        List<LPVSPullRequest> result =
                statisticsService.pathCheck("own", "testNickname", authentication);

        assertNotNull(result);
        assertEquals(0, result.size());
        result = statisticsService.pathCheck("send", "testNickname", authentication);
        assertNotNull(result);
        assertEquals(0, result.size());

        result = statisticsService.pathCheck("org", "testOrgName", authentication);
        assertNotNull(result);
        assertEquals(0, result.size());

        assertThrows(
                WrongAccessException.class,
                () -> statisticsService.pathCheck("test", "testNickname", authentication));
        assertThrows(
                WrongAccessException.class,
                () -> statisticsService.pathCheck("own", "test", authentication));
        assertThrows(
                WrongAccessException.class,
                () -> statisticsService.pathCheck("send", "test", authentication));
        assertThrows(
                WrongAccessException.class,
                () -> statisticsService.pathCheck("org", "test", authentication));
    }

    @Test
    public void testPathCheckOrgType() {
        Authentication authentication = mock(Authentication.class);
        LPVSMember member = new LPVSMember();
        member.setOrganization("testOrganization");
        when(loginCheckService.getMemberFromMemberMap(authentication)).thenReturn(member);
        when(pullRequestRepository.findByPullRequestBase("testOrganization"))
                .thenReturn(Collections.emptyList());
        List<LPVSPullRequest> result =
                statisticsService.pathCheck("org", "testOrganization", authentication);

        assertNotNull(result);
        assertEquals(0, result.size());
    }

    @Test
    public void testPathCheckSendType() {
        Authentication authentication = mock(Authentication.class);
        LPVSMember member = new LPVSMember();
        member.setNickname("testNickname");
        when(loginCheckService.getMemberFromMemberMap(authentication)).thenReturn(member);
        when(pullRequestRepository.findBySenderOrPullRequestHead("testNickname"))
                .thenReturn(Collections.emptyList());
        List<LPVSPullRequest> result =
                statisticsService.pathCheck("send", "testNickname", authentication);

        assertNotNull(result);
        assertEquals(0, result.size());
    }

    @Test
    public void testPathCheckWrongPathException() {
        Authentication authentication = mock(Authentication.class);
        LPVSMember member = new LPVSMember();
        member.setNickname("testNickname");
        when(loginCheckService.getMemberFromMemberMap(authentication)).thenReturn(member);
        when(pullRequestRepository.findBySenderOrPullRequestHead("testNickname"))
                .thenReturn(Collections.emptyList());
        Exception exception =
                assertThrows(
                        WrongAccessException.class,
                        () -> {
                            statisticsService.pathCheck(
                                    "wrongpath", "testNickname", authentication);
                        });
        assertEquals("WrongPathException", exception.getMessage());
    }

    @ParameterizedTest
    @ValueSource(strings = {"81 ", "51 ", "31 ", "16 "})
    public void testGetDashboardEntity(String match) {
        Authentication authentication = mock(Authentication.class);
        LPVSMember member = new LPVSMember();
        LPVSPullRequest mockRequest = mock(LPVSPullRequest.class);
        List<LPVSPullRequest> pullRequests =
                new ArrayList<>() {
                    {
                        add(mockRequest);
                    }
                };
        LPVSDetectedLicense detectedLicense1 =
                new LPVSDetectedLicense() {
                    {
                        setMatch(match);
                        setLicense(
                                new LPVSLicense() {
                                    {
                                        setSpdxId("MIT");
                                    }
                                });
                        setIssue(true);
                    }
                };
        LPVSDetectedLicense detectedLicense2 =
                new LPVSDetectedLicense() {
                    {
                        setIssue(false);
                    }
                };
        member.setNickname("testNickname");
        when(loginCheckService.getMemberFromMemberMap(authentication)).thenReturn(member);
        when(pullRequestRepository.findByPullRequestBase("testNickname")).thenReturn(pullRequests);
        when(licenseRepository.findAllSpdxId()).thenReturn(Collections.singletonList("MIT"));
        when(mockRequest.getDate()).thenReturn(new Date());
        when(mockRequest.getSender()).thenReturn("");
        when(mockRequest.getRepositoryName()).thenReturn("name");
        when(detectedLicenseRepository.findByPullRequestAndLicenseIsNotNull(mockRequest))
                .thenReturn(
                        new ArrayList<>() {
                            {
                                add(detectedLicense1);
                                add(detectedLicense2);
                            }
                        });

        Dashboard result =
                statisticsService.getDashboardEntity("own", "testNickname", authentication);
        assertNotNull(result);
    }

    @Test
    public void testGetDashboardEntityWithDummyPRList() {
        Authentication authentication = mock(Authentication.class);
        LPVSMember member = new LPVSMember();
        member.setNickname("testNickname");
        List<LPVSPullRequest> prList = createMockPullRequestList();
        List<LPVSDetectedLicense> dlList = Collections.emptyList();
        when(licenseRepository.findAllSpdxId()).thenReturn(Collections.<String>emptyList());
        when(pullRequestRepository.findByPullRequestBase("testNickname")).thenReturn(prList);
        when(detectedLicenseRepository.findByPullRequestAndLicenseIsNotNull(any()))
                .thenReturn(dlList);
        LPVSStatisticsServiceHelper statisticsServiceHelper =
                new LPVSStatisticsServiceHelper(
                        pullRequestRepository,
                        detectedLicenseRepository,
                        loginCheckService,
                        licenseRepository,
                        null,
                        prList);

        Dashboard dashboard =
                statisticsServiceHelper.getDashboardEntity("send", "testNickname", authentication);

        assertNotNull(dashboard);
        assertEquals("testNickname", dashboard.getName());
        assertEquals(1, dashboard.getDashboardElementsByDates().size());
    }

    @Test
    public void testGetGradeHigh() {
        Grade grade = statisticsService.getGrade("80%");
        assertEquals(Grade.HIGH, grade);
    }

    @Test
    public void testGetGradeMiddle() {
        Grade grade = statisticsService.getGrade("60%");
        assertEquals(Grade.MIDDLE, grade);
    }

    @Test
    public void testGetGradeLow() {
        Grade grade = statisticsService.getGrade("35%");
        assertEquals(Grade.LOW, grade);
    }

    @Test
    public void testGetGradeNone() {
        Grade grade = statisticsService.getGrade("10%");
        assertEquals(Grade.NONE, grade);
    }

    private List<LPVSPullRequest> createMockPullRequestList() {

        LPVSPullRequest pullRequest1 = new LPVSPullRequest();
        LPVSPullRequest pullRequest2 = new LPVSPullRequest();
        LPVSPullRequest pullRequest3 = new LPVSPullRequest();

        pullRequest1.setDate(new Date());
        pullRequest1.setRepositoryName("SampleRepository1");
        pullRequest1.setPullRequestUrl("https://url.com/user/repo/pull/1");
        pullRequest1.setPullRequestFilesUrl("https://url.com/user/repo/pull/1/files");
        pullRequest2.setDate(new Date());
        pullRequest2.setRepositoryName("SampleRepository2");
        pullRequest2.setPullRequestUrl("https://url.com/user/repo/pull/2");
        pullRequest2.setPullRequestFilesUrl("https://url.com/user/repo/pull/2/files");
        pullRequest3.setDate(new Date());
        pullRequest3.setRepositoryName("");
        pullRequest3.setPullRequestUrl("https://url.com/user/repo/pull/2");
        pullRequest3.setPullRequestFilesUrl("https://url.com/user/repo/pull/2/files");

        List<LPVSPullRequest> pullRequestList = Arrays.asList(pullRequest1, pullRequest2);

        return pullRequestList;
    }

    /**
     * Helper class to mock `LPVSStatisticsService`
     * we can't mock intenal call of  `pathCheck()` method
     *
     */
    public class LPVSStatisticsServiceHelper extends LPVSStatisticsService {

        List<LPVSPullRequest> pullRequestList;

        public LPVSStatisticsServiceHelper(
                LPVSPullRequestRepository lpvsPullRequestRepository,
                LPVSDetectedLicenseRepository lpvsDetectedLicenseRepository,
                LPVSLoginCheckService loginCheckService,
                LPVSLicenseRepository lpvsLicenseRepository,
                LPVSMemberRepository memberRepository,
                List<LPVSPullRequest> pullRequestList) {
            super(
                    lpvsPullRequestRepository,
                    lpvsDetectedLicenseRepository,
                    loginCheckService,
                    lpvsLicenseRepository,
                    memberRepository);
            this.pullRequestList = pullRequestList;
        }

        @Override
        public List<LPVSPullRequest> pathCheck(
                String type, String name, Authentication authentication) {
            return pullRequestList;
        }
    }
}
