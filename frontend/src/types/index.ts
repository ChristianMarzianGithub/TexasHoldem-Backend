export type GamePhase = 'PRE_FLOP' | 'FLOP' | 'TURN' | 'RIVER' | 'SHOWDOWN' | 'FINISHED';

export interface CardDto {
  rank: string;
  suit: string;
}

export interface PlayerViewDto {
  id: string;
  name: string;
  stack: number;
  bet: number;
  folded: boolean;
  position: number;
  playerType: 'HUMAN' | 'BOT';
  ownerUserId?: string;
  holeCards?: CardDto[];
}

export interface GameStateDto {
  tableId: string;
  phase: GamePhase;
  communityCards: CardDto[];
  pot: number;
  currentPlayerId: string | null;
  players: PlayerViewDto[];
  lastAction?: string;
  winners?: string[];
}

export interface TableDto {
  id: string;
  smallBlind: number;
  bigBlind: number;
  players: PlayerViewDto[];
  dealerPosition: number;
}

export interface CreateTableRequest {
  smallBlind: number;
  bigBlind: number;
  initialStack: number;
}

export interface AddPlayerRequest {
  name: string;
  type: 'HUMAN' | 'BOT';
  userId?: string;
}

export interface PlayerActionRequest {
  playerId: string;
  action: 'FOLD' | 'CHECK' | 'CALL' | 'BET' | 'RAISE';
  amount?: number;
}
