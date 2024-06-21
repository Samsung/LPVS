/**
 * Copyright 2023 Basaeng, kyudori, hwan5180, quswjdgma83
 *
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */
package com.lpvs.service;

import com.lpvs.entity.LPVSDetectedLicense;
import com.lpvs.entity.auth.LPVSMember;
import com.lpvs.entity.LPVSPullRequest;
import com.lpvs.entity.dashboard.Dashboard;
import com.lpvs.entity.dashboard.DashboardElementsByDate;
import com.lpvs.entity.enums.Grade;
import com.lpvs.exception.WrongAccessException;
import com.lpvs.repository.LPVSDetectedLicenseRepository;
import com.lpvs.repository.LPVSLicenseRepository;
import com.lpvs.repository.LPVSMemberRepository;
import com.lpvs.repository.LPVSPullRequestRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

/**
 * Service for managing statistics related to LPVSPullRequest entities.
 */
@Service
@Slf4j
public class LPVSStatisticsService {
    /**
     * Repository for storing LPVSPullRequest entities.
     */
    private LPVSPullRequestRepository lpvsPullRequestRepository;

    /**
     * Repository for storing LPVSDetectedLicense entities.
     */
    private LPVSDetectedLicenseRepository lpvsDetectedLicenseRepository;

    /**
     * Service for checking user authentication and obtaining LPVSMember.
     */
    private LPVSLoginCheckService loginCheckService;

    /**
     * Repository for storing LPVSLicense entities.
     */
    private LPVSLicenseRepository lpvsLicenseRepository;

    /**
     * Repository for storing LPVSMember entities.
     */
    private LPVSMemberRepository memberRepository;

    /**
     * Constructor for LPVSStatisticsService.
     *
     * @param lpvsPullRequestRepository      Repository for storing LPVSPullRequest entities.
     * @param lpvsDetectedLicenseRepository  Repository for storing LPVSDetectedLicense entities.
     * @param loginCheckService              Service for checking user authentication and obtaining LPVSMember.
     * @param lpvsLicenseRepository          Repository for storing LPVSLicense entities.
     * @param memberRepository               Repository for storing LPVSMember entities.
     */
    public LPVSStatisticsService(
            LPVSPullRequestRepository lpvsPullRequestRepository,
            LPVSDetectedLicenseRepository lpvsDetectedLicenseRepository,
            LPVSLoginCheckService loginCheckService,
            LPVSLicenseRepository lpvsLicenseRepository,
            LPVSMemberRepository memberRepository) {
        this.lpvsPullRequestRepository = lpvsPullRequestRepository;
        this.lpvsDetectedLicenseRepository = lpvsDetectedLicenseRepository;
        this.loginCheckService = loginCheckService;
        this.lpvsLicenseRepository = lpvsLicenseRepository;
        this.memberRepository = memberRepository;
    }

    /**
     * Performs a path check based on the specified type and name.
     *
     * @param type           The type ("own", "org", or "send").
     * @param name           The name associated with the path.
     * @param authentication The authentication details.
     * @return A list of LPVSPullRequest entities based on the path check.
     */
    public List<LPVSPullRequest> pathCheck(
            String type, String name, Authentication authentication) {
        LPVSMember findMember = loginCheckService.getMemberFromMemberMap(authentication);

        String findNickName = findMember.getNickname();
        String findOrganization = findMember.getOrganization();

        List<LPVSPullRequest> prList;

        if ((type.equals("own") && findNickName.equals(name))
                || (type.equals("org") && findOrganization.equals(name))) {
            prList = lpvsPullRequestRepository.findByPullRequestBase(name);
        } else if (type.equals("send") && findNickName.equals(name)) {
            prList = lpvsPullRequestRepository.findBySenderOrPullRequestHead(name);
        } else {
            throw new WrongAccessException("WrongPathException");
        }

        return prList;
    }

    /**
     * Retrieves a Dashboard entity based on the specified type, name, and authentication details.
     *
     * @param type           The type ("own", "org", or "send").
     * @param name           The name associated with the path.
     * @param authentication The authentication details.
     * @return A Dashboard entity with statistical information.
     */
    public Dashboard getDashboardEntity(String type, String name, Authentication authentication) {

        int totalDetectionCount = 0;
        int highSimilarityCount = 0;
        int totalIssueCount = 0;
        int totalParticipantsCount = 0;
        int totalRepositoryCount = 0;

        List<LPVSPullRequest> prList = pathCheck(type, name, authentication);
        Map<String, Integer> licenseCountMap = new HashMap<>();
        List<DashboardElementsByDate> dashboardByDates = new ArrayList<>();
        Map<LocalDate, List<LPVSPullRequest>> datePrMap = new HashMap<>();

        List<String> allSpdxId = lpvsLicenseRepository.findAllSpdxId();
        for (String spdxId : allSpdxId) {
            licenseCountMap.put(spdxId, 0);
        }

        Set<String> totalSenderSet = new HashSet<>();
        Set<String> totalRepositorySet = new HashSet<>();
        for (LPVSPullRequest pr : prList) {
            LocalDate localDate =
                    pr.getDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            List<LPVSPullRequest> datePrMapValue = datePrMap.get(localDate);
            if (datePrMapValue == null) {
                datePrMapValue = new ArrayList<>();
                datePrMap.put(localDate, datePrMapValue);
            }
            datePrMapValue.add(pr);

            totalSenderSet.add(pr.getSender());
            if (!(pr.getRepositoryName() == null || pr.getRepositoryName().isEmpty())) {
                totalRepositorySet.add(pr.getRepositoryName());
            }
        }
        totalSenderSet.remove(null);

        for (Map.Entry<LocalDate, List<LPVSPullRequest>> entry : datePrMap.entrySet()) {
            Map<Grade, Integer> riskGradeMap = new HashMap<>();
            riskGradeMap = putDefaultriskGradeMap(riskGradeMap);

            Set<String> senderSet = new HashSet<>();
            for (LPVSPullRequest pr : entry.getValue()) {
                List<LPVSDetectedLicense> dlList =
                        lpvsDetectedLicenseRepository.findByPullRequestAndLicenseIsNotNull(pr);
                if (!(pr.getRepositoryName() == null || pr.getRepositoryName().isEmpty())) {
                    senderSet.add(pr.getSender());
                }
                for (LPVSDetectedLicense dl : dlList) {
                    Grade grade = null;
                    if (dl.getMatch() != null) {
                        grade = getGrade(dl.getMatch());
                        riskGradeMap.put(grade, riskGradeMap.getOrDefault(grade, 0) + 1);
                    }
                    if (dl.getLicense() != null) {
                        licenseCountMap.put(
                                dl.getLicense().getSpdxId(),
                                licenseCountMap.get(dl.getLicense().getSpdxId()) + 1);
                    }

                    if (grade == Grade.HIGH) {
                        highSimilarityCount += 1;
                    }
                    if (dl.getIssue()) {
                        totalIssueCount += 1;
                    }
                }
                totalDetectionCount += dlList.size();
            }

            senderSet.remove(null);
            dashboardByDates.add(
                    new DashboardElementsByDate(
                            entry.getKey(),
                            senderSet.size(),
                            entry.getValue().size(),
                            riskGradeMap));
        }

        for (String s : totalSenderSet) {
            log.info(s);
        }

        totalParticipantsCount = totalSenderSet.size();
        totalRepositoryCount = totalRepositorySet.size();
        return new Dashboard(
                name,
                licenseCountMap,
                totalDetectionCount,
                highSimilarityCount,
                totalIssueCount,
                totalParticipantsCount,
                totalRepositoryCount,
                dashboardByDates);
    }

    /**
     * Retrieves the Grade enum based on the specified match value.
     *
     * @param match The match value.
     * @return The Grade enum.
     */
    public Grade getGrade(String match) {
        int matchValue = Integer.parseInt(match.substring(0, match.length() - 1));

        if (matchValue >= 80) {
            return Grade.HIGH;
        } else if (matchValue >= 50) {
            return Grade.MIDDLE;
        } else if (matchValue >= 30) {
            return Grade.LOW;
        } else {
            return Grade.NONE;
        }
    }

    /**
     * Initializes and returns a default risk grade map with Grade enums as keys and 0 as values.
     *
     * @param riskGradeMap The risk grade map to be initialized.
     * @return The initialized risk grade map.
     */
    public Map<Grade, Integer> putDefaultriskGradeMap(Map<Grade, Integer> riskGradeMap) {
        riskGradeMap.put(Grade.HIGH, 0);
        riskGradeMap.put(Grade.MIDDLE, 0);
        riskGradeMap.put(Grade.LOW, 0);
        riskGradeMap.put(Grade.NONE, 0);

        return riskGradeMap;
    }
}
