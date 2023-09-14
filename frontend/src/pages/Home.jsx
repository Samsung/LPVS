/**
 * Copyright 2023 kyudori, Basaeng, hwan5180, quswjdgma83
 *
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */

import React, { useState, useEffect } from "react";
import axios from "axios";
import { Link } from "react-router-dom";
import "../css/Home_style.css";

export const Home = () => {
  const [isLoggedIn, setIsLoggedIn] = useState(false);
  const [username, setUsername] = useState("");

  useEffect(() => {
    axios.get("/login/check").then((loginresponse) => {
      if (loginresponse.data.isLoggedIn) {
        setIsLoggedIn(loginresponse.data.isLoggedIn);
        axios.get("/user/info").then((userInfoResponse) => {
          setUsername(userInfoResponse.data);
        });
      }
    });
  }, []);

  function truncateName(name) {
    if (/[\u3131-\u314e\u314f-\u3163\uac00-\ud7a3]/g.test(name)) {
      return name.length > 3 ? `${name.substring(0, 3)}.` : name;
    } else {
      return name.length > 5 ? `${name.substring(0, 5)}.` : name;
    }
  }

  return (
    <div className="home">
      <div className="div">
        <div className="LPVS-info">
          <div className="overlap-group">
            <div className="user-guide">
              <p>Welcome to the License Pre-Validation Service (LPVS).</p>
              <h3>Usage Procedure</h3>
              <ol>
                <li>Sign up and Login to the service.</li>
                <li>Go to user information page, enter your GitHub ID (required) and Organization Name (optional), then click the "Admit" button.</li>
                <li>Login to GitHub using the GitHub ID you entered in step 2.</li>
                <li>To configure the repository for license validation, follow these steps:</li>
                <ol type="a">
                  <li>Go to the repository you want to validation.</li>
                  <li>Navigate to Settings -{">"} Webhooks -{">"} Add Webhooks.</li>
                  <li>Enter 'http://{"<"}IP where LPVS is running:7896/webhooks{">"}' in the Payload URL field.</li>
                  <li>Select 'application/json' for Content Type.</li>
                  <li>Enter 'LPVS' in the Secret field.</li>
                  <li>Under "Which events would you like to trigger this webhook?", select 'Let me select individual events.' and check only the 'Pull Request' option.</li>
                  <li>Click the green 'Add webhook' button.</li>
                </ol>
                <li>After completing the webhook setup for the repository, create a Pull Request on that repository to see the results of license validation.</li>
              </ol>
              <h3>Important Notes</h3>
              <ul>
                <li>If you enter a GitHub ID that is already used by someone else in your user information, it will not be reflected.</li>
                <li>This service is only available for Public Repository.</li>
                <li>Webhook settings are mandatory for using this service.</li>
              </ul>
            </div>
          </div>
        </div>
        <div className="LPVS-logo">
          <a href="https://github.com/Samsung/LPVS">
            <img className="LPVS-github" alt="img" src="/image/png/LPVSLogo.png" />
          </a>
          <div className="license-explain">
            License <br />
            Pre <br />
            Validation <br />
            Service
          </div>
          <div className="text-wrapper-2">About</div>
        </div>
        <div className="menubar-top">
          <div className="menu-line" />
          <div className="menu">
            <div className="overlap">
              <div className="profile-border" />
              <div className="profile">
                <div className="overlap-group-2">
                  <img className="image" alt="img" src="/image/png/ProfileImg.png" />
                  <div className="text-wrapper-3">
                    {isLoggedIn ? (
                      <span style={{ color: "black", textDecoration: "none" }}>
                        <Link
                          to={"/home"}
                          style={{ color: "black", textDecoration: "none" }}
                        >
                          {username?.name ? (
                            <div>{truncateName(username.name)}</div>
                              ) : (
                              <div>Loading...</div>
                          )}
                        </Link>
                      </span>
                    ) : (
                      <Link
                        to={"/login"}
                        style={{ color: "black", textDecoration: "none" }}
                      >
                        Login
                      </Link>
                    )}
                  </div>
                </div>
              </div>
            </div>
          </div>
          <Link to={"/home"} style={{ color: "black", textDecoration: "none"}}>
            <img className="LPVS" alt="img" src="/image/png/LPVS_logo_bar.png" />
          </Link>
        </div>
      </div>
    </div>
  );
};

export default Home;
