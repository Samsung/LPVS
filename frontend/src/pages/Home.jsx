/**
 * Copyright 2023 kyudori, Basaeng, hwan5180, quswjdgma83
 *
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */

import React from "react";
import { Link } from "react-router-dom";
import "../css/Home_style.css";

export const Home = () => {

  return (
    <div className="home">
      <div className="div">
        <div className="LPVS-info">
          <div className="overlap-group">
            <p className="open-source-code">
              <span className="text-wrapper">
                  Welcome to the License Pre-Validation Service (LPVS).
    			<h3>Usage Procedure</h3>
    			<ol>
        		<li>Sign up and Login to the service.</li>
        		<li>Go to user information page, enter your GitHub ID (required) and Organization Name (optional), then click the "Admit" button.</li>
        		<li>Login to GitHub using the GitHub ID you entered in step 2.</li>
        		<li>To configure the repository for license validation, follow these steps:
            			<ol type="a">
                		<li>Go to the repository you want to validation.</li>
                		<li>Navigate to Settings -{">"} Webhooks -{">"} Add Webhooks.</li>
                		<li>Enter 'http://{"<"}IP where LPVS is running:7896/webhooks{">"}' in the Payload URL field.</li>
                		<li>Select 'application/json' for Content Type.</li>
                		<li>Enter 'LPVS' in the Secret field.</li>
                		<li>Under "Which events would you like to trigger this webhook?", select 'Let me select individual events.' and check only the 'Pull Request' option.</li>
                		<li>Click the green 'Add webhook' button.</li>
            			</ol>
        		</li>
        		<li>After completing the webhook setup for the repository, create a Pull Request on that repository to see the results of license validation.</li>
    			</ol>

    			<h3>Important Notes</h3>
    			<ul>
        		<li>If you enter a GitHub ID that is already used by someone else in your user information, it will not be reflected.</li>
        		<li>This service is only available for Public Repository.</li>
        		<li>Webhook settings are mandatory for using this service.</li>
    			</ul>
              </span>
            </p>
          </div>
        </div>
        <div className="LPVS-logo">
          <a href="https://github.com/Samsung/LPVS">
            <img className="LPVS-remove" alt="Lpvs remove" src="/image/LPVSLogo.png" />
          </a>
          <div className="license-pre">
            License <br />
            Pre
            <br />
            Validation <br />
            Service
          </div>
          <div className="text-wrapper-2">About</div>
        </div>
        <div className="menubar-top">
          <div className="menu-line" />
          <Link to={"/home"} style={{ color: "black", textDecoration: "none"}}>
          <img className="LPVS" alt="Lpvs" src="/image/LPVS_logo_bar.png" />
            </Link>
        </div>
      </div>
    </div>
  );
};

export default Home;
