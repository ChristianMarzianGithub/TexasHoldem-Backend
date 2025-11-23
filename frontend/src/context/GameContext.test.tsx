import { renderHook, act } from '@testing-library/react';
import React from 'react';
import { GameProvider, useGameContext } from './GameContext';
import type { AddPlayerRequest, CreateTableRequest } from '../types';
import { vi } from 'vitest';

const mockCreateTable = vi.fn();
const mockAddPlayer = vi.fn();
const mockFetchState = vi.fn();
const mockSendAction = vi.fn();
const mockStartHand = vi.fn();
const mockTriggerBots = vi.fn();
const mockNextHand = vi.fn();
const mockBackendUrl = vi.fn(() => 'http://default-backend');

vi.mock('../api/pokerApi', () => ({
  backendUrl: mockBackendUrl,
  createTable: (req: CreateTableRequest, backend: string) => mockCreateTable(req, backend),
  addPlayer: (tableId: string, req: AddPlayerRequest, backend: string) => mockAddPlayer(tableId, req, backend),
  fetchState: (tableId: string, playerId: string, backend: string) => mockFetchState(tableId, playerId, backend),
  sendAction: (tableId: string, payload: any, backend: string) => mockSendAction(tableId, payload, backend),
  startHand: (tableId: string, backend: string) => mockStartHand(tableId, backend),
  triggerBots: (tableId: string, backend: string) => mockTriggerBots(tableId, backend),
  nextHand: (tableId: string, backend: string) => mockNextHand(tableId, backend)
}));

describe('GameContext', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('uses the default backend from configuration for API calls', async () => {
    const wrapper: React.FC<{ children: React.ReactNode }> = ({ children }) => (
      <GameProvider>{children}</GameProvider>
    );

    const { result } = renderHook(() => useGameContext(), { wrapper });

    await act(async () => {
      await result.current.createTable({ smallBlind: 10, bigBlind: 20, initialStack: 1000 });
    });

    await act(async () => {
      await result.current.fetchState('table-1', 'player-1');
    });

    expect(mockCreateTable).toHaveBeenCalledWith(
      { smallBlind: 10, bigBlind: 20, initialStack: 1000 },
      'http://default-backend'
    );
    expect(mockFetchState).toHaveBeenCalledWith('table-1', 'player-1', 'http://default-backend');
  });

  it('updates backend base when setBackend is invoked', async () => {
    const wrapper: React.FC<{ children: React.ReactNode }> = ({ children }) => (
      <GameProvider>{children}</GameProvider>
    );

    const { result } = renderHook(() => useGameContext(), { wrapper });

    act(() => {
      result.current.setBackend('http://custom');
    });

    await act(async () => {
      await result.current.addPlayer('table-1', { name: 'Bot', type: 'BOT' });
    });

    expect(mockAddPlayer).toHaveBeenCalledWith('table-1', { name: 'Bot', type: 'BOT' }, 'http://custom');
  });

  it('throws when the hook is used outside of the provider', () => {
    const renderWithoutProvider = () => renderHook(() => useGameContext());
    expect(renderWithoutProvider).toThrow('useGameContext must be used inside GameProvider');
  });
});
