import { AddPlayerRequest, CreateTableRequest, GameStateDto, PlayerActionRequest, TableDto } from '../types';

const defaultBackend = import.meta.env.VITE_BACKEND_URL || 'http://localhost:8080';

async function handleResponse<T>(response: Response): Promise<T> {
  if (!response.ok) {
    let message = 'Request failed';
    try {
      const data = await response.json();
      message = data.message || JSON.stringify(data);
    } catch (err) {
      message = response.statusText;
    }
    throw new Error(message);
  }
  return response.json() as Promise<T>;
}

export function backendUrl(custom?: string) {
  return custom && custom.length > 0 ? custom : defaultBackend;
}

export async function createTable(request: CreateTableRequest, baseUrl?: string): Promise<TableDto> {
  const response = await fetch(`${backendUrl(baseUrl)}/api/tables`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(request)
  });
  return handleResponse<TableDto>(response);
}

export async function addPlayer(tableId: string, request: AddPlayerRequest, baseUrl?: string): Promise<any> {
  const response = await fetch(`${backendUrl(baseUrl)}/api/tables/${tableId}/players`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(request)
  });
  return handleResponse(response);
}

export async function startHand(tableId: string, baseUrl?: string): Promise<GameStateDto> {
  const response = await fetch(`${backendUrl(baseUrl)}/api/tables/${tableId}/start`, { method: 'POST' });
  return handleResponse<GameStateDto>(response);
}

export async function fetchState(tableId: string, playerId: string, baseUrl?: string): Promise<GameStateDto> {
  const response = await fetch(`${backendUrl(baseUrl)}/api/tables/${tableId}/state?playerId=${playerId}`);
  return handleResponse<GameStateDto>(response);
}

export async function sendAction(tableId: string, payload: PlayerActionRequest, baseUrl?: string): Promise<GameStateDto> {
  const response = await fetch(`${backendUrl(baseUrl)}/api/tables/${tableId}/action`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(payload)
  });
  return handleResponse<GameStateDto>(response);
}

export async function triggerBots(tableId: string, baseUrl?: string): Promise<GameStateDto> {
  const response = await fetch(`${backendUrl(baseUrl)}/api/tables/${tableId}/bots/act`, { method: 'POST' });
  return handleResponse<GameStateDto>(response);
}

export async function nextHand(tableId: string, baseUrl?: string): Promise<GameStateDto> {
  const response = await fetch(`${backendUrl(baseUrl)}/api/tables/${tableId}/next-hand`, { method: 'POST' });
  return handleResponse<GameStateDto>(response);
}
