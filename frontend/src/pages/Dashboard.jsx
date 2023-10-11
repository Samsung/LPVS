/**
 * Copyright 2023 kyudori, Basaeng, hwan5180, quswjdgma83
 *
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */

import React, { useState, useEffect } from "react";
import "../css/Dashboard_style.css";
import { Link, useParams } from "react-router-dom";
import axios from "axios";
import { useNavigate } from "react-router-dom";
import { PieChart, Pie, Sector, Cell, ResponsiveContainer, Rectangle } from 'recharts';
import {BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, Legend, LineChart, Line } from "recharts";

export const Dashboard = () => {

    const navigate = useNavigate();
    const { type, name } = useParams();

    const [ isLoggedIn, setIsLoggedIn ] = useState(false);
    const [username, setUsername] = useState("");

    const [dashBoardInfo, setDashBoardInfo] = useState();

    useEffect(() => {
        axios
            .get(`/dashboard/${type}/${name}`)
            .then((response) => {
                console.log(response.data);
                setDashBoardInfo(response.data);
            })
            .catch((error) => {
                console.log(error);

            });
    }, [type, name, username]);

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

    const navigateToOrg = () => {
        if (!username?.organization || username?.organization.trim() === "") {
            window.alert("Please enter the Organization information on the User page.");
        } else {
            navigate(`/dashboard/org/${username?.organization}`);
        }
    };
    const navigateToOwn = () => {
        if (!username?.nickname || username?.nickname.trim() === "") {
            window.alert("Please enter the Organization information on the User page.");
            navigate('/user/info')
        } else {
            navigate(`/dashboard/own/${username?.nickname}`);
        }
    };
    const navigateToSend = () => {
        if (!username?.nickname || username?.nickname.trim() === "") {
            window.alert("Please enter the Organization information on the User page.");
            navigate('/user/info')
        } else {
            navigate(`/dashboard/send/${username?.nickname}`);
        }
    };

    const add_Licenses =()=> {
        let a =
            dashBoardInfo?.licenseCountMap['GPL-2.0-or-later']+
            dashBoardInfo?.licenseCountMap.OpenSSL+
            dashBoardInfo?.licenseCountMap['GPL-3.0-only']+
            dashBoardInfo?.licenseCountMap['Apache-2.0']+
            dashBoardInfo?.licenseCountMap?.MIT
        return a;
    }
//=============================PullRequests====================

    const PRdataMaps = dashBoardInfo?.dashboardElementsByDates?.map(function(element){
        return element.date
    })

    const PRsMaps = dashBoardInfo?.dashboardElementsByDates?.map(function(element){
        return element.pullRequestCount
    })

    const PR_data=[];



    for (var i=0; i < PRdataMaps?.length; i++) {
        PR_data.push({'name': PRdataMaps[i], 'Pull Requests': PRsMaps[i], 'amt': 2100});
    }

    console.log()
    console.log(PR_data)

    PR_data.sort((a, b) =>
        a.name.localeCompare(b.name));

//==========================Participants============================

    const PC_dataMaps = dashBoardInfo?.dashboardElementsByDates?.map(function(element){
        return element.date
    })

    const PC_Maps = dashBoardInfo?.dashboardElementsByDates?.map(function(element){
        return element.participantCount
    })

    const PC_data=[];

    for (var i=0; i < PC_dataMaps?.length; i++) {
        PC_data.push({'name': PC_dataMaps[i], 'Participants': PC_Maps[i], 'amt': 2100});
    }

    PC_data.sort((a, b) =>
        a.name.localeCompare(b.name));
//=================License Usage===========================================

    const LU_dataMaps = dashBoardInfo?.dashboardElementsByDates?.map(function(element){
        return element.date
    })

    const high_Maps = dashBoardInfo?.dashboardElementsByDates?.map(function(element){
        return element.riskGradeMap.HIGH
    })

    const medium_Maps = dashBoardInfo?.dashboardElementsByDates?.map(function(element){
        return element.riskGradeMap.MIDDLE
    })

    const low_Maps = dashBoardInfo?.dashboardElementsByDates?.map(function(element){
        return element.riskGradeMap.LOW
    })

    const LU_data=[];

    for (var i=0; i < LU_dataMaps?.length; i++) {
        LU_data.push({'name': LU_dataMaps[i], 'high': high_Maps[i], 'medium': medium_Maps[i], 'low': low_Maps[i]});
    }

    LU_data.sort((a, b) =>
        a.name.localeCompare(b.name));
//===================Pie Chart=========================================================
    const COLORS = ['#F28300', '#a31f34', '#7f237d', '#000000', '#73c541', '#007c31'];

    const data = [
        { name: "Unsupported License", value: dashBoardInfo?.totalDetectionCount - add_Licenses()},
        { name: "MIT", value: dashBoardInfo?.licenseCountMap?.MIT },
        { name: "Apache 2.0", value: dashBoardInfo?.licenseCountMap['Apache-2.0'] },
        { name: "GNU GP", value: dashBoardInfo?.licenseCountMap['GPL-3.0-only'] },
        { name: "Open SSL", value: dashBoardInfo?.licenseCountMap.OpenSSL },
        { name: "GNU LG", value: dashBoardInfo?.licenseCountMap['GPL-2.0-or-later'] },
    ];
//===================Truncate Name=========================================================
    function truncateName(name) {
        if (/[\u3131-\u314e\u314f-\u3163\uac00-\ud7a3]/g.test(name)) {
            return name.length > 3 ? `${name.substring(0, 3)}.` : name;
        } else {
            return name.length > 5 ? `${name.substring(0, 5)}.` : name;
        }
    }

    return (
        <div className="dashboard-send">
            <div className="div">
                <div className="dash-board">
                    <div className="charts">
                        <div className="usage-chart">
                            <div className="div-2">
                                <div className="text-wrapper">License Usage</div>
                                <div className="div-4" />
                            </div>
                            <div style={{ marginTop: '120px' }}>
                                <LineChart
                                    width={850}
                                    height={300}
                                    data={LU_data}
                                    margin={{
                                        top: 5,
                                        right: 40,
                                        left: 30,
                                        bottom: 5,
                                    }}
                                >
                                    <CartesianGrid strokeDasharray="3 3" />
                                    <XAxis dataKey="name" />
                                    <YAxis />
                                    <Tooltip />
                                    <Legend />
                                    <Line type="monotone" dataKey="high" stroke="#FF0000" activeDot={{ r: 5 }} />
                                    <Line type="monotone" dataKey="medium" stroke="#F4D600" />
                                    <Line type="monotone" dataKey="low" stroke="#00FF19" />
                                </LineChart>
                            </div>

                        </div>
                        <div className="pie-chart">
                            <div className="overlap">
                                <div className="div-3" />
                                <div className="status">


                                    {Object.entries(dashBoardInfo?.licenseCountMap || {}).map(([license, count]) => {

                                        const cleanLicenseName = license.replace(/[^a-zA-Z]/g, '');
                                        if ( cleanLicenseName == 'MIT' || cleanLicenseName == 'Apache' || cleanLicenseName == 'GPLorlater' || cleanLicenseName == 'GPLonly' || cleanLicenseName == 'OpenSSL' )
                                            return (
                                                <div className={cleanLicenseName} key={license}>
                                                    <div className="text-wrapper-2">{count}</div>
                                                    <div className={`text-wrapper-${cleanLicenseName}`}>{license}</div>
                                                    <div className={`${cleanLicenseName}-color`}></div>
                                                </div>
                                            );
                                        else return null;
                                    })}
                                    <div className="Unsupported-license">
                                        <div className="text-wrapper-2">{dashBoardInfo?.totalDetectionCount-add_Licenses()}</div>
                                        <div className="text-wrapper-300">{dashBoardInfo?.totalDetectionCount == 0 ? (
                                            <>Unsupported Licenses</>
                                        ) : (
                                        null
                                        )}</div>
                                        <div className="Unsupported-license-color" />
                                    </div>
                                    <div className="total-header">
                                        <div className="total-line" />
                                        <div className="text-wrapper-8">Total ({dashBoardInfo?.totalDetectionCount})</div>
                                    </div>
                                </div>
                                <PieChart style={{zIndex:1000}} width={500} height={500}>
                                    <Pie
                                        dataKey="value"
                                        isAnimationActive={false}
                                        data={data}
                                        cx="50%"
                                        cy="50%"
                                        innerRadius={70}
                                        outerRadius={130}
                                        fill="#8884d8"
                                        startAngle={180}
                                        endAngle={540}
                                    >
                                        {
                                            data.map((entry, index) =>
                                                <Cell key={`cell-${index}`} fill={COLORS[index % COLORS.length]} />)
                                        }
                                    </Pie>
                                    <Tooltip />
                                </PieChart>



                                <div className="div-2">
                                    <div className="overlap-group">
                                        <div className="text-wrapper-10">Detected Licenses</div>
                                        <div className="div-4" />
                                    </div>
                                </div>
                            </div>
                        </div>
                        <div className="PR-chart">
                            <div className="numbers-of-prs">
                                <div className="text-wrapper">Pull Requests</div>
                                <div className="div-4" />
                            </div>
                            <div style={{ marginTop: '110px' }}>
                                <BarChart
                                    width={826}
                                    height={297}
                                    data={PR_data}
                                    margin={{
                                        top: 5,
                                        right: 30,
                                        left: 20,
                                        bottom: 5
                                    }}
                                >
                                    <CartesianGrid strokeDasharray="3 3" />
                                    <XAxis dataKey="name" />
                                    <YAxis />
                                    <Tooltip />
                                    <Legend />
                                    <Bar dataKey="Pull Requests" fill="#FF8BD1" barSize={20}/>
                                </BarChart>
                            </div>

                        </div>

                        <div className="div-3">
                            <div className="number-of">
                                <div className="text-wrapper-11">Participants</div>
                                <div className="div-4" />
                            </div>
                            <div style={{ marginTop: '-280px' }}>
                                <BarChart
                                    width={826}
                                    height={297}
                                    data={PC_data}
                                    margin={{
                                        top: 5,
                                        right: 30,
                                        left: 20,
                                        bottom: 5
                                    }}
                                >
                                    <CartesianGrid strokeDasharray="3 3" />
                                    <XAxis dataKey="name" />
                                    <YAxis />
                                    <Tooltip />
                                    <Legend />
                                    <Bar dataKey="Participants" fill="#73C541" barSize={20}/>
                                </BarChart>
                            </div>

                        </div>
                    </div>
                    <div className="dashboard-data">
                        <div className="repository">
                            <div className="repo-number">
                                <div className="div-wrapper">
                                    <div className="text-wrapper-12">{dashBoardInfo?.totalRepositoryCount}</div>
                                </div>
                            </div>
                            <div className="repo">
                                <div className="div-wrapper">
                                    <div className="text-wrapper-13">Repository</div>
                                </div>
                            </div>
                        </div>
                        <div className="participants">
                            <div className="participants-number">
                                <div className="overlap-group-2">
                                    <div className="text-wrapper-14">{dashBoardInfo?.totalParticipantsCount}</div>
                                </div>
                            </div>
                            <div className="overlap-wrapper">
                                <div className="overlap-group-2">
                                    <div className="text-wrapper-15">Participants</div>
                                </div>
                            </div>
                        </div>
                        <div className="issues">
                            <div className="overlap-group-wrapper">
                                <div className="overlap-group-3">
                                    <div className="text-wrapper-16">{dashBoardInfo?.totalIssueCount}</div>
                                </div>
                            </div>
                            <div className="overlap-wrapper-2">
                                <div className="overlap-group-3">
                                    <div className="text-wrapper-17">Issues</div>
                                </div>
                            </div>
                        </div>
                        <div className="total-detection">
                            <div className="overlap-group-wrapper">
                                <div className="overlap-group-4">
                                    <div className="text-wrapper-18">{dashBoardInfo?.totalDetectionCount}</div>
                                </div>
                            </div>
                            <div className="overlap-wrapper-2">
                                <div className="overlap-group-4">
                                    <div className="text-wrapper-19">Detection</div>
                                </div>
                            </div>
                        </div>
                        <div className="serious-license">
                            <div className="overlap-group-wrapper">
                                <div className="overlap-group-5">
                                    <div className="text-wrapper-20">{dashBoardInfo?.highSimilarityCount}</div>
                                </div>
                            </div>
                            <div className="overlap-wrapper-2">
                                <div className="overlap-group-5">
                                    <div className="text-wrapper-21">High Similarity</div>
                                </div>
                            </div>
                        </div>
                    </div>
                    <div className="overlap-2">

                        <div className="date-end">
                            <div className="overlap-4">
                                <div className="text-wrapper-22">{PR_data[PR_data.length-1]?.name}</div>
                            </div>
                        </div>

                        <div style={{fontSize: '35px', marginTop:'5px', fontWeight: '500', marginLeft: '150px'}}>
                            Date
                        </div>


                        <p className="wave" style={{marginTop:'-18px'}}>~</p>
                        <div className="date-start">
                            <div className="overlap-4">
                                <div className="text-wrapper-23">{PR_data[0]?.name}</div>
                            </div>
                        </div>

                    </div>

                    <div className="my-option">
                        <div className="overlap-5">
                            <div className="org-button">
                                <div className="overlap-group-6">
                                    {type === "org" ? <div
                                            className="OptionButton-pr" onClick=
                                            {navigateToOrg} style={{ cursor:
                                                "pointer"}}>My Org</div>:
                                        <div className="OptionButton" onClick={navigateToOrg} style={{ cursor: "pointer"}}>My Org</div>}
                                </div>
                            </div>
                            <div className="repo-button">
                                <div className="overlap-group-6">
                                    {type === "own" ? <div
                                            className="OptionButton-pr" onClick=
                                            {navigateToOwn} style={{ cursor:
                                                "pointer"}}>My Repo</div>:
                                        <div className="OptionButton" onClick={navigateToOwn} style={{ cursor: "pointer"}}>My Repo</div>}
                                </div>
                            </div>
                            <div className="sender-button">
                                <div className="overlap-6">
                                    {type === "send" ? <div
                                            className='OptionButton-pr' onClick=
                                            {navigateToSend} style={{ cursor:
                                                "pointer" }}>My PR</div>:
                                        <div className='OptionButton' onClick={navigateToSend} style={{ cursor: "pointer" }}>My PR</div>}
                                </div>
                            </div>
                            <div className="my-option-header">
                                <img className="my-option-line" alt="My option line" src="/image/svg/DashMyOptionLine.svg" />
                                <div className="text-wrapper-26">My Option</div>
                            </div>
                        </div>
                    </div>
                </div>
                <div className="overlap-7">
                    <div className="menubar-top">
                        <div className="menu-line" />
                        <div className="menu">
                            <div className="overlap-8">
                                <div className="profile-border" />
                                <div className="profile">
                                    <div className="overlap-group-7">
                                        <img className="image-3" alt="Image" src="/image/png/ProfileImg.png" />
                                        <div className="text-wrapper-28"><Link to={"/user/info"} style={{ color: "black", textDecoration: "none"}}>{username?.name ? (
                                            <div>{truncateName(username.name)}</div>
                                        ) : (
                                            <div>Loading...</div>
                                        )}</Link></div>
                                    </div>
                                </div>
                            </div>
                            <div className="text-wrapper-27"><Link to={`/dashboard/send/${username?.nickname}`} style={{ color: "black", textDecoration: "none"}}>Dashboard</Link></div>
                            <div className="text-wrapper-29"><Link to={`/history/send/${username?.nickname}?page=0`} style={{ color: "black", textDecoration: "none"}}>History</Link></div>
                        </div>
                    </div>
                    <Link to={"/home"} style={{ color: "black", textDecoration: "none"}}>
                        <img className="LPVS" alt="Lpvs" src="/image/png/LPVS_logo_bar.png" />
                    </Link>
                </div>
            </div>
        </div>
    );
};

export default Dashboard;
