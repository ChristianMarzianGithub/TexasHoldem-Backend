let sessionId = null;
const playerHandEl = document.getElementById('player-hand');
const communityEl = document.getElementById('community-cards');
const messageEl = document.getElementById('message');
const playerChipsEl = document.getElementById('player-chips');
const botChipsEl = document.getElementById('bot-chips');
const potEl = document.getElementById('pot');
const currentBetEl = document.getElementById('current-bet');
const raiseInput = document.getElementById('raise-amount');

async function startGame() {
  const res = await fetch('/api/new-game', { method: 'POST' });
  const data = await res.json();
  sessionId = data.sessionId;
  renderState(data);
}

function renderCards(el, cards) {
  el.innerHTML = cards.map((c) => `<div class="card">${c}</div>`).join('');
}

function renderState(state) {
  playerChipsEl.textContent = state.player.chips;
  botChipsEl.textContent = state.bot.chips;
  potEl.textContent = state.pot;
  currentBetEl.textContent = state.currentBet;
  messageEl.textContent = state.message + (state.winner ? ` (${state.winner})` : '');
  renderCards(playerHandEl, state.player.hand);
  renderCards(communityEl, state.community);

  const disable = state.handOver && !['preflop', 'flop', 'turn', 'river'].includes(state.stage);
  document.querySelectorAll('.action, #raise-btn').forEach((btn) => {
    btn.disabled = disable || !sessionId;
  });
}

async function sendAction(action, amount = 0) {
  if (!sessionId) return;
  const res = await fetch('/api/action', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ sessionId, action, amount }),
  });
  const data = await res.json();
  if (data.error) {
    messageEl.textContent = data.error;
    return;
  }
  renderState(data);
}

function bindActions() {
  document.querySelectorAll('.action').forEach((btn) => {
    btn.addEventListener('click', () => sendAction(btn.dataset.action));
  });
  document.getElementById('raise-btn').addEventListener('click', () => {
    const amt = parseInt(raiseInput.value, 10) || 0;
    sendAction('raise', amt);
  });
  document.getElementById('new-game').addEventListener('click', startGame);
}

bindActions();
