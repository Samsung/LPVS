/**
 * Copyright (c) 2023, Samsung Electronics Co., Ltd. All rights reserved.
 *
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */

/**
 * This package contains utility classes used across the LPVS (License Plate Verification Service) application.
 * These utility classes provide various functionalities such as handling GitHub webhook payloads,
 * file and directory operations, configuration settings, and other common tasks.
 * <p>
 * The classes in this package are designed to encapsulate reusable and often-used functionality
 * that doesn't fit into the primary business logic of the application but supports it.
 * They aim to enhance code organization, maintainability, and reusability.
 * <p>
 * List of classes in this package:
 * <ul>
 *     <li>{@link com.lpvs.util.LPVSCommentUtil} - Utility methods for generating links and handling comments.</li>
 *     <li>{@link com.lpvs.util.LPVSExitHandler} - Utility for gracefully exiting the application.</li>
 *     <li>{@link com.lpvs.util.LPVSFileUtil} - Utility methods for file-related operations.</li>
 *     <li>{@link com.lpvs.util.LPVSWebhookUtil} - Utility methods for processing GitHub webhook payloads.</li>
 *     <li>{@link com.lpvs.util.LPVSTableConfiguration} - Configuration settings for database tables.</li>
 * </ul>
 * <p>
 * These utility classes are organized to promote modularity and ease of maintenance within the LPVS application.
 * </p>
 */
package com.lpvs.util;
