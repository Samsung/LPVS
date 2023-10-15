/**
 * Copyright 2023 kyudori, Basaeng, hwan5180, quswjdgma83
 *
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */

import {React,useEffect, useState} from "react";
import axios from 'axios';
import { Link, useParams, useLocation } from "react-router-dom";
import { useNavigate } from "react-router-dom";
import "../css/History_style.css";

export const History= () => {

  const { type, name } = useParams();
  const [ lpvsHistories, setlpvsHistories ] = useState();
  const navigate = useNavigate();

  const location = useLocation();
  const queryParams = new URLSearchParams(location.search);
  const page = queryParams.get('page') || 0;


  const [isLoggedIn, setIsLoggedIn] = useState(false);
  const [username, setUsername] = useState("");

  useEffect(() => {
    axios.get("/user/login")
      .then((loginresponse) => {
        if (loginresponse.data.isLoggedIn) {
          setIsLoggedIn(loginresponse.data.isLoggedIn);
          axios.get("/user/info")
            .then((userInfoResponse) => {
              setUsername(userInfoResponse.data);
            })
            .catch(function(error) {
              console.log(error.toJSON());
              navigate("/user/login");
            });
        } else {
          navigate("/user/login");
        }
      })
      .catch(function(error) {
        console.log(error.toJSON());
        navigate("/user/login");
      });
  }, []);

  useEffect(() => {
    axios
      .get(`/history/${type}/${name}?page=${page}`)
      .then((response) => {
        console.log(response.data)
          setlpvsHistories(response.data);
      })
      .catch(function(error) {
        console.log(error.toJSON());
        const userChoice = window.confirm("Please enter the GitHub ID on the User page.");
        if (userChoice) {
          navigate('/user/setting');
        } else {
          navigate(`/history/send/${username?.nickname}?page=0`);
        }
      });
  }, [type, name, page]);

  const handlePageChange = (pageNumber) => {
    navigate(`/history/${type}/${name}?page=${pageNumber}`);
  };

  const [isHistoriesEmpty, setIsHistoriesEmpty] = useState(false);
  console.log(lpvsHistories?.lpvsHistories?.length)
  useEffect(() => {
    if (lpvsHistories?.lpvsHistories?.length === 0) {
      setIsHistoriesEmpty(true);
    } else {
      setIsHistoriesEmpty(false);
    }
  }, [lpvsHistories, page]);
  
  console.log(lpvsHistories)

  const navigateToOrg = () => {
    if (!username?.organization || username?.organization.trim() === "") {
      window.alert("Please enter the Organization information on the User page.");
    } else {
      navigate(`/history/org/${username?.organization}?page=0`);
    }
  };
  const navigateToOwn = () => {
      navigate(`/history/own/${username?.nickname}?page=0`);
  };
  const navigateToSend = () => {
      navigate(`/history/send/${username?.nickname}?page=0`);
  };

   const [pageCount,setPageCount] = useState(1);
   const [currentPage, setCurrentPage] = useState(1);

   useEffect(()=> {
    setPageCount(Math.ceil(lpvsHistories?.count/5))
    }, [lpvsHistories?.count, currentPage]);

   console.log(pageCount);

   const check_page_plus =()=> {
    if(currentPage <= pageCount) {
      console.log(currentPage)
      return setCurrentPage(currentPage+5);
    }
    else {
      return 
    }
   }
   console.log(page);
   console.log(currentPage);
   const check_page_minus =(page)=> {
    if(page <=4) {
      return setCurrentPage(currentPage)
    }
    else {
      return setCurrentPage(currentPage-5);
    }
   }

   const trueOrFalse =(a)=> {
    if(a <= pageCount) {
      return true;
    }
    else {
      return false;
    }
  }

   const status_check=(a)=> {
    if(a) {
      return 'Issue-Detected';
    }
    else {
      return 'Scan-completed';
    }
   }
   console.log(lpvsHistories?.lpvsHistories[0]?.status)

  if (!lpvsHistories) {
    return <div>Loading...</div>;
  }

  function truncateName(name) {
        if (/[\u3131-\u314e\u314f-\u3163\uac00-\ud7a3]/g.test(name)) {
            return name.length > 3 ? `${name.substring(0, 3)}.` : name;
        } else {
            return name.length > 5 ? `${name.substring(0, 5)}.` : name;
        }
  }

  const pageArrow=(page)=> {
    if(page<=5) {
      return false;
    }
  }
  console.log(isHistoriesEmpty)
  return (
    <div className="history-send">
      <div className="div">
        <div className="overlap">
          <div className="option-menu">

            <div className="button">
              <div className="my-pull-request">
                <div className="overlap-group" onClick={navigateToSend} style={{ cursor: "pointer", color: "black", textDecoration: "none"}}>
                {type === "send" ?
                  <div className="text-wrapper-gray">My Pull Request</div>
                  :
                  <div className="text-wrapper">My Pull Request</div>}
                </div>
              </div>
              <div className="my-repo-pr-button">
                <div className="overlap-group" onClick={navigateToOwn} style={{ cursor: "pointer", color: "Black", textDecoration: "none"}}>
                {type === "own" ?
                  <div className="my-repo-PR-gray">My Repo PR</div>
                  :
                  <div className="my-repo-PR">My Repo PR</div>}
                </div>
              </div>
              <div className="my-org-PR-button">
                <div className="overlap-group"  onClick={navigateToOrg} style={{ cursor: "pointer", color: "Black", textDecoration: "none"}}>
                {type === "org" ?
                  <div className="text-wrapper-2-gray">My Org PR</div>
                  :
                  <div className="text-wrapper-2">My Org PR</div>}
                </div>
              </div>
            </div>

            <div className="my-option-header">
              <img className="my-option-line" alt="My option line" src="/image/svg/HistoryMyOptionLine.svg" />
              <div className="text-wrapper-3">My Option</div>
            </div>
          </div>
        </div>
        <div className="page-move-tool-bar">
          <div className="page-number">
          <img src="/image/svg/LeftArrow.svg" onClick={() => {
            if (page > 4) {
              check_page_minus();
              handlePageChange(currentPage - 2);
            }}} style={{ cursor: "pointer" }} />
            {trueOrFalse(currentPage) && <div className="text-wrapper-4" onClick={() => handlePageChange(currentPage-1)}>{currentPage}</div>}
            {trueOrFalse(currentPage+1) && <div className="text-wrapper-5" onClick={() => handlePageChange(currentPage)}>{currentPage+1}</div>}
            {trueOrFalse(currentPage+2) && <div className="text-wrapper-6" onClick={() => handlePageChange(currentPage+1)}>{currentPage+2}</div>}
            {trueOrFalse(currentPage+3) && <div className="text-wrapper-6" onClick={() => handlePageChange(currentPage+2)}>{currentPage+3}</div>}
            {trueOrFalse(currentPage+4) && <div className="text-wrapper-5" onClick={() => handlePageChange(currentPage+3)}>{currentPage+4}</div>}
            {trueOrFalse(currentPage+5) ? <img src="/image/svg/RightArrow.svg" onClick={() => {check_page_plus(); handlePageChange(currentPage + 4); }}style={{ cursor: "pointer" }}/> 
            : 
            <img src="/image/svg/RightArrow.svg" style={{ cursor: "pointer", opacity: 0.5 }}/>}
          </div>
        </div>
        <div className="overlap-2">
          <div className="history-box">
        {lpvsHistories.lpvsHistories[4]?.status !== undefined ? (      
          <Link to={`/result/${lpvsHistories.lpvsHistories[4]?.pullRequestId}`} style={{ color: "black", textDecoration: "none"}}>
        <div className="history">
            <div className="overlap-3">

              <div className="text-wrapper-7">
                  {lpvsHistories.lpvsHistories[4]?.repositoryName}
                <div className="status-scan-error">
                    <div className="overlap-10">
                      <div className={status_check(lpvsHistories.lpvsHistories[4]?.hasIssue) + '-text'}>
                        {status_check(lpvsHistories.lpvsHistories[4]?.hasIssue)}
                      </div>
                      <div className={status_check(lpvsHistories.lpvsHistories[4]?.hasIssue)} />
                      <div className="prid">
                        <div className="div-wrapper">
                          <div className="text-wrapper-10">{lpvsHistories.lpvsHistories[4]?.pullNumber}</div>
                        </div>
                      </div>
                    </div>
              </div>
            </div>

              <div className="text-wrapper-8">{lpvsHistories.lpvsHistories[4]?.url}</div>
              <div className="text-wrapper-9">{lpvsHistories.lpvsHistories[4]?.scanDate}</div>
            </div>
          </div></Link>    ): null}        

          {lpvsHistories.lpvsHistories[3]?.status !== undefined ? (  
            <Link to={`/result/${lpvsHistories.lpvsHistories[3]?.pullRequestId}`} style={{ color: "black", textDecoration: "none"}}>          
          <div className="overlap-wrapper">
              <div className="overlap-4">
                <div className="text-wrapper-11">
                  {lpvsHistories.lpvsHistories[3]?.repositoryName}
                  <div className="status-issue">
                    <div className="overlap-5">
                      <div className={status_check(lpvsHistories.lpvsHistories[3]?.hasIssue) + '-text'}>{status_check(lpvsHistories.lpvsHistories[3]?.hasIssue)}</div>
                      <div className={status_check(lpvsHistories.lpvsHistories[3]?.hasIssue)} />
                    </div>
                    <div className="overlap-group-wrapper">
                      <div className="div-wrapper">
                        <div className="text-wrapper-13">{lpvsHistories.lpvsHistories[3]?.pullNumber}</div>
                      </div>
                    </div>
                    
                  </div>
                </div>
                <div className="text-wrapper-8">{lpvsHistories.lpvsHistories[3]?.url}</div>
                <div className="text-wrapper-9">{lpvsHistories.lpvsHistories[3]?.scanDate}</div>
              </div>
            </div>
            </Link>): null}

            {lpvsHistories.lpvsHistories[2]?.status !== undefined ? (
              <Link to={`/result/${lpvsHistories.lpvsHistories[2]?.pullRequestId}`} style={{ color: "black", textDecoration: "none"}}>
             <div className="history-2">
              <div className="overlap-6">
                <div className="text-wrapper-15">
                  {lpvsHistories.lpvsHistories[2]?.repositoryName}
                  <div className="status-scan">
                    <div className="overlap-5">
                      <div className={status_check(lpvsHistories.lpvsHistories[2]?.hasIssue) + '-text'}>{status_check(lpvsHistories.lpvsHistories[2]?.hasIssue)}</div>
                      <div className={status_check(lpvsHistories.lpvsHistories[2]?.hasIssue)} />
                    </div>
                    <div className="prid-2">
                      <div className="div-wrapper">
                        <div className="text-wrapper-16">{lpvsHistories.lpvsHistories[2]?.pullNumber}</div>
                      </div>
                    </div>
                  </div>
                </div>
                <div className="text-wrapper-8">{lpvsHistories.lpvsHistories[2]?.url}</div>
                <div className="text-wrapper-9">{lpvsHistories.lpvsHistories[2]?.scanDate}</div>
              </div>
            </div>
            </Link>
            ): null}

            {lpvsHistories.lpvsHistories[1]?.status !== undefined ? (   
              <Link to={`/result/${lpvsHistories.lpvsHistories[1]?.pullRequestId}`} style={{ color: "black", textDecoration: "none"}}>
            <div className="history-3">
              <div className="overlap-7">
                <div className="text-wrapper-17">
                  {lpvsHistories.lpvsHistories[1]?.repositoryName}
                  <div className="status-scan-2">
                    <div className="overlap-8">
                      <div className={status_check(lpvsHistories.lpvsHistories[1]?.hasIssue) + '-text'}>{status_check(lpvsHistories.lpvsHistories[1]?.hasIssue)}</div>
                      <div className={status_check(lpvsHistories.lpvsHistories[1]?.hasIssue)} />
                    </div>
                    <div className="prid-3">
                      <div className="div-wrapper">
                        <div className="text-wrapper-18">{lpvsHistories.lpvsHistories[1]?.pullNumber}</div>
                      </div>
                    </div>
                  </div>
                </div>
                <div className="text-wrapper-8">{lpvsHistories.lpvsHistories[1]?.url}</div>
                <div className="text-wrapper-9">{lpvsHistories.lpvsHistories[1]?.scanDate}</div>
              </div>
            </div>
            </Link>         
            ) : null}
            {lpvsHistories.lpvsHistories[0]?.status !== undefined ? (            
            <Link to={`/result/${lpvsHistories.lpvsHistories[0]?.pullRequestId}`} style={{ color: "black", textDecoration: "none"}}>
            <div className="history-4">
              <div className="overlap-9">
                <div className="text-wrapper-19">
                  {lpvsHistories.lpvsHistories[0]?.repositoryName}
                  <div className="status-issue-2">
                    <div className="overlap-5">
                      <div className={status_check(lpvsHistories.lpvsHistories[0]?.hasIssue) + '-text'}>{status_check(lpvsHistories.lpvsHistories[0]?.hasIssue)}</div>
                      <div className={status_check(lpvsHistories.lpvsHistories[0]?.hasIssue)} />
                    </div>
                    <div className="prid-4">
                      <div className="div-wrapper">
                        <div className="text-wrapper-20">{lpvsHistories.lpvsHistories[0]?.pullNumber}</div>
                      </div>
                    </div>
                  </div>
                </div>
                <div className="text-wrapper-8">{lpvsHistories.lpvsHistories[0]?.url}</div>
                <div className="text-wrapper-9">{lpvsHistories.lpvsHistories[0]?.scanDate}</div>
              </div>
            </div>
            </Link>
            ): null}
          </div>
        </div>
        <div className="menubar-top">
          <div className="menu-line" />
          <div className="menu">
            <div className="overlap-11">
              <div className="profile-border" />
              <div className="profile">
                <div className="overlap-group-2">
                  <img className="image" alt="Image" src="/image/png/ProfileImg.png" />
                  <div className="text-wrapper-23"><Link to={"/user/setting"} style={{ color: "black", textDecoration: "none"}}>{username.name ? (
                      <div>{truncateName(username.name)}</div>
                  ) : (
                      <div>Loading...</div>
                  )}
                  </Link></div>
                </div>
              </div>
            </div>
              <div className="text-wrapper-22"><Link to={`/dashboard/send/${username?.nickname}`} style={{ color: "black", textDecoration: "none"}}>Dashboard</Link></div>
              <div className="text-wrapper-21">
                <Link 
                to={`/history/send/${username?.nickname}?page=0`} 
                style={{ color: "black", textDecoration: "none"}}>History
                </Link>
              </div>
                <Link to={"/home"} 
                style={{ color: "black", textDecoration: "none"}}>
                <img className="LPVS" alt="img" src="/image/png/LPVS_logo_bar.png" />
                </Link>
          </div>
        </div>
      </div>
    </div>
  );
};

export default History;
