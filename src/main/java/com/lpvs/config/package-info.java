/**
 * Copyright (c) 2023-2025, Samsung Electronics Co., Ltd. All rights reserved.
 *
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */

/**
 * This package contains configuration classes related to the security setup of the application.
 *
 * <p>
 * The central class in this package is {@link com.lpvs.config.SecurityConfig}, which configures
 * the application's security filter chain. Currently, it is set up to permit all requests
 * and disable CSRF protection, as the application does not have a user-facing frontend
 * requiring authentication or authorization.
 * </p>
 */
package com.lpvs.config;
