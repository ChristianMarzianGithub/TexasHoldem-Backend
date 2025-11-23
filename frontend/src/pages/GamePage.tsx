import {
  Alert,
  Box,
  Button,
  CircularProgress,
  Snackbar,
  Stack,
  Typography
} from '@mui/material';
import React, { useEffect, useMemo, useState } from 'react';
import { useParams } from 'react-router-dom';
import { useGameContext } from '../context/GameContext';
import { GameStateDto } from '../types';
import { ActionPanel } from '../components/ActionPanel';
import { TableView } from '../components/TableView';

export const GamePage: React.FC = () => {
  const { tableId } = useParams();
  const {
    playerId,
    gameState,
    setGameState,
    fetchState,
    sendAction,
    triggerBots,
    startHand,
    nextHand
  } = useGameContext();
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [showToast, setShowToast] = useState(false);

  const isPlayersTurn = useMemo(
    () => !!gameState && !!playerId && gameState.currentPlayerId === playerId,
    [gameState, playerId]
  );

  useEffect(() => {
    let interval: ReturnType<typeof setInterval>;
    const performPoll = async () => {
      if (!tableId || !playerId) return;
      try {
        const state = await fetchState(tableId, playerId);
        setGameState(state);
      } catch (err) {
        console.error(err);
      }
    };

    performPoll();
    interval = setInterval(performPoll, 2000);
    return () => clearInterval(interval);
  }, [tableId, playerId, fetchState, setGameState]);

  const handleAction = async (type: 'FOLD' | 'CHECK' | 'CALL' | 'BET' | 'RAISE', amount?: number) => {
    if (!tableId || !playerId) return;
    setLoading(true);
    setError(null);
    try {
      const nextState = await sendAction(tableId, { playerId, action: type, amount });
      setGameState(nextState);
      if (type !== 'FOLD') {
        await triggerBots(tableId);
        const refreshed = await fetchState(tableId, playerId);
        setGameState(refreshed);
      }
    } catch (err: any) {
      setError(err.message);
      setShowToast(true);
    } finally {
      setLoading(false);
    }
  };

  const handleStart = async () => {
    if (!tableId) return;
    setLoading(true);
    try {
      const state = await startHand(tableId);
      setGameState(state);
      if (playerId) {
        const refreshed = await fetchState(tableId, playerId);
        setGameState(refreshed);
      }
    } catch (err: any) {
      setError(err.message);
      setShowToast(true);
    } finally {
      setLoading(false);
    }
  };

  const handleNextHand = async () => {
    if (!tableId) return;
    setLoading(true);
    try {
      const state = await nextHand(tableId);
      setGameState(state);
    } catch (err: any) {
      setError(err.message);
      setShowToast(true);
    } finally {
      setLoading(false);
    }
  };

  const showStartButton = useMemo(() => !gameState || gameState.phase === 'FINISHED', [gameState]);

  return (
    <Box maxWidth={1100} mx="auto" mt={4}>
      <Stack spacing={2}>
        <Typography variant="h4" fontWeight={700}>
          Table {tableId}
        </Typography>
        {error && <Alert severity="error">{error}</Alert>}
        {loading && <CircularProgress />}

        {gameState ? (
          <TableView gameState={gameState} selfPlayerId={playerId} />
        ) : (
          <Alert severity="info">No game state yet. Start a hand to begin.</Alert>
        )}

        <Stack direction="row" spacing={2}>
          {showStartButton && (
            <Button variant="contained" onClick={handleStart} disabled={loading}>
              Start Hand
            </Button>
          )}
          <Button variant="outlined" onClick={handleNextHand} disabled={loading}>
            Next Hand
          </Button>
        </Stack>

        <ActionPanel
          disabled={!isPlayersTurn || loading}
          onFold={() => handleAction('FOLD')}
          onCheck={() => handleAction('CHECK')}
          onCall={() => handleAction('CALL')}
          onBet={(amount) => handleAction('BET', amount)}
          onRaise={(amount) => handleAction('RAISE', amount)}
        />
      </Stack>

      <Snackbar
        open={showToast}
        autoHideDuration={4000}
        onClose={() => setShowToast(false)}
        message={error}
      />
    </Box>
  );
};
