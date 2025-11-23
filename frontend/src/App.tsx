import React from 'react';
import { BrowserRouter, Route, Routes } from 'react-router-dom';
import { GameProvider } from './context/GameContext';
import { GamePage } from './pages/GamePage';
import { LandingPage } from './pages/LandingPage';
import { CssBaseline, ThemeProvider, createTheme } from '@mui/material';

const theme = createTheme({
  palette: {
    mode: 'light',
    primary: { main: '#1e88e5' },
    secondary: { main: '#8e24aa' }
  },
  typography: {
    fontFamily: 'Inter, system-ui, -apple-system, BlinkMacSystemFont, "Segoe UI", sans-serif'
  }
});

export const App: React.FC = () => (
  <ThemeProvider theme={theme}>
    <CssBaseline />
    <GameProvider>
      <BrowserRouter>
        <Routes>
          <Route path="/" element={<LandingPage />} />
          <Route path="/game/:tableId" element={<GamePage />} />
        </Routes>
      </BrowserRouter>
    </GameProvider>
  </ThemeProvider>
);
