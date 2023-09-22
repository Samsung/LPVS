/**
 * Copyright 2023 kyudori, Basaeng, hwan5180, quswjdgma83
 *
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */

import React from "react";
import { Link } from "react-router-dom";
import "../css/Login_style.css";

export const KAKAO_AUTH_URL = "http://localhost:7896/oauth2/authorization/kakao";
export const NAVER_AUTH_URL = "http://localhost:7896/oauth2/authorization/naver";
export const GOOGLE_AUTH_URL = "http://localhost:7896/oauth2/authorization/google";

export const Login = () => {
  return (
    <div className="login">
      <div className="div">
        <div className="SNS-login">
          <div className="overlap">
            <div className="login-header">
              <div className="overlap-group">
                <div className="line" alt="Line" />
                <div className="text-wrapper">SNS Login</div>
              </div>
            </div>
            <div className="sns-item naver">
              <div className="text-wrapper-4">Naver</div>
              <a href={NAVER_AUTH_URL}>
                <img className="sns-logo" alt="Naver login" src="/image/png/NaverLogin.png" />
              </a>
            </div>
            <div className="sns-item kakao">
              <div className="text-wrapper-3">Kakao</div>
              <a href={KAKAO_AUTH_URL}>
                <img className="sns-logo" alt="Kakao login" src="/image/png/KakaoLogin.png" />
              </a>
            </div>
            <div className="sns-item google">
              <div className="text-wrapper-2">Google</div>
              <a href={GOOGLE_AUTH_URL}>
                <img className="sns-logo" alt="Google login" src="/image/png/GoogleLogin.png" />
              </a>
            </div>
          </div>
        </div>
        <div className="menubar-top">
          <div className="menu-line" />
          <div className="menu"></div>
          <Link to={"/home"} style={{ color: "black", textDecoration: "none"}}>
            <img className="LPVS" alt="Lpvs" src="/image/png/LPVS_logo_bar.png" />
          </Link>
        </div>
      </div>
    </div>
  );
};

export default Login;
