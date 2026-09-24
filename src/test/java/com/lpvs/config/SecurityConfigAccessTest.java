/**
 * Copyright (c) 2026, Samsung Electronics Co., Ltd. All rights reserved.
 *
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */
package com.lpvs.config;

import com.lpvs.controller.GitHubController;
import com.lpvs.controller.HealthController;
import com.lpvs.repository.LPVSQueueRepository;
import com.lpvs.service.LPVSGitHubConnectionService;
import com.lpvs.service.LPVSGitHubService;
import com.lpvs.service.LPVSQueueService;
import com.lpvs.util.LPVSExitHandler;
import org.apache.commons.codec.binary.Hex;
import org.junit.jupiter.api.Test;
import org.kohsuke.github.GHPullRequest;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.forwardedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest
@ContextConfiguration(
        classes = {SecurityConfig.class, GitHubController.class, HealthController.class})
@TestPropertySource(
        properties = {"github.secret=" + SecurityConfigAccessTest.SECRET, "lpvs.api.key=test-key"})
class SecurityConfigAccessTest {

    static final String SECRET = "test-webhook-secret";

    @Autowired private MockMvc mockMvc;

    @MockitoBean private LPVSQueueService queueService;
    @MockitoBean private LPVSGitHubService gitHubService;
    @MockitoBean private LPVSGitHubConnectionService gitHubConnectionService;
    @MockitoBean private LPVSQueueRepository queueRepository;
    @MockitoBean private LPVSExitHandler exitHandler;

    private static String sign(String payload) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(SECRET.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        return "sha256="
                + Hex.encodeHexString(mac.doFinal(payload.getBytes(StandardCharsets.UTF_8)));
    }

    @Test
    void healthIsPublic() throws Exception {
        mockMvc.perform(get("/health")).andExpect(status().isOk());
    }

    @Test
    void webhookIsPublic() throws Exception {
        String payload = "{\"zen\": \"ping\"}";
        mockMvc.perform(
                        post("/webhooks")
                                .contentType(MediaType.APPLICATION_JSON)
                                .header("X-Hub-Signature-256", sign(payload))
                                .content(payload))
                .andExpect(status().isOk());
    }

    @Test
    void rootPostIsForwardedToWebhook() throws Exception {
        mockMvc.perform(post("/")).andExpect(forwardedUrl("/webhooks"));
    }

    @Test
    void singleScanWithoutApiKeyIsForbidden() throws Exception {
        mockMvc.perform(post("/scan/org/repo/1")).andExpect(status().isForbidden());
        verify(gitHubConnectionService, never()).connectToGitHubApi();
    }

    @Test
    void singleScanWithApiKeyIsAllowed() throws Exception {
        GitHub gitHub = mock(GitHub.class);
        GHRepository repository = mock(GHRepository.class);
        when(gitHubConnectionService.connectToGitHubApi()).thenReturn(gitHub);
        when(gitHub.getRepository("org/repo")).thenReturn(repository);
        when(repository.getPullRequest(1)).thenReturn(mock(GHPullRequest.class));

        mockMvc.perform(post("/scan/org/repo/1").header("X-LPVS-Api-Key", "test-key"))
                .andExpect(status().isOk());
        verify(queueService).addFirst(any());
    }

    @Test
    void unknownEndpointIsDenied() throws Exception {
        mockMvc.perform(get("/unknown")).andExpect(status().isForbidden());
        mockMvc.perform(post("/unknown")).andExpect(status().isForbidden());
    }

    @Test
    void wrongMethodIsDenied() throws Exception {
        mockMvc.perform(post("/health")).andExpect(status().isForbidden());
        mockMvc.perform(get("/scan/org/repo/1").header("X-LPVS-Api-Key", "test-key"))
                .andExpect(status().isForbidden());
    }
}
