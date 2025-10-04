import { createSlice, createAsyncThunk } from '@reduxjs/toolkit';
import axios from 'axios';

// Async thunk to fetch user points with token
export const fetchUserPoints = createAsyncThunk(
  'points/fetchUserPoints',
  async ({ getToken, isSignedIn }, { rejectWithValue }) => {
    if (!isSignedIn) return rejectWithValue('Not signed in');
    try {
      const token = await getToken();
      const response = await axios.get('https://certiquest.up.railway.app/users/points', {
        headers: { Authorization: `Bearer ${token}` },
      });

      if (response.status !== 200) {
        return rejectWithValue(`Unexpected status ${response.status}`);
      }
      if (!response.data || typeof response.data.points !== 'number') {
        return rejectWithValue('Invalid server data');
      }

      return response.data; // return full object { points, plan, ... }
    } catch (error) {
      return rejectWithValue(
        error.response?.data?.message || error.message || 'Unknown error'
      );
    }
  }
);

const pointsSlice = createSlice({
  name: 'points',
  initialState: {
    points: 10,   // avoid hardcoding default
    plan: 'FREE',
    loading: false,
    error: null,
  },
  reducers: {
    updatePoints: (state, action) => {
      state.points = action.payload;
    },
    incrementPoints: (state, action) => {
      state.points += action.payload || 1;
    },
    decrementPoints: (state, action) => {
      state.points -= action.payload || 1;
    },
  },
  extraReducers: (builder) => {
    builder
      .addCase(fetchUserPoints.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(fetchUserPoints.fulfilled, (state, action) => {
        state.points = action.payload.points;
        state.plan = action.payload.plan || 'FREE';
        state.loading = false;
      })
      .addCase(fetchUserPoints.rejected, (state, action) => {
        state.loading = false;
        state.error = action.payload || action.error.message;
      });
  },
});

export const { updatePoints, incrementPoints, decrementPoints } = pointsSlice.actions;
export default pointsSlice.reducer;
