import { Box, Button, Stack, TextField } from '@mui/material';
import React, { useState } from 'react';

interface Props {
  disabled: boolean;
  onFold: () => void;
  onCheck: () => void;
  onCall: () => void;
  onBet: (amount: number) => void;
  onRaise: (amount: number) => void;
}

export const ActionPanel: React.FC<Props> = ({ disabled, onFold, onCheck, onCall, onBet, onRaise }) => {
  const [amount, setAmount] = useState<number>(50);

  return (
    <Box sx={{ mt: 3 }}>
      <Stack direction="row" spacing={2} alignItems="center">
        <Button variant="outlined" onClick={onFold} disabled={disabled} color="inherit">
          Fold
        </Button>
        <Button variant="outlined" onClick={onCheck} disabled={disabled}>
          Check
        </Button>
        <Button variant="contained" onClick={onCall} disabled={disabled} color="secondary">
          Call
        </Button>
        <TextField
          type="number"
          label="Amount"
          size="small"
          value={amount}
          onChange={(e) => setAmount(Number(e.target.value))}
          sx={{ width: 120 }}
          inputProps={{ min: 1 }}
        />
        <Button variant="contained" onClick={() => onBet(amount)} disabled={disabled} color="primary">
          Bet
        </Button>
        <Button variant="contained" onClick={() => onRaise(amount)} disabled={disabled} color="success">
          Raise
        </Button>
      </Stack>
    </Box>
  );
};
