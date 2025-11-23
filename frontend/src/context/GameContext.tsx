import React, { createContext, useContext, useEffect, useMemo, useState } from 'react';
import {
  addPlayer,
  backendUrl,
  createTable,
  fetchState,
  nextHand,
  sendAction,
  startHand,
  triggerBots
} from '../api/pokerApi';
import { AddPlayerRequest, CreateTableRequest, GameStateDto, PlayerActionRequest, TableDto } from '../types';

interface GameContextValue {
  backend: string;
  setBackend: (url: string) => void;
  table: TableDto | null;
  setTable: (table: TableDto | null) => void;
  playerId: string | null;
  setPlayerId: (id: string | null) => void;
  gameState: GameStateDto | null;
  setGameState: (state: GameStateDto | null) => void;
  createTable: (request: CreateTableRequest) => Promise<TableDto>;
  addPlayer: (tableId: string, request: AddPlayerRequest) => Promise<any>;
  startHand: (tableId: string) => Promise<GameStateDto>;
  fetchState: (tableId: string, playerId: string) => Promise<GameStateDto>;
  sendAction: (tableId: string, payload: PlayerActionRequest) => Promise<GameStateDto>;
  triggerBots: (tableId: string) => Promise<GameStateDto>;
  nextHand: (tableId: string) => Promise<GameStateDto>;
}

const GameContext = createContext<GameContextValue | undefined>(undefined);

export const GameProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [backendBase, setBackendBase] = useState<string>(() => backendUrl());
  const [table, setTable] = useState<TableDto | null>(null);
  const [playerId, setPlayerId] = useState<string | null>(null);
  const [gameState, setGameState] = useState<GameStateDto | null>(null);

  useEffect(() => {
    setBackendBase((prev) => prev || backendUrl());
  }, []);

  const value = useMemo<GameContextValue>(() => ({
    backend: backendBase,
    setBackend: setBackendBase,
    table,
    setTable,
    playerId,
    setPlayerId,
    gameState,
    setGameState,
    createTable: (request: CreateTableRequest) => createTable(request, backendBase),
    addPlayer: (tableId: string, request: AddPlayerRequest) => addPlayer(tableId, request, backendBase),
    startHand: (tableId: string) => startHand(tableId, backendBase),
    fetchState: (tableId: string, playerId: string) => fetchState(tableId, playerId, backendBase),
    sendAction: (tableId: string, payload: PlayerActionRequest) => sendAction(tableId, payload, backendBase),
    triggerBots: (tableId: string) => triggerBots(tableId, backendBase),
    nextHand: (tableId: string) => nextHand(tableId, backendBase)
  }), [backendBase, gameState, playerId, table]);

  return <GameContext.Provider value={value}>{children}</GameContext.Provider>;
};

export function useGameContext() {
  const ctx = useContext(GameContext);
  if (!ctx) {
    throw new Error('useGameContext must be used inside GameProvider');
  }
  return ctx;
}
