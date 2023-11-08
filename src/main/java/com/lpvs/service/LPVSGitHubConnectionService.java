package com.lpvs.service;

import com.lpvs.util.LPVSExitHandler;
import lombok.extern.slf4j.Slf4j;
import org.kohsuke.github.GitHub;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.Optional;

@Service
@Slf4j
public class LPVSGitHubConnectionService {

    private String GITHUB_LOGIN;
    private String GITHUB_AUTH_TOKEN;
    private String GITHUB_API_URL;

    private static final String GITHUB_LOGIN_PROP_NAME = "github.login";
    private static final String GITHUB_AUTH_TOKEN_PROP_NAME = "github.token";
    private static final String GITHUB_API_URL_PROP_NAME = "github.api.url";

    private static final String GITHUB_LOGIN_ENV_VAR_NAME = "LPVS_GITHUB_LOGIN";
    private static final String GITHUB_AUTH_TOKEN_ENV_VAR_NAME = "LPVS_GITHUB_TOKEN";
    private static final String GITHUB_API_URL_ENV_VAR_NAME = "LPVS_GITHUB_API_URL";

    private LPVSExitHandler exitHandler;

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

    public void setGithubTokenFromEnv() {
        if (System.getenv("LPVS_GITHUB_TOKEN") != null)
            GITHUB_AUTH_TOKEN = System.getenv("LPVS_GITHUB_TOKEN");
    }
}
