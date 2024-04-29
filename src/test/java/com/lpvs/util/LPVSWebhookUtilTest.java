/**
 * Copyright (c) 2023, Samsung Electronics Co., Ltd. All rights reserved.
 *
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */
package com.lpvs.util;

import com.lpvs.entity.LPVSQueue;
import com.lpvs.entity.enums.LPVSPullRequestAction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.http.HttpHeaders;

import static org.junit.jupiter.api.Assertions.*;

public class LPVSWebhookUtilTest {

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
            assertEquals(expected, LPVSWebhookUtil.getGitHubWebhookConfig(json_to_test));
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
            assertEquals(expected, LPVSWebhookUtil.getGitHubWebhookConfig(json_to_test));
        }
    }

    @Nested
    class TestCheckPayload {
        String json_to_test;

        @Test
        public void testCheckPayload() {
            // test initial `if`
            json_to_test = "{" + "\"action\": \"opened\", " + "\"zen\": \"test\"" + "}";
            assertFalse(LPVSWebhookUtil.checkPayload(json_to_test));

            // test the rest 6 cases of `LPVSPullRequestAction`
            json_to_test = "{\"action\": \"opened\"}";
            assertTrue(LPVSWebhookUtil.checkPayload(json_to_test));

            json_to_test = "{\"action\": \"reopened\"}";
            assertTrue(LPVSWebhookUtil.checkPayload(json_to_test));

            json_to_test = "{\"action\": \"synchronize\"}";
            assertTrue(LPVSWebhookUtil.checkPayload(json_to_test));

            json_to_test = "{\"action\": \"closed\"}";
            assertFalse(LPVSWebhookUtil.checkPayload(json_to_test));

            json_to_test = "{\"action\": \"rescan\"}";
            assertFalse(LPVSWebhookUtil.checkPayload(json_to_test));

            json_to_test = "{\"action\": \"any_of_above\"}";
            assertFalse(LPVSWebhookUtil.checkPayload(json_to_test));
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
            String result = LPVSWebhookUtil.getPullRequestId(mockWebhookConfig);
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
                                LPVSWebhookUtil.getRepositoryOrganization(null);
                            });
            assertEquals("Webhook Config is absent", exception.getMessage());

            exception =
                    assertThrows(
                            IllegalArgumentException.class,
                            () -> {
                                LPVSWebhookUtil.getRepositoryOrganization(mockWebhookConfig);
                            });
            assertEquals("No repository URL info in webhook config", exception.getMessage());

            exception =
                    assertThrows(
                            IllegalArgumentException.class,
                            () -> {
                                LPVSWebhookUtil.getRepositoryName(null);
                            });
            assertEquals("Webhook Config is absent", exception.getMessage());

            exception =
                    assertThrows(
                            IllegalArgumentException.class,
                            () -> {
                                LPVSWebhookUtil.getRepositoryName(mockWebhookConfig);
                            });
            assertEquals("No repository URL info in webhook config", exception.getMessage());

            exception =
                    assertThrows(
                            IllegalArgumentException.class,
                            () -> {
                                LPVSWebhookUtil.getRepositoryUrl(null);
                            });
            assertEquals("Webhook Config is absent", exception.getMessage());

            exception =
                    assertThrows(
                            IllegalArgumentException.class,
                            () -> {
                                LPVSWebhookUtil.getPullRequestId(null);
                            });
            assertEquals("Webhook Config is absent", exception.getMessage());

            exception =
                    assertThrows(
                            IllegalArgumentException.class,
                            () -> {
                                LPVSWebhookUtil.getPullRequestId(mockWebhookConfig);
                            });
            assertEquals("No repository URL info in webhook config", exception.getMessage());

            mockWebhookConfig.setRepositoryUrl("https://github.com/Samsung/LPVS");
            exception =
                    assertThrows(
                            IllegalArgumentException.class,
                            () -> {
                                LPVSWebhookUtil.getPullRequestId(mockWebhookConfig);
                            });
            assertEquals("Pull Request URL is absent in webhook config", exception.getMessage());
        }
    }

    @Nested
    public class TestHttpHeaders {

        @Test
        void generateSecurityHeadersTest() {
            HttpHeaders headers = LPVSWebhookUtil.generateSecurityHeaders();

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
}
