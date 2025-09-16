/**
 * Copyright (c) 2023-2025, Samsung Electronics Co., Ltd. All rights reserved.
 *
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */

/**
 * This package contains the controller classes for handling various aspects of the License Pre-Validation Service (LPVS).
 * Controllers in this package manage interactions related to GitHub webhooks and service health checks.
 * <p>
 * - {@link com.lpvs.controller.GitHubController}: Manages GitHub webhook events, processes payloads, and interacts
 *   with LPVS services for queue handling and GitHub operations.
 * - {@link com.lpvs.controller.HealthController}: Provides a health check endpoint to monitor the service,
 *   specifically returning the current length of the processing queue.
 * </p>
 * These controllers play a crucial role in integrating LPVS functionalities into different parts of the application,
 * such as handling external events and providing service status information.
 */
package com.lpvs.controller;
