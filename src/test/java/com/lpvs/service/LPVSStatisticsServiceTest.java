/**
 * Copyright (c) 2023, Samsung Electronics Co., Ltd. All rights reserved.
 *
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */
package com.lpvs.service;

import com.lpvs.entity.LPVSMember;
import com.lpvs.entity.LPVSPullRequest;
import com.lpvs.entity.enums.Grade;
import com.lpvs.repository.LPVSPullRequestRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.Authentication;
import java.util.ArrayList;
import java.util.Collections;
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
