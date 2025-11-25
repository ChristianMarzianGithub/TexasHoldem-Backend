from app import api_action, api_new_game


def test_new_game_starts_with_chips_and_blinds():
    data = api_new_game()
    assert data['player']['chips'] == 990  # small blind posted
    assert data['bot']['chips'] == 980  # big blind posted
    assert data['pot'] == 30
    assert data['stage'] == 'preflop'


def test_action_flow_to_flop_and_error_on_bad_check():
    new_game = api_new_game()
    session_id = new_game['sessionId']

    _, status, error = api_action(session_id, 'check')
    assert status == 400
    assert 'Cannot check' in error

    call_data, status, error = api_action(session_id, 'call')
    assert status == 200
    assert call_data['stage'] in {'flop', 'turn', 'river', 'showdown'}


def test_unknown_session_returns_404():
    _, status, error = api_action('missing', 'check')
    assert status == 404
    assert error == 'Session not found'
