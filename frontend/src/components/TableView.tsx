import { Box, Chip, Divider, Grid, Paper, Stack, Typography } from '@mui/material';
import React from 'react';
import { GameStateDto, PlayerViewDto } from '../types';
import { Card } from './Card';
import { PlayerSeat } from './PlayerSeat';

interface Props {
  gameState: GameStateDto;
  selfPlayerId: string | null;
}

const seatPositions = [
  { gridProps: { xs: 12, md: 6 }, align: 'flex-start' },
  { gridProps: { xs: 12, md: 6 }, align: 'flex-end' },
  { gridProps: { xs: 12, md: 6 }, align: 'flex-start' },
  { gridProps: { xs: 12, md: 6 }, align: 'flex-end' },
  { gridProps: { xs: 12 }, align: 'center' }
];

export const TableView: React.FC<Props> = ({ gameState, selfPlayerId }) => {
  const sortedPlayers = [...gameState.players].sort((a, b) => a.position - b.position);

  const renderCommunityCards = () => (
    <Stack direction="row" spacing={1} justifyContent="center" alignItems="center" sx={{ my: 3 }}>
      {['Slot1', 'Slot2', 'Slot3', 'Slot4', 'Slot5'].map((_, idx) => (
        <Card key={idx} card={gameState.communityCards[idx]} faceDown={!gameState.communityCards[idx]} />
      ))}
    </Stack>
  );

  return (
    <Paper elevation={2} sx={{ p: 3, background: 'linear-gradient(180deg, #f5f7fb 0%, #ffffff 100%)' }}>
      <Stack spacing={2}>
        <Box display="flex" justifyContent="space-between" alignItems="center">
          <Typography variant="h5" fontWeight={700}>
            Table {gameState.tableId}
          </Typography>
          <Stack direction="row" spacing={2} alignItems="center">
            <Chip color="primary" label={`Phase: ${gameState.phase}`} />
            <Chip color="secondary" label={`Pot: ${gameState.pot}`} />
            {gameState.currentPlayerId && <Chip label={`Turn: ${gameState.currentPlayerId}`} />}
          </Stack>
        </Box>

        <Divider />

        {renderCommunityCards()}

        <Grid container spacing={2}>
          {sortedPlayers.map((player: PlayerViewDto, idx: number) => (
            <Grid key={player.id} item {...seatPositions[idx % seatPositions.length].gridProps}>
              <Box display="flex" justifyContent={seatPositions[idx % seatPositions.length].align}>
                <PlayerSeat
                  player={player}
                  isCurrent={gameState.currentPlayerId === player.id}
                  isSelf={player.id === selfPlayerId}
                />
              </Box>
            </Grid>
          ))}
        </Grid>

        {gameState.winners && gameState.winners.length > 0 && (
          <Chip color="success" label={`Winner: ${gameState.winners.join(', ')}`} />
        )}
      </Stack>
    </Paper>
  );
};
