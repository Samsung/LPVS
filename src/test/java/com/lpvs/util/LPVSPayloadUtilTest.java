/**
 * Copyright (c) 2023-2025, Samsung Electronics Co., Ltd. All rights reserved.
 *
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */
package com.lpvs.util;

import com.lpvs.entity.LPVSLicense;
import com.lpvs.entity.LPVSQueue;
import com.lpvs.entity.enums.LPVSPullRequestAction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.kohsuke.github.GHCommitPointer;
import org.kohsuke.github.GHPullRequest;
import org.kohsuke.github.GHRepository;
import org.mockito.Mock;
import org.springframework.http.HttpHeaders;

import java.io.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class LPVSPayloadUtilTest {

    @Nested
    class TestConvertOsoriDbResponseToLicenseAlternative {

        @Test
        public void testConvertOsoriDbResponseToLicenseAlternative_Green() {
            String payload =
                    "{\"name\":\"Apache License 2.0\",\"obligationDisclosingSrc\":\"NONE\",\"obligationNotification\":true,\"spdxIdentifier\":\"Apache-2.0\",\"webpage\":\"https://spdx.org/licenses/Apache-2.0.html\",\"description\":\"To distribute the software, copyright and license notice is required.\\n\\n\",\"descriptionKo\":null,\"licenseText\":\"Apache License\\nVersion 2.0, ....\",\"osiApproval\":true,\"nicknames\":[\"Apache Public License 2.0\",\"http://www.apache.org/licenses/LICENSE-2.0\",\"Apache License Version 2.0\"],\"restrictions\":[],\"trafficLight\":\"🟢\",\"note\":null}";
            LPVSLicense expectedLicense = new LPVSLicense();
            expectedLicense.setLicenseName("Apache License 2.0");
            expectedLicense.setSpdxId("Apache-2.0");
            expectedLicense.setAccess("PERMITTED");
            expectedLicense.setAlternativeNames("Apache Public License 2.0,http://www.apache.org/licenses/LICENSE-2.0,Apache License Version 2.0");

            LPVSLicense actualLicense = LPVSPayloadUtil.convertOsoriDbResponseToLicenseAlternative(payload);

            assertNotNull(actualLicense);
            assertEquals(expectedLicense.getLicenseName(), actualLicense.getLicenseName());
            assertEquals(expectedLicense.getSpdxId(), actualLicense.getSpdxId());
            assertEquals(expectedLicense.getAccess(), actualLicense.getAccess());
            assertEquals(
                    expectedLicense.getAlternativeNames(), actualLicense.getAlternativeNames());
        }

        @Test
        public void testConvertOsoriDbResponseToLicenseAlternative_Red() {
            String payload =
                    "{\"name\":\"GNU General Public License v3.0 only\",\"obligationDisclosingSrc\":\"SOURCE\",\"obligationNotification\":true,\"spdxIdentifier\":\"GPL-3.0-only\",\"webpage\":\"https://spdx.org/licenses/GPL-3.0-only.html\",\"description\":\"Distribute the software and its source code.\\n\\n\",\"descriptionKo\":null,\"licenseText\":\"GNU GENERAL PUBLIC LICENSE\\nVersion 3, 29 June 2007\\n\\n\",\"osiApproval\":true,\"nicknames\":[\"GNU General Public License (GPL) version 3.0\",\"GPLv3\"],\"restrictions\":[],\"trafficLight\":\"🔴\",\"note\":null}";
            LPVSLicense expectedLicense = new LPVSLicense();
            expectedLicense.setLicenseName("GNU General Public License v3.0 only");
            expectedLicense.setSpdxId("GPL-3.0-only");
            expectedLicense.setAccess("PROHIBITED");
            expectedLicense.setAlternativeNames("GNU General Public License (GPL) version 3.0,GPLv3");

            LPVSLicense actualLicense = LPVSPayloadUtil.convertOsoriDbResponseToLicenseAlternative(payload);

            assertNotNull(actualLicense);
            assertEquals(expectedLicense.getLicenseName(), actualLicense.getLicenseName());
            assertEquals(expectedLicense.getSpdxId(), actualLicense.getSpdxId());
            assertEquals(expectedLicense.getAccess(), actualLicense.getAccess());
            assertEquals(
                    expectedLicense.getAlternativeNames(), actualLicense.getAlternativeNames());
        }

        @Test
        public void testConvertOsoriDbResponseToLicenseAlternative_Yellow() {
            String payload =
                    "{\"name\":\"Mozilla Public License 2.0\",\"obligationDisclosingSrc\":\"SOURCE\",\"obligationNotification\":true,\"spdxIdentifier\":\"MPL-2.0\",\"webpage\":\"https://spdx.org/licenses/MPL-2.0.html\",\"description\":\"Distribute the software and its source code.\\n\\n\",\"descriptionKo\":null,\"licenseText\":\"Mozilla Public License Version 2.0\\n\\n\",\"osiApproval\":true,\"nicknames\":[\"Mozilla Public License (MPL) version 2.0\",\"MPLv2\"],\"restrictions\":[],\"trafficLight\":\"🟡\",\"note\":null}";
            LPVSLicense expectedLicense = new LPVSLicense();
            expectedLicense.setLicenseName("Mozilla Public License 2.0");
            expectedLicense.setSpdxId("MPL-2.0");
            expectedLicense.setAccess("RESTRICTED");
            expectedLicense.setAlternativeNames("Mozilla Public License (MPL) version 2.0,MPLv2");

            LPVSLicense actualLicense = LPVSPayloadUtil.convertOsoriDbResponseToLicenseAlternative(payload);

            assertNotNull(actualLicense);
            assertEquals(expectedLicense.getLicenseName(), actualLicense.getLicenseName());
            assertEquals(expectedLicense.getSpdxId(), actualLicense.getSpdxId());
            assertEquals(expectedLicense.getAccess(), actualLicense.getAccess());
            assertEquals(
                    expectedLicense.getAlternativeNames(), actualLicense.getAlternativeNames());
        }

        @Test
        public void testConvertOsoriDbResponseToLicenseAlternative_NoTrafficLight() {
            String payload =
                    "{\"name\":\"MIT License\",\"obligationDisclosingSrc\":\"NONE\",\"obligationNotification\":true,\"spdxIdentifier\":\"MIT\",\"webpage\":\"https://spdx.org/licenses/MIT.html\",\"description\":\"To distribute the software, copyright and license notice is required.\\n\\n\",\"descriptionKo\":null,\"licenseText\":\"MIT License\\n\\n\",\"osiApproval\":true,\"nicknames\":[\"The MIT License (MIT)\"],\"restrictions\":[],\"note\":null}";
            LPVSLicense expectedLicense = new LPVSLicense();
            expectedLicense.setLicenseName("MIT License");
            expectedLicense.setSpdxId("MIT");
            expectedLicense.setAccess("UNREVIEWED");
            expectedLicense.setAlternativeNames("The MIT License (MIT)");

            LPVSLicense actualLicense = LPVSPayloadUtil.convertOsoriDbResponseToLicenseAlternative(payload);

            assertNotNull(actualLicense);
            assertEquals(expectedLicense.getLicenseName(), actualLicense.getLicenseName());
            assertEquals(expectedLicense.getSpdxId(), actualLicense.getSpdxId());
            assertEquals(expectedLicense.getAccess(), actualLicense.getAccess());
            assertEquals(
                    expectedLicense.getAlternativeNames(), actualLicense.getAlternativeNames());
        }

        @Test
        public void testConvertOsoriDbResponseToLicenseAlternative_nullNickname() {
            String payload =
                    "{\"name\":\"Apache License 2.0\",\"obligationDisclosingSrc\":\"NONE\",\"obligationNotification\":true,\"spdxIdentifier\":\"Apache-2.0\",\"webpage\":\"https://spdx.org/licenses/Apache-2.0.html\",\"description\":\"To distribute the software, copyright and license notice is required.\\n\\n\",\"descriptionKo\":null,\"licenseText\":\"Apache License\\nVersion 2.0, ....\",\"osiApproval\":true,\"nicknames\":null,\"restrictions\":[],\"trafficLight\":\"🟢\",\"note\":null}";
            LPVSLicense expectedLicense = new LPVSLicense();
            expectedLicense.setLicenseName("Apache License 2.0");
            expectedLicense.setSpdxId("Apache-2.0");
            expectedLicense.setAccess("PERMITTED");
            expectedLicense.setAlternativeNames("");

            LPVSLicense actualLicense = LPVSPayloadUtil.convertOsoriDbResponseToLicenseAlternative(payload);

            assertNotNull(actualLicense);
            assertEquals(expectedLicense.getLicenseName(), actualLicense.getLicenseName());
            assertEquals(expectedLicense.getSpdxId(), actualLicense.getSpdxId());
            assertEquals(expectedLicense.getAccess(), actualLicense.getAccess());
            assertEquals(
                    expectedLicense.getAlternativeNames(), actualLicense.getAlternativeNames());
        }

        @Test
        public void testConvertOsoriDbResponseToLicenseAlternative_withInvalidPayload_N() {
            String payload =
                    "{\"obligationDisclosingSrc\":\"NONE\",\"obligationNotification\":true,\"webpage\":\"https://spdx.org/licenses/Apache-2.0.html\",\"description\":\"To distribute the software, copyright and license notice is required.\\n\\n\",\"descriptionKo\":null,\"licenseText\":\"Apache License\\nVersion 2.0, ....\",\"osiApproval\":true,\"nicknames\":[\"Apache Public License 2.0\",\"http://www.apache.org/licenses/LICENSE-2.0\",\"Apache License Version 2.0\"],\"restrictions\":[],\"trafficLight\":\"🟢\",\"note\":null}";
            LPVSLicense actualLicense = LPVSPayloadUtil.convertOsoriDbResponseToLicenseAlternative(payload);
            assertNull(actualLicense);
        }
    }

    @Nested
    class TestConvertOsoriDbResponseToLicense {
        @Test
        public void testConvertOsoriDbResponseToLicense() {
            String payload =
                    "{\"code\":\"200\",\"messageList\":{\"detailInfo\":[{\"name\":\"Apache License 2.0\",\"spdx_identifier\":\"Apache-2.0\",\"webpage\":\"https://spdx.org/licenses/Apache-2.0.html\",\"nicknameList\":[\"Android-Apache-2.0\",\"Apache 2\"],\"webpageList\":null,\"restrictionList\":null}]},\"success\":true}";
            LPVSLicense expectedLicense = new LPVSLicense();
            expectedLicense.setLicenseName("Apache License 2.0");
            expectedLicense.setSpdxId("Apache-2.0");
            expectedLicense.setAccess("UNREVIEWED");
            expectedLicense.setAlternativeNames("Android-Apache-2.0,Apache 2");

            LPVSLicense actualLicense = LPVSPayloadUtil.convertOsoriDbResponseToLicense(payload);

            assertNotNull(actualLicense);
            assertEquals(expectedLicense.getLicenseName(), actualLicense.getLicenseName());
            assertEquals(expectedLicense.getSpdxId(), actualLicense.getSpdxId());
            assertEquals(expectedLicense.getAccess(), actualLicense.getAccess());
            assertEquals(
                    expectedLicense.getAlternativeNames(), actualLicense.getAlternativeNames());
        }

        @Test
        public void testConvertOsoriDbResponseToLicense_nullNickname() {
            String payload =
                    "{\"code\":\"200\",\"messageList\":{\"detailInfo\":[{\"name\":\"Apache License 2.0\",\"spdx_identifier\":\"Apache-2.0\",\"webpage\":\"https://spdx.org/licenses/Apache-2.0.html\",\"nicknameList\":null,\"webpageList\":null,\"restrictionList\":null}]},\"success\":true}";
            LPVSLicense expectedLicense = new LPVSLicense();
            expectedLicense.setLicenseName("Apache License 2.0");
            expectedLicense.setSpdxId("Apache-2.0");
            expectedLicense.setAccess("UNREVIEWED");
            expectedLicense.setAlternativeNames("");

            LPVSLicense actualLicense = LPVSPayloadUtil.convertOsoriDbResponseToLicense(payload);

            assertNotNull(actualLicense);
            assertEquals(expectedLicense.getLicenseName(), actualLicense.getLicenseName());
            assertEquals(expectedLicense.getSpdxId(), actualLicense.getSpdxId());
            assertEquals(expectedLicense.getAccess(), actualLicense.getAccess());
            assertEquals(
                    expectedLicense.getAlternativeNames(), actualLicense.getAlternativeNames());
        }

        @Test
        public void testConvertOsoriDbResponseToLicense_withInvalidPayload_N() {
            String payload =
                    "{\"code\":\"200\",\"messageList\":{\"detailInfo\":[{\"name\":\"Apache License 2.0\",\"webpage\":\"https://spdx.org/licenses/Apache-2.0.html\",\"webpageList\":null,\"restrictionList\":null}]},\"success\":true}";
            LPVSLicense actualLicense = LPVSPayloadUtil.convertOsoriDbResponseToLicense(payload);
            assertNull(actualLicense);
        }

        @Test
        public void testConvertOsoriDbResponseToLicense_emptyPayload_N() {
            String payload =
                    "{\"code\":\"200\",\"messageList\":{\"detailInfo\":[]},\"success\":true}";
            LPVSLicense actualLicense = LPVSPayloadUtil.convertOsoriDbResponseToLicense(payload);
            assertNull(actualLicense);
        }
    }

    @Nested
    class TestGetGitHubWebhookConfig {
        GHRepository repo;
        GHPullRequest pR;

        @BeforeEach
        void setUp() {
            repo = mock(GHRepository.class);
            pR = mock(GHPullRequest.class);
        }

        @Test
        void testGetGitHubWebhookConfig_WithValidPRAndRepo() throws MalformedURLException {
            when(repo.getHtmlUrl()).thenReturn(new URL("https://github.com/repo"));
            when(pR.getHtmlUrl()).thenReturn(new URL("https://github.com/repo/pull/1"));
            when(pR.getHead()).thenReturn(mock(GHCommitPointer.class));
            when(pR.getHead().getRepository()).thenReturn(repo);
            when(pR.getHead().getRepository().getHtmlUrl())
                    .thenReturn(new URL("https://github.com/repo"));
            when(pR.getHead().getSha()).thenReturn("1234567890");

            LPVSQueue webhookConfig = LPVSPayloadUtil.getGitHubWebhookConfig(repo, pR);

            assertEquals("https://github.com/repo/pull/1", webhookConfig.getPullRequestUrl());
            assertEquals("https://github.com/repo", webhookConfig.getPullRequestFilesUrl());
            assertNull(webhookConfig.getPullRequestAPIUrl());
            assertEquals("https://github.com/repo", webhookConfig.getRepositoryUrl());
            assertEquals("Single scan of pull request run", webhookConfig.getUserId());
            assertEquals("1234567890", webhookConfig.getHeadCommitSHA());
        }

        @Test
        void testGetGitHubWebhookConfig_WithNullPRHtmlUrl() {
            when(pR.getHtmlUrl()).thenReturn(null);
            when(pR.getHead()).thenReturn(mock(GHCommitPointer.class));
            when(pR.getHead().getRepository()).thenReturn(repo);
            when(pR.getHead().getRepository().getHtmlUrl()).thenReturn(null);
            when(pR.getHead().getSha()).thenReturn("1234567890");

            LPVSQueue webhookConfig = LPVSPayloadUtil.getGitHubWebhookConfig(repo, pR);

            assertNull(webhookConfig.getPullRequestUrl());
            assertNull(webhookConfig.getPullRequestFilesUrl());
            assertNull(webhookConfig.getPullRequestAPIUrl());
            assertNull(webhookConfig.getRepositoryUrl());
            assertEquals("Single scan of pull request run", webhookConfig.getUserId());
            assertEquals("1234567890", webhookConfig.getHeadCommitSHA());
        }

        @Test
        void testGetGitHubWebhookConfig_WithNullPRHead() {
            when(pR.getHtmlUrl()).thenReturn(null);
            when(pR.getHead()).thenReturn(mock(GHCommitPointer.class));
            when(pR.getHead().getRepository()).thenReturn(null);
            when(pR.getHead().getSha()).thenReturn("1234567890");

            LPVSQueue webhookConfig = LPVSPayloadUtil.getGitHubWebhookConfig(repo, pR);

            assertNull(webhookConfig.getPullRequestUrl());
            assertNull(webhookConfig.getPullRequestFilesUrl());
            assertNull(webhookConfig.getPullRequestAPIUrl());
            assertNull(webhookConfig.getRepositoryUrl());
            assertEquals("Single scan of pull request run", webhookConfig.getUserId());
            assertEquals("1234567890", webhookConfig.getHeadCommitSHA());
        }
    }

    @Nested
    class TestGetGitHubWebhookConfig__ForkTrue {
        String json_to_test;
        LPVSQueue expected;

        @BeforeEach
        void setUp() {
            json_to_test =
                    "{"
                            + "\"action\": \"opened\", "
                            + "\"repository\": {"
                            + "\"name\": \"LPVS\", "
                            + "\"full_name\": \"Samsung/LPVS\", "
                            + "\"html_url\": \"https://github.com/Samsung/LPVS\""
                            + "}, "
                            + "\"pull_request\": {"
                            + "\"html_url\": \"https://github.com/Samsung/LPVS/pull/18\", "
                            + "\"base\": {"
                            + "\"repo\": {"
                            + "\"owner\": {"
                            + "\"login\": \"Samsung\""
                            + "}"
                            + "}"
                            + "}, "
                            + "\"head\": {"
                            + "\"repo\": {"
                            + "\"owner\": {"
                            + "\"login\": \"o-kopysov\""
                            + "},"
                            + "\"fork\": true, "
                            + "\"html_url\": \"https://github.com/o-kopysov/LPVS/tree/utests\""
                            + "}, "
                            + "\"sha\": \"edde69ecb8e8a88dde09fa9789e2c9cab7cf7cf9\", "
                            + "\"ref\": \"o-kopysov:utests\""
                            + "}, "
                            + "\"url\": \"https://api.github.com/repos/Samsung/LPVS/pulls/18\""
                            + "},"
                            + "\"sender\": {"
                            + "\"login\": \"o-kopysov\""
                            + "}"
                            + "}";

            expected = new LPVSQueue();
            expected.setAction(LPVSPullRequestAction.OPEN);
            expected.setPullRequestUrl("https://github.com/Samsung/LPVS/pull/18");
            expected.setPullRequestFilesUrl(
                    "https://github.com/o-kopysov/LPVS/tree/utests"); // fork == True
            expected.setPullRequestAPIUrl("https://api.github.com/repos/Samsung/LPVS/pulls/18");
            expected.setRepositoryUrl("https://github.com/Samsung/LPVS");
            expected.setUserId("GitHub hook");
            expected.setPullRequestBase("Samsung");
            expected.setPullRequestHead("o-kopysov");
            expected.setSender("o-kopysov");
            expected.setHeadCommitSHA("edde69ecb8e8a88dde09fa9789e2c9cab7cf7cf9");
            expected.setAttempts(0);
        }

        @Test
        public void testGetGitHubWebhookConfig__ForkTrue() {
            // main test
            assertEquals(expected, LPVSPayloadUtil.getGitHubWebhookConfig(json_to_test));
        }
    }

    @Nested
    class TestGetGitHubWebhookConfig__ForkFalse {
        String json_to_test;
        LPVSQueue expected;

        @BeforeEach
        void setUp() {
            json_to_test =
                    "{"
                            + "\"action\": \"opened\", "
                            + "\"repository\": {"
                            + "\"name\": \"LPVS\", "
                            + "\"full_name\": \"Samsung/LPVS\", "
                            + "\"html_url\": \"https://github.com/Samsung/LPVS\""
                            + "}, "
                            + "\"pull_request\": {"
                            + "\"html_url\": \"https://github.com/Samsung/LPVS/pull/18\", "
                            + "\"base\": {"
                            + "\"repo\": {"
                            + "\"owner\": {"
                            + "\"login\": \"Samsung\""
                            + "}"
                            + "}"
                            + "}, "
                            + "\"head\": {"
                            + "\"repo\": {"
                            + "\"owner\": {"
                            + "\"login\": \"o-kopysov\""
                            + "},"
                            + "\"fork\": false, "
                            + "\"html_url\": \"https://github.com/o-kopysov/LPVS/tree/utests\""
                            + "}, "
                            + "\"sha\": \"edde69ecb8e8a88dde09fa9789e2c9cab7cf7cf9\", "
                            + "\"ref\": \"o-kopysov:utests\""
                            + "}, "
                            + "\"url\": \"https://api.github.com/repos/Samsung/LPVS/pulls/18\""
                            + "},"
                            + "\"sender\": {"
                            + "\"login\": \"o-kopysov\""
                            + "}"
                            + "}";

            expected = new LPVSQueue();
            expected.setAction(LPVSPullRequestAction.OPEN);
            expected.setPullRequestUrl("https://github.com/Samsung/LPVS/pull/18");
            expected.setPullRequestFilesUrl(
                    "https://github.com/Samsung/LPVS/pull/18"); // fork == False
            expected.setPullRequestAPIUrl("https://api.github.com/repos/Samsung/LPVS/pulls/18");
            expected.setRepositoryUrl("https://github.com/Samsung/LPVS");
            expected.setUserId("GitHub hook");
            expected.setPullRequestBase("Samsung");
            expected.setPullRequestHead("o-kopysov");
            expected.setSender("o-kopysov");
            expected.setHeadCommitSHA("edde69ecb8e8a88dde09fa9789e2c9cab7cf7cf9");
            expected.setAttempts(0);
        }

        @Test
        public void testGetGitHubWebhookConfig__ForkFalse() {
            // main test
            assertEquals(expected, LPVSPayloadUtil.getGitHubWebhookConfig(json_to_test));
        }
    }

    @Nested
    class TestCheckPayload {
        String json_to_test;

        @Test
        public void testCheckPayload() {
            // test initial `if`
            json_to_test = "{" + "\"action\": \"opened\", " + "\"zen\": \"test\"" + "}";
            assertFalse(LPVSPayloadUtil.checkPayload(json_to_test));

            // test the rest 6 cases of `LPVSPullRequestAction`
            json_to_test = "{\"action\": \"opened\"}";
            assertTrue(LPVSPayloadUtil.checkPayload(json_to_test));

            json_to_test = "{\"action\": \"reopened\"}";
            assertTrue(LPVSPayloadUtil.checkPayload(json_to_test));

            json_to_test = "{\"action\": \"synchronize\"}";
            assertTrue(LPVSPayloadUtil.checkPayload(json_to_test));

            json_to_test = "{\"action\": \"closed\"}";
            assertFalse(LPVSPayloadUtil.checkPayload(json_to_test));

            json_to_test = "{\"action\": \"rescan\"}";
            assertFalse(LPVSPayloadUtil.checkPayload(json_to_test));

            json_to_test = "{\"action\": \"any_of_above\"}";
            assertFalse(LPVSPayloadUtil.checkPayload(json_to_test));
        }
    }

    @Nested
    public class TestGetPullRequestId {

        @Mock private LPVSQueue mockWebhookConfig;

        @Test
        public void testGetPullRequestIdWithValidConfig() {
            mockWebhookConfig = new LPVSQueue();
            mockWebhookConfig.setRepositoryUrl("https://github.com/repo");
            mockWebhookConfig.setPullRequestUrl("https://github.com/repo/pull/123");
            String result = LPVSPayloadUtil.getPullRequestId(mockWebhookConfig);
            assertEquals("123", result);
        }
    }

    @Nested
    public class TestWebhookNull {

        @Test
        public void checkNull() {
            LPVSQueue mockWebhookConfig = new LPVSQueue();

            IllegalArgumentException exception =
                    assertThrows(
                            IllegalArgumentException.class,
                            () -> {
                                LPVSPayloadUtil.getRepositoryOrganization(null);
                            });
            assertEquals("Webhook Config is absent", exception.getMessage());

            exception =
                    assertThrows(
                            IllegalArgumentException.class,
                            () -> {
                                LPVSPayloadUtil.getRepositoryOrganization(mockWebhookConfig);
                            });
            assertEquals("No repository URL info in webhook config", exception.getMessage());

            exception =
                    assertThrows(
                            IllegalArgumentException.class,
                            () -> {
                                LPVSPayloadUtil.getRepositoryName(null);
                            });
            assertEquals("Webhook Config is absent", exception.getMessage());

            exception =
                    assertThrows(
                            IllegalArgumentException.class,
                            () -> {
                                LPVSPayloadUtil.getRepositoryName(mockWebhookConfig);
                            });
            assertEquals("No repository URL info in webhook config", exception.getMessage());

            exception =
                    assertThrows(
                            IllegalArgumentException.class,
                            () -> {
                                LPVSPayloadUtil.getRepositoryUrl(null);
                            });
            assertEquals("Webhook Config is absent", exception.getMessage());

            exception =
                    assertThrows(
                            IllegalArgumentException.class,
                            () -> {
                                LPVSPayloadUtil.getPullRequestId(null);
                            });
            assertEquals("Webhook Config is absent", exception.getMessage());

            exception =
                    assertThrows(
                            IllegalArgumentException.class,
                            () -> {
                                LPVSPayloadUtil.getPullRequestId(mockWebhookConfig);
                            });
            assertEquals("No repository URL info in webhook config", exception.getMessage());

            mockWebhookConfig.setRepositoryUrl("https://github.com/Samsung/LPVS");
            exception =
                    assertThrows(
                            IllegalArgumentException.class,
                            () -> {
                                LPVSPayloadUtil.getPullRequestId(mockWebhookConfig);
                            });
            assertEquals("Pull Request URL is absent in webhook config", exception.getMessage());
        }
    }

    @Nested
    public class TestHttpHeaders {

        @Test
        void generateSecurityHeadersTest() {
            HttpHeaders headers = LPVSPayloadUtil.generateSecurityHeaders();

            // Assert the presence of each expected header
            assertEquals(
                    "max-age=31536000; includeSubDomains",
                    headers.getFirst("Strict-Transport-Security"));
            assertEquals("default-src 'self'", headers.getFirst("Content-Security-Policy"));
            assertEquals("nosniff", headers.getFirst("X-Content-Type-Options"));
            assertEquals("DENY", headers.getFirst("X-Frame-Options"));
            assertEquals("1; mode=block", headers.getFirst("X-XSS-Protection"));
            assertEquals("same-origin", headers.getFirst("Referrer-Policy"));
            assertEquals("none", headers.getFirst("Feature-Policy"));
            assertEquals("same-origin", headers.getFirst("Access-Control-Allow-Origin"));
            assertEquals(
                    "GET, POST, PUT, DELETE", headers.getFirst("Access-Control-Allow-Methods"));
            assertEquals("Content-Type", headers.getFirst("Access-Control-Allow-Headers"));
        }
    }

    @Nested
    public class TestCreateInputStreamReader {

        @Test
        public void testCreateInputStreamReader() throws IOException, URISyntaxException {
            Path path =
                    Paths.get(
                            Objects.requireNonNull(
                                            getClass().getClassLoader().getResource("1-A_B.json"))
                                    .toURI());
            InputStream inputStream = Files.newInputStream(path);
            InputStreamReader inputStreamReader =
                    LPVSPayloadUtil.createInputStreamReader(inputStream);
            assertEquals("UTF8", inputStreamReader.getEncoding());
            inputStreamReader.close();
            inputStream.close();
        }

        @Test
        public void testCreateInputStreamReader_ThrowsException_N() {
            InputStream inputStream = null;
            assertThrows(
                    NullPointerException.class,
                    () -> LPVSPayloadUtil.createInputStreamReader(inputStream));
        }
    }

    @Nested
    public class TestCreateBufferReader {

        @Test
        public void testCreateBufferReader() throws URISyntaxException, IOException {
            Path path =
                    Paths.get(
                            Objects.requireNonNull(
                                            getClass().getClassLoader().getResource("1-A_B.json"))
                                    .toURI());
            InputStream inputStream = Files.newInputStream(path);
            InputStreamReader inputStreamReader =
                    LPVSPayloadUtil.createInputStreamReader(inputStream);
            BufferedReader bufferedReader = LPVSPayloadUtil.createBufferReader(inputStreamReader);
            assertNotNull(bufferedReader.readLine());
            bufferedReader.close();
            inputStreamReader.close();
            inputStream.close();
        }

        @Test
        public void testCreateBufferedReader_ThrowsException_N() {
            InputStreamReader inputStreamReader = null;
            assertThrows(
                    NullPointerException.class,
                    () -> LPVSPayloadUtil.createBufferReader(inputStreamReader));
        }
    }

    @Nested
    public class TestConvertInputStreamToString {

        @Test
        public void testConvertInputStreamToString() throws IOException, URISyntaxException {
            Path path =
                    Paths.get(
                            Objects.requireNonNull(
                                            getClass().getClassLoader().getResource("1-A_B.json"))
                                    .toURI());
            InputStream inputStream = Files.newInputStream(path);
            assertNotNull(LPVSPayloadUtil.convertInputStreamToString(inputStream));
            inputStream.close();
        }

        @Test
        public void testConvertInputStreamToString_ThrowsException_N() {
            InputStream inputStream = null;
            assertThrows(
                    NullPointerException.class,
                    () -> LPVSPayloadUtil.convertInputStreamToString(inputStream));
        }
    }

    @Test
    void testConstructorThrowsException_N() {
        try {
            Constructor<LPVSPayloadUtil> constructor =
                    LPVSPayloadUtil.class.getDeclaredConstructor();
            constructor.setAccessible(true);
            constructor.newInstance();
            fail("Expected UnsupportedOperationException to be thrown");
        } catch (InvocationTargetException e) {
            assertInstanceOf(
                    UnsupportedOperationException.class,
                    e.getCause(),
                    "UnsupportedOperationException expected");
        } catch (Exception e) {
            fail("Unexpected exception type thrown: " + e.getCause());
        }
    }
}
