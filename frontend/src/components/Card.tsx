import { Card as MuiCard, CardContent, Typography } from '@mui/material';
import React from 'react';
import { CardDto } from '../types';

interface Props {
  card?: CardDto;
  faceDown?: boolean;
}

const suitSymbol: Record<string, string> = {
  HEARTS: '♥',
  DIAMONDS: '♦',
  CLUBS: '♣',
  SPADES: '♠'
};

const suitColor: Record<string, string> = {
  HEARTS: '#e53935',
  DIAMONDS: '#e53935',
  CLUBS: '#1e88e5',
  SPADES: '#1e88e5'
};

export const Card: React.FC<Props> = ({ card, faceDown }) => {
  if (!card || faceDown) {
    return (
      <MuiCard sx={{ width: 64, height: 90, background: 'linear-gradient(135deg, #455a64, #263238)', color: '#eceff1' }}>
        <CardContent sx={{ display: 'flex', alignItems: 'center', justifyContent: 'center', height: '100%' }}>
          <Typography variant="subtitle2">🎴</Typography>
        </CardContent>
      </MuiCard>
    );
  }

  const symbol = suitSymbol[card.suit] || card.suit;
  const color = suitColor[card.suit] || '#1e1e1e';

  return (
    <MuiCard sx={{ width: 64, height: 90, border: '1px solid #cfd8dc' }}>
      <CardContent sx={{ display: 'flex', flexDirection: 'column', height: '100%' }}>
        <Typography variant="subtitle2" sx={{ color }}>
          {card.rank}
        </Typography>
        <Typography variant="h5" component="div" sx={{ flexGrow: 1, display: 'flex', alignItems: 'center', justifyContent: 'center', color }}>
          {symbol}
        </Typography>
        <Typography variant="subtitle2" sx={{ textAlign: 'right', color }}>
          {card.rank}
        </Typography>
      </CardContent>
    </MuiCard>
  );
};
