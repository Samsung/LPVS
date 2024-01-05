/**
 * Copyright (c) 2023, Samsung Electronics Co., Ltd. All rights reserved.
 *
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */
package com.lpvs.service;

import com.lpvs.util.LPVSExitHandler;
import lombok.extern.slf4j.Slf4j;
import org.kohsuke.github.GitHub;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.util.Optional;

/**
 * Service class for managing connections to the GitHub API.
 *
 * It provides methods to connect to the GitHub API with the specified login and authentication token.
 */
@Service
@Slf4j
public class LPVSGitHubConnectionService {

    /**
     * GitHub login username.
     */
    private String GITHUB_LOGIN;

    /**
     * GitHub authentication token.
     */
    private String GITHUB_AUTH_TOKEN;

    /**
     * GitHub API URL for connecting to a GitHub instance.
     */
    private String GITHUB_API_URL;

    /**
     * Name of the GitHub login property.
     */
    private static final String GITHUB_LOGIN_PROP_NAME = "github.login";

    /**
     * Name of the GitHub authentication token property.
     */
    private static final String GITHUB_AUTH_TOKEN_PROP_NAME = "github.token";

    /**
     * Name of the GitHub API URL property.
     */
    private static final String GITHUB_API_URL_PROP_NAME = "github.api.url";

    /**
     * Name of the GitHub login environment variable.
     */
    private static final String GITHUB_LOGIN_ENV_VAR_NAME = "LPVS_GITHUB_LOGIN";

    /**
     * Name of the GitHub authentication token environment variable.
     */
    private static final String GITHUB_AUTH_TOKEN_ENV_VAR_NAME = "LPVS_GITHUB_TOKEN";

    /**
     * Name of the GitHub API URL environment variable.
     */
    private static final String GITHUB_API_URL_ENV_VAR_NAME = "LPVS_GITHUB_API_URL";

    /**
     * Handler for exiting the application.
     */
    private LPVSExitHandler exitHandler;

    /**
     * Constructs an instance of LPVSGitHubConnectionService with the specified properties and exit handler.
     *
     * @param GITHUB_LOGIN       GitHub login username.
     * @param GITHUB_AUTH_TOKEN  GitHub authentication token.
     * @param GITHUB_API_URL     GitHub API URL for connecting to a GitHub Enterprise instance.
     * @param exitHandler        Handler for exiting the application.
     */
    @Autowired
    public LPVSGitHubConnectionService(
            @Value("${" + GITHUB_LOGIN_PROP_NAME + "}") String GITHUB_LOGIN,
            @Value("${" + GITHUB_AUTH_TOKEN_PROP_NAME + "}") String GITHUB_AUTH_TOKEN,
            @Value("${" + GITHUB_API_URL_PROP_NAME + "}") String GITHUB_API_URL,
            LPVSExitHandler exitHandler) {
        this.GITHUB_LOGIN =
                Optional.ofNullable(GITHUB_LOGIN)
                        .filter(s -> !s.isEmpty())
                        .orElse(
                                Optional.ofNullable(System.getenv(GITHUB_LOGIN_ENV_VAR_NAME))
                                        .orElse(""));
        this.GITHUB_AUTH_TOKEN =
                Optional.ofNullable(GITHUB_AUTH_TOKEN)
                        .filter(s -> !s.isEmpty())
                        .orElse(
                                Optional.ofNullable(System.getenv(GITHUB_AUTH_TOKEN_ENV_VAR_NAME))
                                        .orElse(""));
        this.GITHUB_API_URL =
                Optional.ofNullable(GITHUB_API_URL)
                        .filter(s -> !s.isEmpty())
                        .orElse(
                                Optional.ofNullable(System.getenv(GITHUB_API_URL_ENV_VAR_NAME))
                                        .orElse(""));
        this.exitHandler = exitHandler;
    }

    /**
     * Checks if the GitHub authentication token is set and exits the application if not.
     */
    @PostConstruct
    private void checks() {
        if (this.GITHUB_AUTH_TOKEN.isEmpty()) {
            log.error(
                    GITHUB_AUTH_TOKEN_ENV_VAR_NAME
                            + "("
                            + GITHUB_AUTH_TOKEN_PROP_NAME
                            + ") is not set.");
            exitHandler.exit(-1);
        }
    }

    /**
     * Connects to the GitHub API based on the configured login, authentication token, and API URL.
     *
     * @return GitHub instance for interacting with the GitHub API.
     * @throws IOException if an error occurs during the GitHub connection.
     */
    public GitHub connectToGitHubApi() throws IOException {
        GitHub gH;
        if (GITHUB_AUTH_TOKEN.isEmpty()) setGithubTokenFromEnv();
        if (GITHUB_API_URL.isEmpty()) gH = GitHub.connect(GITHUB_LOGIN, GITHUB_AUTH_TOKEN);
        else
            gH =
                    GitHub.connectToEnterpriseWithOAuth(
                            GITHUB_API_URL, GITHUB_LOGIN, GITHUB_AUTH_TOKEN);
        return gH;
    }

    /**
     * Sets the GitHub authentication token from the environment variable if available.
     */
    public void setGithubTokenFromEnv() {
        if (System.getenv("LPVS_GITHUB_TOKEN") != null)
            GITHUB_AUTH_TOKEN = System.getenv("LPVS_GITHUB_TOKEN");
    }
}
