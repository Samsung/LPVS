/**
 * This package contains the controller classes for handling various aspects of the License Pre-Validation Service (LPVS).
 * Controllers in this package manage interactions related to GitHub webhooks, user interfaces, and API endpoints.
 *
 * - {@link com.lpvs.controller.GitHubWebhooksController}: Manages GitHub webhook events, processes payloads, and interacts
 *   with LPVS services for queue handling and GitHub operations.
 *
 * - {@link com.lpvs.controller.LPVSWebController}: Controls the web interface and API endpoints for LPVS, including user
 *   information, login status, user settings, history, results, and dashboard-related functionalities.
 *
 * These controllers play a crucial role in integrating LPVS functionalities into different parts of the application,
 * such as handling external events, providing user interfaces, and exposing APIs for external integrations.
 */
package com.lpvs.controller;
