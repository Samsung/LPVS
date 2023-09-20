/**
 * Copyright 2023 kyudori, Basaeng, hwan5180, quswjdgma83
 *
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */

import React, { useEffect, useState } from "react";
import axios from "axios";
import { Link } from "react-router-dom";
import { useNavigate } from "react-router-dom";
import "../css/User_style.css";
import { LPVS_SERVER } from "./Home";

export const User = () => {

  const navigate = useNavigate();

  const [isLoggedIn, setIsLoggedIn] = useState(false);
  const [userInfo, setUserInfo] = useState(null);
  const [userInfoChanged, setUserInfoChanged] = useState(false);

  const [isEditing_nickname, setIsEditing_nickname] = useState(false);
  const [isEditing_organization, setIsEditing_organization] = useState(false);

  const [editedUserInfo, setEditedUserInfo] = useState({
    nickname: "",
    organization: "",
  });
  
  useEffect(() => {
    axios.get("/login/check")
      .then((loginresponse) => {
        if (loginresponse.data.isLoggedIn) {
          setIsLoggedIn(loginresponse.data.isLoggedIn);
          axios.get("/user/info").then((userInfoResponse) => {
            setUserInfo(userInfoResponse.data);
          });
        }
        else navigate("/login")
      })
      .catch(function(error) {
        console.log(error.toJSON());
      });
  }, []);

  useEffect(() => {
    if (userInfo) {
        setEditedUserInfo({
            nickname: userInfo.nickname || "",
            organization: userInfo.organization || "",
        });
    }
  }, [userInfo]);

  const handleAdmitClick = () => {

    if (!editedUserInfo.nickname) {
      alert("Must enter a GitHub ID.");
      return;
    }
  
    axios({
      url: `/user/update`, 
      method: 'post',
      data: {
        nickname: editedUserInfo?.nickname,
        organization: editedUserInfo?.organization 
      },
      headers: { "Access-Control-Allow-Origin": "*" },
    })
    .then((response) => {
      return axios.get("/user/info");
    })
    .then((userInfoResponse) => {
      setUserInfoChanged(true);
      setIsEditing_nickname(false)
      setIsEditing_organization(false)
  
      setUserInfo(userInfoResponse.data);
      console.log(userInfoResponse.data);

      if(!editedUserInfo.organization){
        alert(`User information has been updated.\nGitHubID: ${editedUserInfo.nickname}`);
      } else {
        alert(`User information has been updated.\nGitHubID: ${editedUserInfo.nickname}\nOrganization: ${editedUserInfo.organization}`);
      }
    })
    .catch((error) => {
      if (error.response && error.response.status === 409) {
        alert("Another user is already using this GitHubID.");
      } else {
        console.error("An error occurred:", error);
      }
    });
  };

  const handleUsernameChange = (event) => {
    setEditedUserInfo(prevState => ({
      ...prevState,
      nickname: event.target.value
    }));
  };

  const handleOrganizationChange = (event) => {
    setEditedUserInfo(prevState => ({
      ...prevState,
      organization: event.target.value
    }));
  };

  const handleLogoClick = (event) => {
    event.preventDefault();

    if (!userInfo?.nickname) {
      alert('To use service, You must enter a GitHub ID.');
      navigate('/user/info');
    } else {
      navigate('/home');
    }
  };

  function truncateName(name) {
    if (/[\u3131-\u314e\u314f-\u3163\uac00-\ud7a3]/g.test(name)) {
      return name.length > 3 ? `${name.substring(0, 3)}.` : name;
    } else {
      return name.length > 5 ? `${name.substring(0, 5)}.` : name;
    }
  }

  return (
    <div className="user">
      <div className="div">
        <div className="setting">
          <div className="overlap">
            <div className="overlap-group">
              <div className="logout-button">
                <div className="overlap-group-2">
                  <div className="text-wrapper"><a style={{color:"black", textDecoration:"none"}} href = {LPVS_SERVER+"/oauth/logout"}> Logout </a></div>
                  <div className="logout-rect" />
                </div>
              </div>
              <div className="profile-header">
                <div className="overlap-2">
                  <div className="profile-line" alt="Profile line" />
                  <div className="text-wrapper-2">Profile</div>
                </div>
              </div>
            </div>
            <div className="admit-button">
              <div className="overlap-3">
                <div className="text-wrapper-3" onClick={handleAdmitClick}>Admit</div>
                <div className="admit-rect" />
              </div>
            </div>
            <div className="overlap-4">
              <div className="organization-edit" />
              <div className="github-id-edit">
                <div className="overlap-5">
                <div className="text-wrapper-4" onClick={() => setIsEditing_nickname(!isEditing_nickname)}>
                  {isEditing_nickname ? "cancel" : "edit"}
                </div>
                  <div className="admit-rect-2" />
                </div>
                <div className="text-wrapper-5" onClick={()=>setIsEditing_organization(!isEditing_organization)}>
                  {isEditing_organization ? "cancel" : "edit"}
                </div>
              </div>
            </div>
            <div className="profile-title">
              <div className="text-wrapper-6">Organization name</div>
              <div className="text-wrapper-7">Github ID</div>
              <div className="text-wrapper-8">Social</div>
              <div className="text-wrapper-9">E-mail</div>
              <div className="text-wrapper-10">Username</div>
            </div>
            <div className="profile-input-box">
              <div className="organization-box">
                <div className="div-wrapper">
                {isEditing_organization ? (
                    <input
                       type="text"
                       value={editedUserInfo.organization || ""}
                       onChange={handleOrganizationChange}
                       placeholder={userInfo?.organization}
                    />
                   ) : (
                     <p className="org-text">
                       {userInfo?.organization
                       ? userInfo.organization
                       : "Type your Organization name (Optional)"}
                     </p>
                      )}
                </div>
              </div>
              <div className="github-ID-box">
                <div className="div-wrapper">
                {isEditing_nickname ? (
                     <input
                      type="text"
                      value={editedUserInfo.nickname || ""}
                      onChange={handleUsernameChange}
                      placeholder={userInfo?.nickname}
                      className="input-style" 
                     />
                   ) : (
                    <p className="text-wrapper-11">
                      {userInfo?.nickname
                         ? userInfo.nickname
                         : "Type your Github ID (Must be filled)"}
                   </p>
                  )}
                </div>
              </div>
              <div className="social-box">
                <div className="div-wrapper">
                  <div className="text-wrapper-12">
                    {userInfo?.provider ? (
                      <div>
                        {userInfo.provider}
                        </div>
                      ) : (
                        <div>Loading...</div>
                      )}
                    </div>
                </div>
              </div>

              <div className="email-box">
                <div className="div-wrapper">
                  <div className="text-wrapper-13">
                  {userInfo?.email ? (
                    <div>{userInfo.email}</div>
                  ) : (
                    <div>Loading...</div>
                  )}
                    </div>
                </div>
              </div>
              <div className="user-name-box">
                <div className="div-wrapper">
                  <div className="text-wrapper-14">
                    {userInfo?.name ? (
                    <div>{userInfo.name}</div>
                  ) : (
                    <div>Loading...</div>
                  )}
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
        <div className="menubar-top">
          <div className="menu-line" />
          <div className="menu">
            <div className="overlap-6">
                <div className="profile">
                  <div className="overlap-group-3">
                    <img className="image" alt="Image" src="/image/png/ProfileImg.png" />
                      <div className="text-wrapper-16">
                      <span style={{ color: "black", textDecoration: "none" }}>
                        <Link
                          to={"/user/info"}
                          style={{ color: "black", textDecoration: "none" }}
                        >
                          {userInfo?.name ? (
                            <div>{truncateName(userInfo.name)}</div>
                            ) : (
                            <div>Loading...</div>
                          )}
                        </Link>
                      </span>
                          </div>
                        </div>
                </div>
              </div>
          </div>
          <Link to="/home" style={{ color: 'black', textDecoration: 'none' }}>
            <img
              className="LPVS"
              alt="img"
              src="/image/png/LPVS_logo_bar.png"
              onClick={handleLogoClick}
            />
          </Link>
        </div>
      </div>
    </div>
  );
};

export default User;
