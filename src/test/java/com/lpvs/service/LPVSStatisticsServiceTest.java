/**
 * Copyright (c) 2023, Samsung Electronics Co., Ltd. All rights reserved.
 *
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */
package com.lpvs.service;

import com.lpvs.entity.LPVSDetectedLicense;
import com.lpvs.entity.LPVSLicense;
import com.lpvs.entity.LPVSMember;
import com.lpvs.entity.LPVSPullRequest;
import com.lpvs.entity.dashboard.Dashboard;
import com.lpvs.entity.enums.Grade;
import com.lpvs.exception.WrongAccessException;
import com.lpvs.repository.LPVSDetectedLicenseRepository;
import com.lpvs.repository.LPVSLicenseRepository;
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
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class LPVSStatisticsServiceTest {

    @InjectMocks
    private LPVSStatisticsService statisticsService;

    @Mock
    private LPVSPullRequestRepository pullRequestRepository;

    @Mock
    private LPVSLoginCheckService loginCheckService;

    @Mock
    private LPVSLicenseRepository licenseRepository;

    @Mock
    private LPVSDetectedLicenseRepository detectedLicenseRepository;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testPathCheckOwnType() {
        Authentication authentication = mock(Authentication.class);
        LPVSMember member = new LPVSMember();
        member.setNickname("testNickname");
        when(loginCheckService.getMemberFromMemberMap(authentication)).thenReturn(member);
        when(pullRequestRepository.findByPullRequestBase("testNickname")).thenReturn(new ArrayList<>());
        List<LPVSPullRequest> result = statisticsService.pathCheck("own", "testNickname", authentication);

        assertNotNull(result);
        assertEquals(0, result.size());
    }

    @Test
    public void testPathCheckOrgType() {
        Authentication authentication = mock(Authentication.class);
        LPVSMember member = new LPVSMember();
        member.setOrganization("testOrganization");
        when(loginCheckService.getMemberFromMemberMap(authentication)).thenReturn(member);
        when(pullRequestRepository.findByPullRequestBase("testOrganization")).thenReturn(Collections.emptyList());
        List<LPVSPullRequest> result = statisticsService.pathCheck("org", "testOrganization", authentication);

        assertNotNull(result);
        assertEquals(0, result.size());
    }

    @Test
    public void testPathCheckSendType() {
        Authentication authentication = mock(Authentication.class);
        LPVSMember member = new LPVSMember();
        member.setNickname("testNickname");
        when(loginCheckService.getMemberFromMemberMap(authentication)).thenReturn(member);
        when(pullRequestRepository.findBySenderOrPullRequestHead("testNickname")).thenReturn(Collections.emptyList());
        List<LPVSPullRequest> result = statisticsService.pathCheck("send", "testNickname", authentication);

        assertNotNull(result);
        assertEquals(0, result.size());
    }

    @Test
    public void testPathCheckWrongPathException() {
        Authentication authentication = mock(Authentication.class);
        LPVSMember member = new LPVSMember();
        member.setNickname("testNickname");
        when(loginCheckService.getMemberFromMemberMap(authentication)).thenReturn(member);
        when(pullRequestRepository.findBySenderOrPullRequestHead("testNickname")).thenReturn(Collections.emptyList());
        Exception exception = assertThrows(WrongAccessException.class, () -> {
            statisticsService.pathCheck("wrongpath", "testNickname", authentication);
        });
        assertEquals("WrongPathException", exception.getMessage());
    }

    @ParameterizedTest
    @ValueSource(strings = {"81 ", "51 ", "31 ", "16 "})
    public void testGetDashboardEntity(String match) {
        Authentication authentication = mock(Authentication.class);
        LPVSMember member = new LPVSMember();
        LPVSPullRequest mockRequest = mock(LPVSPullRequest.class);
        List<LPVSPullRequest> pullRequests = new ArrayList<>(){{ add(mockRequest); }};
        LPVSDetectedLicense detectedLicense = new LPVSDetectedLicense(){{
            setMatch(match);
            setLicense(new LPVSLicense(){{
                setSpdxId("MIT");
            }});
            setIssue(true);
        }};
        member.setNickname("testNickname");
        when(loginCheckService.getMemberFromMemberMap(authentication)).thenReturn(member);
        when(pullRequestRepository.findByPullRequestBase("testNickname")).thenReturn(pullRequests);
        /*List<LPVSPullRequest> result = statisticsService.pathCheck("own", "testNickname", authentication);

        assertNotNull(result);
        assertEquals(0, result.size());*/
        when(licenseRepository.takeAllSpdxId()).thenReturn(Collections.singletonList("MIT"));
        when(mockRequest.getDate()).thenReturn(new Date());
        when(mockRequest.getSender()).thenReturn("");
        when(mockRequest.getRepositoryName()).thenReturn("name");
        when(detectedLicenseRepository.findNotNullDLByPR(mockRequest)).thenReturn(new ArrayList<>(){{
            add(detectedLicense);
        }});

        Dashboard result = statisticsService.getDashboardEntity("own", "testNickname", authentication);
        assertNotNull(result);
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
}
