import {
  Box,
  Button,
  Card,
  CardContent,
  Stack,
  TextField,
  Typography,
  MenuItem,
  Select,
  InputLabel,
  FormControl,
  Alert
} from '@mui/material';
import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useGameContext } from '../context/GameContext';

export const LandingPage: React.FC = () => {
  const navigate = useNavigate();
  const { setBackend, createTable, setTable, addPlayer, setPlayerId, backend } = useGameContext();
  const [backendInput, setBackendInput] = useState<string>(backend);
  const [name, setName] = useState('Hero');
  const [playerType, setPlayerType] = useState<'HUMAN' | 'BOT'>('HUMAN');
  const [tableConfig] = useState({ smallBlind: 10, bigBlind: 20, initialStack: 1000 });
  const [tableIdInput, setTableIdInput] = useState('');
  const [error, setError] = useState<string | null>(null);

  const handleCreateAndJoin = async () => {
    setError(null);
    try {
      const table = await createTable(tableConfig);
      setTable(table);
      const player = await addPlayer(table.id, { name, type: playerType });
      setPlayerId(player.id);
      navigate(`/game/${table.id}`);
    } catch (err: any) {
      setError(err.message);
    }
  };

  const handleJoinExisting = async () => {
    setError(null);
    try {
      const player = await addPlayer(tableIdInput, { name, type: playerType });
      setPlayerId(player.id);
      setTable({
        id: tableIdInput,
        smallBlind: tableConfig.smallBlind,
        bigBlind: tableConfig.bigBlind,
        players: [],
        dealerPosition: 0
      });
      navigate(`/game/${tableIdInput}`);
    } catch (err: any) {
      setError(err.message);
    }
  };

  const applyBackend = () => {
    setBackend(backendInput);
  };

  return (
    <Box maxWidth={640} mx="auto" mt={6}>
      <Card>
        <CardContent>
          <Stack spacing={3}>
            <Typography variant="h4" fontWeight={700} textAlign="center">
              Texas Hold'em Poker
            </Typography>
            {error && <Alert severity="error">{error}</Alert>}
            <TextField label="Backend URL" value={backendInput} onChange={(e) => setBackendInput(e.target.value)} fullWidth />
            <Button variant="outlined" onClick={applyBackend}>
              Use Backend
            </Button>
            <TextField label="Player Name" value={name} onChange={(e) => setName(e.target.value)} fullWidth />
            <FormControl>
              <InputLabel>Player Type</InputLabel>
              <Select value={playerType} label="Player Type" onChange={(e) => setPlayerType(e.target.value as any)}>
                <MenuItem value="HUMAN">Human</MenuItem>
                <MenuItem value="BOT">Bot</MenuItem>
              </Select>
            </FormControl>

            <Stack direction="row" spacing={2}>
              <Button fullWidth variant="contained" onClick={handleCreateAndJoin}>
                Create Table & Join
              </Button>
              <Button fullWidth variant="outlined" onClick={handleJoinExisting} disabled={!tableIdInput}>
                Join Existing
              </Button>
            </Stack>
            <TextField
              label="Existing Table Id"
              placeholder="table-id"
              value={tableIdInput}
              onChange={(e) => setTableIdInput(e.target.value)}
              fullWidth
            />
          </Stack>
        </CardContent>
      </Card>
    </Box>
  );
};
