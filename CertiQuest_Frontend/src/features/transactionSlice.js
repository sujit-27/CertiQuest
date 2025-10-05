import { createSlice, createAsyncThunk } from '@reduxjs/toolkit';
import axios from 'axios';

// Async thunk to fetch user transactions with auth token
export const fetchUserTransactions = createAsyncThunk(
  'transactions/fetchUserTransactions',
  async ({ getToken, isSignedIn }, { rejectWithValue }) => {
    if (!isSignedIn) return rejectWithValue('Not signed in');
    try {
      const token = await getToken();
      const response = await axios.get('https://certiquest.up.railway.app/api/transactions', {
        headers: { Authorization: `Bearer ${token}` },
        withCredentials: true,
      });
      if (response.status !== 200) {
        return rejectWithValue(`Unexpected status code: ${response.status}`);
      }
      return response.data; // Expecting array of transactions
    } catch (error) {
      return rejectWithValue(error.message || 'Unknown error fetching transactions');
    }
  }
);

const transactionSlice = createSlice({
  name: 'transactions',
  initialState: {
    transactions: [],
    loading: false,
    error: null,
  },
  reducers: {
    clearTransactions: (state) => {
      state.transactions = [];
      state.loading = false;
      state.error = null;
    }
  },
  extraReducers: (builder) => {
    builder
      .addCase(fetchUserTransactions.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(fetchUserTransactions.fulfilled, (state, action) => {
        state.transactions = action.payload;
        state.loading = false;
      })
      .addCase(fetchUserTransactions.rejected, (state, action) => {
        state.loading = false;
        state.error = action.payload || action.error.message;
      });
  },
});

export const { clearTransactions } = transactionSlice.actions;
export default transactionSlice.reducer;
