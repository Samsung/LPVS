/**
 * Copyright (c) 2022-2024, Samsung Electronics Co., Ltd. All rights reserved.
 *
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */
package com.lpvs.util;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.lpvs.entity.LPVSLicense;
import com.lpvs.entity.LPVSQueue;
import com.lpvs.entity.enums.LPVSPullRequestAction;
import lombok.extern.slf4j.Slf4j;
import org.kohsuke.github.GHPullRequest;
import org.kohsuke.github.GHRepository;
import org.springframework.http.HttpHeaders;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Utility class for processing GitHub webhook payloads and extracting relevant information.
 * This class provides methods to parse GitHub webhook payloads, check their validity, and retrieve
 * specific details such as organization name, repository name, repository URL, and pull request ID.
 */
@Slf4j
public class LPVSPayloadUtil {

    /**
     * Creates an InputStreamReader object with the specified input stream and UTF-8 encoding.
     *
     * @param inputStream The input stream to read from.
     * @return An InputStreamReader object with the specified input stream.
     * @throws UnsupportedEncodingException If UTF-8 encoding is not supported.
     */
    public static InputStreamReader createInputStreamReader(InputStream inputStream)
            throws UnsupportedEncodingException {
        return new InputStreamReader(inputStream, "UTF-8");
    }

    /**
     * Creates a BufferedReader for the given InputStreamReader.
     *
     * @param inputStreamReader The InputStreamReader to create a BufferedReader from.
     * @return A BufferedReader for the given InputStreamReader.
     */
    public static BufferedReader createBufferReader(InputStreamReader inputStreamReader) {
        return new BufferedReader(inputStreamReader);
    }

    /**
     * Parses the given payload from the OSORI DB and converts it into a LPVSLicense object.
     *
     * @param payload the JSON payload from the OSORI DB
     * @return the LPVSLicense object containing the parsed information from the payload, or null if the payload is invalid
     */
    public static LPVSLicense convertOsoriDbResponseToLicense(String payload) {
        try {
            Gson gson = new Gson();
            JsonObject json = gson.fromJson(payload, JsonObject.class);
            JsonObject messageList = json.getAsJsonObject("messageList");
            JsonArray detailInfoArray = messageList.getAsJsonArray("detailInfo");

            if (!detailInfoArray.isEmpty()) {
                LPVSLicense lic = new LPVSLicense();
                lic.setLicenseName(
                        detailInfoArray.get(0).getAsJsonObject().get("name").getAsString());
                lic.setSpdxId(
                        detailInfoArray
                                .get(0)
                                .getAsJsonObject()
                                .get("spdx_identifier")
                                .getAsString());
                lic.setAccess("UNREVIEWED");

                List<String> nicknameList = new ArrayList<>();
                JsonElement nicknameListArray =
                        detailInfoArray.get(0).getAsJsonObject().get("nicknameList");
                if (nicknameListArray != null && nicknameListArray.isJsonArray()) {
                    nicknameListArray
                            .getAsJsonArray()
                            .forEach(element -> nicknameList.add(element.getAsString()));
                }
                lic.setAlternativeNames(String.join(",", nicknameList));

                return lic;
            }
        } catch (Exception e) {
            log.error("Error parsing OSORI DB payload: " + e.getMessage());
        }
        return null;
    }

    /**
     * Convert an InputStream into a String by reading the contents line by line.
     *
     * @param inputStream The InputStream to convert.
     * @return A String containing the contents of the InputStream.
     * @throws IOException If an error occurs while reading the InputStream.
     */
    public static String convertInputStreamToString(InputStream inputStream) throws IOException {
        BufferedReader in = createBufferReader(createInputStreamReader(inputStream));
        String inputLine;
        StringBuffer message = new StringBuffer();

        while ((inputLine = in.readLine()) != null) {
            message.append(inputLine);
        }
        in.close();
        return message.toString();
    }

    /**
     * Parses the GitHub webhook payload and extracts relevant information to create an LPVSQueue object.
     *
     * @param payload The GitHub webhook payload in JSON format.
     * @return LPVSQueue object containing information extracted from the webhook payload.
     */
    public static LPVSQueue getGitHubWebhookConfig(String payload) {
        Gson gson = new Gson();
        LPVSQueue webhookConfig = new LPVSQueue();

        JsonObject json = gson.fromJson(payload, JsonObject.class);
        webhookConfig.setAction(
                LPVSPullRequestAction.convertFrom(json.get("action").getAsString()));
        String url = json.getAsJsonObject("pull_request").get("html_url").getAsString();
        webhookConfig.setPullRequestUrl(url);
        if (json.getAsJsonObject("pull_request")
                .getAsJsonObject("head")
                .getAsJsonObject("repo")
                .get("fork")
                .getAsBoolean()) {
            webhookConfig.setPullRequestFilesUrl(
                    json.getAsJsonObject("pull_request")
                            .getAsJsonObject("head")
                            .getAsJsonObject("repo")
                            .get("html_url")
                            .getAsString());
        } else {
            webhookConfig.setPullRequestFilesUrl(webhookConfig.getPullRequestUrl());
        }
        webhookConfig.setPullRequestAPIUrl(
                json.getAsJsonObject("pull_request").get("url").getAsString());
        webhookConfig.setRepositoryUrl(
                json.getAsJsonObject("repository").get("html_url").getAsString());
        webhookConfig.setUserId("GitHub hook");
        webhookConfig.setHeadCommitSHA(
                json.getAsJsonObject("pull_request")
                        .getAsJsonObject("head")
                        .get("sha")
                        .getAsString());

        webhookConfig.setPullRequestBase(
                json.getAsJsonObject("pull_request")
                        .getAsJsonObject("base")
                        .getAsJsonObject("repo")
                        .getAsJsonObject("owner")
                        .get("login")
                        .getAsString());
        webhookConfig.setPullRequestHead(
                json.getAsJsonObject("pull_request")
                        .getAsJsonObject("head")
                        .getAsJsonObject("repo")
                        .getAsJsonObject("owner")
                        .get("login")
                        .getAsString());
        webhookConfig.setSender(json.getAsJsonObject("sender").get("login").getAsString());
        webhookConfig.setAttempts(0);
        return webhookConfig;
    }

    /**
     * Checks if the provided payload represents a valid GitHub webhook event that LPVS can handle.
     *
     * @param payload The GitHub webhook payload in JSON format.
     * @return true if the payload is valid and LPVS can handle the event, false otherwise.
     */
    public static boolean checkPayload(String payload) {
        if (payload.contains("\"zen\":")) {
            log.debug("Initial webhook received");
            return false;
        }

        Gson gson = new Gson();
        JsonObject json = gson.fromJson(payload, JsonObject.class);
        String actionString = json.get("action").getAsString();
        log.debug("Action " + actionString);
        LPVSPullRequestAction action = LPVSPullRequestAction.convertFrom(actionString);
        return (action != null)
                && (action.equals(LPVSPullRequestAction.UPDATE)
                        || action.equals(LPVSPullRequestAction.OPEN)
                        || action.equals(LPVSPullRequestAction.REOPEN));
    }

    /**
     * Checks if the given LPVSQueue object is not null and has a non-null repository URL.
     *
     * @param webhookConfig the LPVSQueue object to check
     * @throws IllegalArgumentException If the webhook configuration is null or if the repository URL is absent.
     */
    private static void checkWebhookConfig(LPVSQueue webhookConfig) {
        if (null == webhookConfig) {
            log.error("Webhook Config is absent");
            throw new IllegalArgumentException("Webhook Config is absent");
        }

        if (null == webhookConfig.getRepositoryUrl()) {
            log.error("No repository URL info in webhook config");
            throw new IllegalArgumentException("No repository URL info in webhook config");
        }
    }

    /**
     * Retrieves the organization name from the repository URL in the LPVSQueue object.
     *
     * @param webhookConfig LPVSQueue object containing repository information.
     * @return The organization name.
     */
    public static String getRepositoryOrganization(LPVSQueue webhookConfig) {
        checkWebhookConfig(webhookConfig);
        List<String> url = Arrays.asList(webhookConfig.getRepositoryUrl().split("/"));
        return url.get(url.size() - 2);
    }

    /**
     * Retrieves the repository name from the repository URL in the LPVSQueue object.
     *
     * @param webhookConfig LPVSQueue object containing repository information.
     * @return The repository name.
     */
    public static String getRepositoryName(LPVSQueue webhookConfig) {
        checkWebhookConfig(webhookConfig);
        List<String> url = Arrays.asList(webhookConfig.getRepositoryUrl().split("/"));
        return url.get(url.size() - 1);
    }

    /**
     * Retrieves the repository URL from the LPVSQueue object.
     *
     * @param webhookConfig LPVSQueue object containing repository information.
     * @return The repository URL.
     * @throws IllegalArgumentException If the provided LPVSQueue object is null.
     */
    public static String getRepositoryUrl(LPVSQueue webhookConfig) {
        if (null == webhookConfig) {
            log.error("Webhook Config is absent");
            throw new IllegalArgumentException("Webhook Config is absent");
        }
        return webhookConfig.getRepositoryUrl();
    }

    /**
     * Retrieves the pull request ID from the pull request URL in the LPVSQueue object.
     *
     * @param webhookConfig LPVSQueue object containing pull request information.
     * @return The pull request ID.
     * @throws IllegalArgumentException If the provided LPVSQueue object is null or if the pull request URL is absent.
     */
    public static String getPullRequestId(LPVSQueue webhookConfig) {
        checkWebhookConfig(webhookConfig);
        if (null == webhookConfig.getPullRequestUrl()) {
            log.error("Pull Request URL is absent in webhook config");
            throw new IllegalArgumentException("Pull Request URL is absent in webhook config");
        }

        List<String> url = Arrays.asList(webhookConfig.getPullRequestUrl().split("/"));
        return url.get(url.size() - 1);
    }

    /**
     * Generates a HttpHeaders object with a set of security headers for a web application.
     *
     * This method includes the following security headers:
     * - Strict-Transport-Security: Enables HSTS for one year, including subdomains.
     * - Content-Security-Policy: Minimal CSP allowing resources only from the same origin.
     * - X-Content-Type-Options: Prevents browsers from MIME-sniffing a response.
     * - X-Frame-Options: Prevents the content from being displayed in iframes.
     * - X-XSS-Protection: Enables XSS protection in modern browsers.
     * - Referrer-Policy: Specifies how much referrer information should be included with requests.
     * - Feature-Policy: Disallows the use of various browser features.
     * - Access-Control-Allow-Origin, Access-Control-Allow-Methods, Access-Control-Allow-Headers:
     *     Headers for Cross-Origin Resource Sharing (CORS) to control which origins are permitted to access resources.
     *
     * @return HttpHeaders object with a set of security headers.
     */
    public static HttpHeaders generateSecurityHeaders() {
        HttpHeaders headers = new HttpHeaders();

        // Enables HSTS for one year, including subdomains
        headers.add("Strict-Transport-Security", "max-age=31536000; includeSubDomains");

        // Minimal CSP allowing resources only from the same origin
        headers.add("Content-Security-Policy", "default-src 'self'");

        // Prevents browsers from MIME-sniffing a response
        headers.add("X-Content-Type-Options", "nosniff");

        // Prevents the content from being displayed in iframes
        headers.add("X-Frame-Options", "DENY");

        // Enables XSS protection in modern browsers
        headers.add("X-XSS-Protection", "1; mode=block");

        // Helps prevent clickjacking attacks by disallowing the content to be embedded in iframes
        headers.add("Referrer-Policy", "same-origin");

        // Helps mitigate the risk of cross-site scripting (XSS) attacks
        headers.add("Feature-Policy", "none");

        // Enables Cross-Origin Resource Sharing (CORS) with a restrictive policy
        headers.add("Access-Control-Allow-Origin", "same-origin");
        headers.add("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE");
        headers.add("Access-Control-Allow-Headers", "Content-Type");

        return headers;
    }

    /**
     * Retrieves an LPVSQueue configuration based on the GitHub repository and pull request.
     *
     * @param repo The GitHub repository.
     * @param pR   The GitHub pull request.
     * @return LPVSQueue configuration for the given GitHub repository and pull request.
     */
    public static LPVSQueue getGitHubWebhookConfig(GHRepository repo, GHPullRequest pR) {
        LPVSQueue webhookConfig = new LPVSQueue();
        webhookConfig.setPullRequestUrl(
                pR.getHtmlUrl() != null ? pR.getHtmlUrl().toString() : null);
        if (pR.getHead() != null
                && pR.getHead().getRepository() != null
                && pR.getHead().getRepository().getHtmlUrl() != null) {
            webhookConfig.setPullRequestFilesUrl(
                    pR.getHead().getRepository().getHtmlUrl().toString());
        } else {
            webhookConfig.setPullRequestFilesUrl(webhookConfig.getPullRequestUrl());
        }
        webhookConfig.setPullRequestAPIUrl(pR.getUrl() != null ? pR.getUrl().toString() : null);
        webhookConfig.setRepositoryUrl(
                repo.getHtmlUrl() != null ? repo.getHtmlUrl().toString() : null);
        webhookConfig.setUserId("Single scan of pull request run");
        webhookConfig.setHeadCommitSHA(pR.getHead() != null ? pR.getHead().getSha() : null);
        return webhookConfig;
    }
}
