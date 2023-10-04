/**
 * Copyright 2023 kyudori, Basaeng, hwan5180, quswjdgma83
 *
 * Use of this source code is governed by a MIT license that can be
 * found in the LICENSE file.
 */

import React from 'react';
import { BrowserRouter, Routes, Route } from "react-router-dom";
import Home from './pages/Home';
import Login from './pages/Login';
import User from './pages/User';
import History from './pages/History';

function App() {
  return (
    <BrowserRouter>
      <div className='App'>
        <Routes>
          <Route path="/" element={<Home />} />
          <Route path="/home" element={<Home />} />
          <Route path="/login" element={<Login />} />
          <Route path="/user/setting" element={<User />} />
          <Route path="/history/:type/:name" element={<History />} />
        </Routes>
      </div>
    </BrowserRouter>
  );
}

export default App;
