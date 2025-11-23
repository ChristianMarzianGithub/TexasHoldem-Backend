import { fireEvent, render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import React from 'react';
import { LandingPage } from './LandingPage';
import { vi } from 'vitest';

const mockCreateTable = vi.fn();
const mockAddPlayer = vi.fn();
const mockSetTable = vi.fn();
const mockSetPlayerId = vi.fn();
const mockSetBackend = vi.fn();
const mockNavigate = vi.fn();

  vi.mock('../context/GameContext', () => ({
    useGameContext: () => ({
      backend: 'http://localhost:8080',
      setBackend: mockSetBackend,
      createTable: (req: any) => mockCreateTable(req),
      setTable: mockSetTable,
      addPlayer: (tableId: string, req: any) => mockAddPlayer(tableId, req),
    setPlayerId: mockSetPlayerId,
    playerId: null,
    table: null,
    gameState: null,
    setGameState: vi.fn(),
    triggerBots: vi.fn(),
    sendAction: vi.fn(),
    startHand: vi.fn(),
    fetchState: vi.fn(),
    nextHand: vi.fn()
  })
}));

vi.mock('react-router-dom', async () => {
  const actual = await vi.importActual<typeof import('react-router-dom')>('react-router-dom');
  return { ...actual, useNavigate: () => mockNavigate, useParams: () => ({}) };
});

describe('LandingPage', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('creates a table then joins and navigates', async () => {
    mockCreateTable.mockResolvedValue({
      id: 'table-123',
      smallBlind: 10,
      bigBlind: 20,
      players: [],
      dealerPosition: 0,
      initialStack: 1000
    });
    mockAddPlayer.mockResolvedValue({ id: 'player-1' });

    render(<LandingPage />);

    await userEvent.click(screen.getByRole('button', { name: /create table & join/i }));

    await waitFor(() => expect(mockCreateTable).toHaveBeenCalled());
    expect(mockAddPlayer).toHaveBeenCalledWith('table-123', { name: 'Hero', type: 'HUMAN' });
    expect(mockSetTable).toHaveBeenCalled();
    expect(mockSetPlayerId).toHaveBeenCalledWith('player-1');
    expect(mockNavigate).toHaveBeenCalledWith('/game/table-123');
  });

  it('disables join button until a table id is provided and applies backend changes', async () => {
    render(<LandingPage />);

    const joinButton = screen.getByRole('button', { name: /join existing/i });
    expect(joinButton).toBeDisabled();

    const tableIdInput = screen.getByLabelText(/existing table id/i);
    await userEvent.type(tableIdInput, 'table-999');
    expect(joinButton).toBeEnabled();

    const backendInput = screen.getByLabelText(/backend url/i);
    fireEvent.change(backendInput, { target: { value: 'http://api.example.com' } });
    await userEvent.click(screen.getByRole('button', { name: /use backend/i }));

    expect(mockSetBackend).toHaveBeenCalledWith('http://api.example.com');
  });
});
