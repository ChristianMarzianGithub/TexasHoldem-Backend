import { Avatar, Box, Chip, Paper, Stack, Typography } from '@mui/material';
import React from 'react';
import { PlayerViewDto } from '../types';
import { Card } from './Card';

interface Props {
  player: PlayerViewDto;
  isCurrent: boolean;
  isSelf: boolean;
}

export const PlayerSeat: React.FC<Props> = ({ player, isCurrent, isSelf }) => {
  return (
    <Paper elevation={isCurrent ? 4 : 1} sx={{ p: 2, minWidth: 200, border: isCurrent ? '2px solid #4caf50' : '1px solid #eceff1' }}>
      <Stack direction="row" spacing={2} alignItems="center">
        <Avatar>{player.name.charAt(0).toUpperCase()}</Avatar>
        <Box sx={{ flexGrow: 1 }}>
          <Typography variant="subtitle1" fontWeight={600}>
            {player.name} {isSelf && '(You)'}
          </Typography>
          <Typography variant="body2" color="text.secondary">
            Stack: {player.stack} | Bet: {player.bet}
          </Typography>
          {player.folded && <Chip label="Folded" size="small" color="default" sx={{ mt: 0.5 }} />}
        </Box>
        {player.holeCards && player.holeCards.length > 0 && (
          <Stack direction="row" spacing={1}>
            {player.holeCards.map((card, idx) => (
              <Card key={idx} card={card} />
            ))}
          </Stack>
        )}
      </Stack>
    </Paper>
  );
};
