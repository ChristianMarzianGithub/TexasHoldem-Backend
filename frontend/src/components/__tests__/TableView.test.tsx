import { render, screen } from '@testing-library/react';
import React from 'react';
import { TableView } from '../TableView';
import { GameStateDto } from '../../types';

describe('TableView', () => {
  const baseState: GameStateDto = {
    tableId: 'table-1',
    phase: 'FLOP',
    communityCards: [
      { rank: 'A', suit: 'SPADES' },
      { rank: 'K', suit: 'HEARTS' },
      { rank: 'Q', suit: 'CLUBS' }
    ],
    pot: 150,
    currentPlayerId: 'p1',
    players: [
      { id: 'p1', name: 'Hero', stack: 900, bet: 50, folded: false, position: 0, playerType: 'HUMAN' },
      { id: 'p2', name: 'Bot', stack: 1000, bet: 0, folded: false, position: 1, playerType: 'BOT' }
    ]
  };

  it('renders community cards and players', () => {
    render(<TableView gameState={baseState} selfPlayerId="p1" />);
    expect(screen.getByText(/Table table-1/)).toBeInTheDocument();
    expect(screen.getByText(/Hero/)).toBeInTheDocument();
    expect(screen.getByText(/Bot/)).toBeInTheDocument();
    expect(screen.getByText(/Pot: 150/)).toBeInTheDocument();
  });
});
