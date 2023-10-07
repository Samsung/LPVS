/**
 * Copyright 2023 kyudori, Basaeng, hwan5180, quswjdgma83
 *
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */

import { Link, useParams, useNavigate, useLocation } from "react-router-dom";
import "../css/Result_style.css";
import axios from "axios";
import React, { useState, useEffect } from "react";
import { PieChart, Pie, Sector, Cell, ResponsiveContainer, Tooltip } from 'recharts';

export const Result = () => {

  const [ isLoggedIn, setIsLoggedIn ] = useState(false);
  const [username, setUsername] = useState("");
  
  const { pull_request_id } = useParams();
  const [ lpvsResult, setLPVSResult ] = useState();
  const [ licenseDetection, setlicenseDetection ] = useState(false);
  
  const navigate = useNavigate();
  const location = useLocation();
  const queryParams = new URLSearchParams(location.search);
  const page = queryParams.get('page') || 0;
  
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
    .get(`/result/${pull_request_id}?page=${page}`)
    .then((response) => {
      setLPVSResult(response.data);
      console.log(response.data?.lpvsResultInfo.status);
      console.log(response.data)
    })   
    .catch(function(error) {
        console.log(error.toJSON());
        const userChoice = window.confirm("Please enter the GitHub ID on the User page.");
        if (userChoice) {
          navigate('/user/setting');
        } else {
          navigate(`/result/${pull_request_id}?page=0`);
        }
    });
  }, [pull_request_id, page]);

  //===============================LabelSetting==============================================================
  const setLabel = (number) => {
    if (lpvsResult && lpvsResult.lpvsResultFileList && lpvsResult.lpvsResultFileList[number]) {
      const status = lpvsResult.lpvsResultFileList[number].status;
      if (status === 'PERMITTED') {
        return {
          backgroundColor: '#0000ff42',
          borderColor: '#0000ff'
        };
      } else if (status === 'PROHIBITED') {
        return {
          backgroundColor: '#ff000054',
          borderColor: '#ff0000'
        };
      } else if (status === 'RESTRICTED') {
        return {
          backgroundColor: '#842b2b54',
          borderColor: '#8b0000'
        };
      } else {
        return {
          backgroundColor: '#D9D9D9',
          borderColor: '#000000'
        };
      }
    } else {
      return {
        backgroundColor: '#D9D9D9',
        borderColor: '#000000'
      };
    }
  };
  console.log(lpvsResult);

// ===============================FontSetting==============================================================
const setFont = (number) => {
  if (lpvsResult && lpvsResult.lpvsResultFileList && lpvsResult.lpvsResultFileList[number]) {
    const status = lpvsResult.lpvsResultFileList[number].status;
    if (status === 'PERMITTED') {
      return {
        color: '#0000ff'
      };
    } else if (status === 'PROHIBITED') {
      return {
        color: '#ff0000'
      };
    } else if (status === 'RESTRICTED') {
      return {
        color: '#8b0000'
      };
    } else {
      return {
        color: '#000000'
      };
    }
  } else {
    return {
      color: '#000000'
    };
  }
};

//===============================FontLocation==============================================================
const setFontAlign = (number) => {
  if (lpvsResult && lpvsResult.lpvsResultFileList && lpvsResult.lpvsResultFileList[number]) {
    const status = lpvsResult.lpvsResultFileList[number].status;
    if (status === 'PERMITTED') {
      return 'permitted';
    } else if (status === 'PROHIBITED') {
      return 'prohibited';
    } else if (status === 'RESTRICTED') {
      return 'restricted';
    } 
      else if (status === 'UNREVIEWED') {
      return 'unreviewed';
    } else {
      return 'nofile';
    }
  } else {
    return 'nofile';
  }
};

//===============================BottomButton==============================================================
const [pageCount,setPageCount] = useState(1);
const [currentPage, setCurrentPage] = useState(1);

   useEffect(()=> {
    setPageCount(Math.ceil(lpvsResult?.count/5))
    }, [lpvsResult?.count, currentPage]);

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

   const check_page_minus =()=> {
    if(currentPage <=1) {
      return 
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

//===============================PageMove==============================================================

  const handlePageChange = (pageNumber) => {
    navigate(`/result/${pull_request_id}?page=${pageNumber}`);
  };

  const [isResultEmpty, setIsResultEmpty] = useState(false);
  console.log(lpvsResult?.lpvsResultFileList?.length)
  useEffect(() => {
    if (lpvsResult?.lpvsResultFileList?.length === 0) {
      setIsResultEmpty(true);
    } else {
      setIsResultEmpty(false);
    }
  }, [lpvsResult, page]);

  //==================================ScanDateFromat========================================================
  function formatDate(dateStr) {
    const dateObj = new Date(dateStr);
    const year = dateObj.getFullYear();
    const month = String(dateObj.getMonth() + 1).padStart(2, '0');
    const day = String(dateObj.getDate()).padStart(2, '0');
    const hours = String(dateObj.getHours()).padStart(2, '0');
    const minutes = String(dateObj.getMinutes()).padStart(2, '0');
    const seconds = String(dateObj.getSeconds()).padStart(2, '0');
    return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`;
}

//===============================LicenseCounting==============================================================
const licenseCount = () => {
  let count = (lpvsResult?.licenseCountMap?.MIT || 0) 
  + (lpvsResult?.licenseCountMap['Apache-2.0'] || 0) 
  + (lpvsResult?.licenseCountMap['GPL-3.0-only'] || 0) 
  + (lpvsResult?.licenseCountMap.OpenSSL || 0) 
  + (lpvsResult?.licenseCountMap['GPL-2.0-or-later'] || 0);    
  return count;
}

//===============================PieChart==============================================================
  const COLORS = ['#A31F34', '#7F237D', '#000000', '#73C541', '007C31', '#F28300'];
    const data = [
    { name: "MIT", value: lpvsResult?.licenseCountMap?.MIT },
    { name: "Apache 2.0", value: lpvsResult?.licenseCountMap['Apache-2.0'] },
    { name: "GNU GP", value: lpvsResult?.licenseCountMap['GPL-3.0-only'] },
    { name: "Open SSL", value: lpvsResult?.licenseCountMap.OpenSSL },
    { name: "GNU LG", value: lpvsResult?.licenseCountMap['GPL-2.0-or-later'] },
    { name: "Unsupported License", value: lpvsResult?.count - licenseCount()}
  ];

  return (
    <div className="result-pull-request">
      <div className="div">
        <div className="number-menu">
          <div className="frame">
            <div className="frame-2">
              <img className="arrow" alt="Arrow" src="/image/svg/LeftArrow.svg" onClick={() => {
                if (page > 4) {
                  check_page_minus(); 
                  handlePageChange(currentPage -2);
                }}} style={{ cursor: "pointer" }} />
              {trueOrFalse(currentPage) && <div className="text-wrapper" onClick={() => handlePageChange(currentPage-1)}>{currentPage}</div>}
              {trueOrFalse(currentPage+1) && <div className="text-wrapper-2" onClick={() => handlePageChange(currentPage)}>{currentPage+1}</div>}
              {trueOrFalse(currentPage+2) && <div className="text-wrapper-3" onClick={() => handlePageChange(currentPage+1)}>{currentPage+2}</div>}
              {trueOrFalse(currentPage+3) && <div className="text-wrapper-4" onClick={() => handlePageChange(currentPage+2)}>{currentPage+3}</div>}
              {trueOrFalse(currentPage+4) && <div className="text-wrapper-5" onClick={() => handlePageChange(currentPage+3)}>{currentPage+4}</div>}
              {trueOrFalse(currentPage+5) ? <img className="img" alt="Img" src="/image/svg/RightArrow.svg" onClick={() => {check_page_plus(); handlePageChange(currentPage + 4);}} style={{ cursor: "pointer" }}/>
              :
              <img src="/image/svg/RightArrow.svg" style={{ cursor: "pointer", opacity: 0.5 }}/>}
            </div>
          </div>
        </div>
        <div className="detected-file-list">
          <div className="overlap">
            <img className="detected-file-list-2" alt="Detected file list" src="/image/svg/DetectedFileListRec.svg" />
            <div className="match-component">
              <div className="text-wrapper-6">Component</div>
            </div>
            <div className="license-chart">
              <div className="text-wrapper-7" style={setFont(4)} >{lpvsResult?.lpvsResultFileList[4]?.licenseSpdx}</div>
              <div className="text-wrapper-8" style={setFont(3)} >{lpvsResult?.lpvsResultFileList[3]?.licenseSpdx}</div>
              <div className="text-wrapper-9" style={setFont(2)} >{lpvsResult?.lpvsResultFileList[2]?.licenseSpdx}</div>
              <div className="text-wrapper-10" style={setFont(1)} >{lpvsResult?.lpvsResultFileList[1]?.licenseSpdx}</div>
              <div className="text-wrapper-11" style={setFont(0)} >{lpvsResult?.lpvsResultFileList[0]?.licenseSpdx}</div>
              <div className="text-wrapper-12">Detected Licenses</div>
            </div>
            <div className="match-value-chart">
              <div className="text-wrapper-13">Match Value</div>
              <div className="text-wrapper-14">{lpvsResult?.lpvsResultFileList[4]?.matchValue}</div>
              <div className="text-wrapper-15">{lpvsResult?.lpvsResultFileList[3]?.matchValue}</div>
              <div className="text-wrapper-16">{lpvsResult?.lpvsResultFileList[2]?.matchValue}</div>
              <div className="text-wrapper-17">{lpvsResult?.lpvsResultFileList[1]?.matchValue}</div>
              <div className="text-wrapper-18">{lpvsResult?.lpvsResultFileList[0]?.matchValue}</div>
            </div>
            <div className="match-line-chart">
              <div className="text-wrapper-19">{lpvsResult?.lpvsResultFileList[4]?.matchLine}</div>
              <div className="text-wrapper-20">{lpvsResult?.lpvsResultFileList[3]?.matchLine}</div>
              <div className="text-wrapper-21">{lpvsResult?.lpvsResultFileList[2]?.matchLine}</div>
              <div className="text-wrapper-22">{lpvsResult?.lpvsResultFileList[1]?.matchLine}</div>
              <div className="text-wrapper-23">{lpvsResult?.lpvsResultFileList[0]?.matchLine}</div>
              <div className="text-wrapper-24">Match Line</div>
            </div>
            <div className="path-chart">
              <div className="text-wrapper-25-container">
                <div className={`text-wrapper-25 ${lpvsResult?.lpvsResultFileList[4]?.path.length >= 28 ? 'long' : 'short'}`}>{lpvsResult?.lpvsResultFileList[4]?.path}</div>
              </div>
              <div className="text-wrapper-26-container">
                <div className={`text-wrapper-26 ${lpvsResult?.lpvsResultFileList[3]?.path.length >= 28 ? 'long' : 'short'}`}>{lpvsResult?.lpvsResultFileList[3]?.path}</div>
              </div>
              <div className="text-wrapper-27-container">
                <div className={`text-wrapper-27 ${lpvsResult?.lpvsResultFileList[2]?.path.length >= 28 ? 'long' : 'short'}`}>{lpvsResult?.lpvsResultFileList[2]?.path}</div>
              </div>
              <div className="text-wrapper-28-container">
                <div className={`text-wrapper-28 ${lpvsResult?.lpvsResultFileList[1]?.path.length >= 28 ? 'long' : 'short'}`}>{lpvsResult?.lpvsResultFileList[1]?.path}</div>
              </div>
              <div className="text-wrapper-29-container">
                <div className={`text-wrapper-29 ${lpvsResult?.lpvsResultFileList[0]?.path.length >= 28 ? 'long' : 'short'}`}>{lpvsResult?.lpvsResultFileList[0]?.path}</div>
              </div>
              <div className="text-wrapper-30">Path</div>
            </div>
            <div className="status">
              <div className="permitted">
              <div className="overlap-group">
                <div className={"text-wrapper-31-" + setFontAlign(4)} >
                {lpvsResult?.lpvsResultFileList[4] != undefined && lpvsResult?.lpvsResultFileList[4]?.status != null ? (
                  lpvsResult?.lpvsResultFileList[4]?.status
                  ) : (
                  <>No File</>
                  )}
                </div>
              <div className="rectangle" style={setLabel(4)} /></div>
              </div>
              <div className="overlap-wrapper">
                <div className="overlap-2">
                <div className={"text-wrapper-31-" + setFontAlign(3)} >
                {lpvsResult?.lpvsResultFileList[3] != undefined && lpvsResult?.lpvsResultFileList[3]?.status != null ? (
                  lpvsResult?.lpvsResultFileList[3]?.status
                  ) : (
                    <>No File</>
                  )}
                </div>
                  <div className="rectangle" style={setLabel(3)} />
                </div>
              </div>
              <div className="prohibited">
                <div className="overlap-group">
                <div className={"text-wrapper-31-" + setFontAlign(2)} >
                {lpvsResult?.lpvsResultFileList[2] != undefined && lpvsResult?.lpvsResultFileList[2]?.status != null ? (
                  lpvsResult?.lpvsResultFileList[2]?.status
                  ) : (
                    <>No File</>
                  )}
                </div>
                  <div className="status-label" style={setLabel(2)} />
                </div>
              </div>
              <div className="restricted">
                <div className="overlap-3">
                <div className={"text-wrapper-31-" + setFontAlign(1)} >
                {lpvsResult?.lpvsResultFileList[1] != undefined && lpvsResult?.lpvsResultFileList[1]?.status != null ? (
                  lpvsResult?.lpvsResultFileList[1]?.status
                  ) : (
                    <>No File</>
                  )}
                </div>
                  <div className="status-label-2" style={setLabel(1)} />
                </div>
              </div>
              <div className="overlap-group-wrapper">
                <div className="overlap-group">
                <div className={"text-wrapper-31-" + setFontAlign(0)} >
                {lpvsResult?.lpvsResultFileList[0] != undefined && lpvsResult?.lpvsResultFileList[0]?.status != null ? (
                  lpvsResult?.lpvsResultFileList[0]?.status
                  ) : (
                    <>No File</>
                  )}
                </div>
                  <div className="rectangle" style={setLabel(0)} />
                </div>
              </div>
              <div className="text-wrapper-35">Status</div>
            </div>
            <img className="list-line" alt="List line" src="/image/svg/ListLine.svg" />
            {lpvsResult?.lpvsResultFileList[4] != undefined && lpvsResult?.lpvsResultFileList[4]?.componentFileUrl != null ? (<img className="component" alt="Component" src="/image/png/Component.png" onClick={()=>{window.open(lpvsResult?.lpvsResultFileList[4].componentFileUrl)}} />
            ) : (
              null
            )}
            {lpvsResult?.lpvsResultFileList[3] != undefined && lpvsResult?.lpvsResultFileList[3]?.componentFileUrl != null ? (<img className="component-2" alt="Component" src="/image/png/Component.png" onClick={()=>{window.open(lpvsResult?.lpvsResultFileList[3].componentFileUrl)}} />
            ) : (
              null
            )}
            {lpvsResult?.lpvsResultFileList[2] != undefined && lpvsResult?.lpvsResultFileList[2]?.componentFileUrl != null ? (<img className="component-3" alt="Component" src="/image/png/Component.png" onClick={()=>{window.open(lpvsResult?.lpvsResultFileList[2].componentFileUrl)}} />
            ) : (
              null
            )}
            {lpvsResult?.lpvsResultFileList[1] != undefined && lpvsResult?.lpvsResultFileList[1]?.componentFileUrl != null ? (<img className="component-4" alt="Component" src="/image/png/Component.png" onClick={()=>{window.open(lpvsResult?.lpvsResultFileList[1].componentFileUrl)}} />
            ) : (
              null
            )}
            {lpvsResult?.lpvsResultFileList[0] != undefined && lpvsResult?.lpvsResultFileList[0]?.componentFileUrl != null ? (<img className="component-5" alt="Component" src="/image/png/Component.png" onClick={()=>{window.open(lpvsResult?.lpvsResultFileList[0].componentFileUrl)}} />
            ) : (
              null
            )}
          </div>
          <div className="text-wrapper-36">Detected file list</div>
        </div>
        <div className="detected-license">
          <div className="overlap-4">
          <div style={{ marginTop: '-70px', marginLeft:'-40px' }}>
          {lpvsResult?.count === 0 ? (
            <div style={{ marginTop:'220px', fontSize:'25px', fontWeight:'700', marginLeft:'125px' }}>No License Detected</div>
            ) : (
              <PieChart width={500} height={500}>
                <Pie
                dataKey="value"
                isAnimationActive={false}
                data={data}
                cx="50%"
                cy="50%"
                innerRadius={50}
                outerRadius={100}
                fill="#8884d8"
                >
                {
                data.map((entry, index) => 
                  <Cell key={`cell-${index}`} fill={COLORS[index % COLORS.length]} />)
                }
                </Pie>
                <Tooltip />
              </PieChart>
                )}
          </div>
            <img className="pie-chart-line" alt="Pie chart line" src="/image/svg/PieChartLine.svg" />
            <div className="text-wrapper-37">Detected License ({lpvsResult?.count})</div>
          </div>
          {Object.entries(lpvsResult?.licenseCountMap || {}).map(([license, count]) => {
          const cleanLicenseName = license.replace(/[^a-zA-Z]/g, '');
            if ( cleanLicenseName == 'MIT' || cleanLicenseName == 'Apache' || cleanLicenseName == 'GPLorlater' || cleanLicenseName == 'GPLonly' || cleanLicenseName == 'OpenSSL' ) 
            return (      
              <div className={cleanLicenseName} key={license}>
                <div className={`text-wrapper-${cleanLicenseName}`}>{license}</div>
                <div className={`${cleanLicenseName}-color`}></div>
              </div>
            );
            else return null;
            })}
            <div className="Unsupported-license">
                <div className="text-wrapper-300">Unsupported Licenses</div>
              <div className="Unsupported-license-color" />
            </div>
        </div>
        <div className="info">
          <div className="overlap-5">
            <div className="flexcontainer">
              <p className="text">
                <span className="span">Scan Date:</span>
                <span className="text-wrapper-38">
                  {" "}
                  {formatDate(lpvsResult?.lpvsResultInfo.scanDate)}
                  <br/>
                </span>
              </p>
              <p className="text">
        <span className="span">Detected Licenses: </span>
        {lpvsResult?.lpvsResultInfo?.detectedLicenses?.length === 0 ? (
          <span className="text-wrapper-38">No License Detected</span>
        ) : (
          <span className="text-wrapper-38">
            {lpvsResult?.lpvsResultInfo?.detectedLicenses?.map((license, index) => (
           <span key={index}>
             {license}
             {index !== lpvsResult.lpvsResultInfo.detectedLicenses.length - 1 && ', '}
           </span>
            ))}
          </span>
        )}
            </p>
            </div>
            <img className="info-line" alt="Info line" src="/image/svg/ResultInfoLine.svg" />
            <div className="text-wrapper-39">Info</div>
          </div>
        </div>
        <div className="text-wrapper-42">{lpvsResult?.lpvsResultInfo?.repositoryName}
        <div className="status-2">
          <div className="overlap-6">
          <div className="text-wrapper-41">
            {lpvsResult?.hasIssue
            ? <div className="issue-detected">Issue Detected</div>
            : <div className="scan-complete">Scan Complete</div>}
          </div>
              {lpvsResult?.hasIssue
              ? <div className="issue-detected-label" />
              : <div className="scan-complete-label" />}
          </div>
          <div className="pr">
            <div className="div-wrapper">
             <div className="text-wrapper-40">{lpvsResult?.pullNumber}</div>
            </div>
          </div>
        </div>
        </div>
        <div className="menubar-top">
          <div className="menu-line" />
          <div className="menu">
            <div className="overlap-7">
              <div className="profile-border" />
              <div className="profile">
                <div className="overlap-group-2">
                  <img className="image-2" alt="Image" src="/image/png/ProfileImg.png" />
                  <div className="text-wrapper-44"><Link to={"/user/setting"} style={{ color: "black", textDecoration: "none"}}>{username.name}</Link></div>
                </div>
              </div>
            </div>
            <div className="text-wrapper-46"><Link to={`/history/send/${username?.nickname}?page=0`}  style={{ color: "black", textDecoration: "none"}}>History</Link></div>
          </div>
          <Link to={"/home"} style={{ color: "black", textDecoration: "none"}}>
          <img className="LPVS" alt="img" src="/image/png/LPVS_logo_bar.png" />
            </Link>
        </div>
      </div>
    </div>
  );
};

export default Result;
