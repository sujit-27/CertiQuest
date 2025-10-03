// src/features/leaderboardSlice.js
import { createSlice, createAsyncThunk } from "@reduxjs/toolkit";
import axios from "axios";

// Fetch Global Leaderboard
export const fetchGlobalLeaderboard = createAsyncThunk(
  "leaderboard/fetchGlobalLeaderboard",
  async ({ getToken, isSignedIn }, { rejectWithValue }) => {
    try {
      if (!isSignedIn) return rejectWithValue("User not signed in");
      const token = await getToken();
      const response = await axios.get(
        "http://localhost:8080/api/leaderboard/global",
        {
          headers: { Authorization: `Bearer ${token}` },
        }
      );
      return response.data;
    } catch (error) {
      return rejectWithValue(error.response?.data || error.message);
    }
  }
);

// Fetch Quiz-specific Leaderboard
export const fetchQuizLeaderboard = createAsyncThunk(
  "leaderboard/fetchQuizLeaderboard",
  async ({ quizId, getToken, isSignedIn }, { rejectWithValue }) => {
    try {
      if (!isSignedIn) return rejectWithValue("User not signed in");
      const token = await getToken();
      const response = await axios.get(
        `http://localhost:8080/api/leaderboard/quiz/${quizId}`,
        {
          headers: { Authorization: `Bearer ${token}` },
        }
      );
      return response.data;
    } catch (error) {
      return rejectWithValue(error.response?.data || error.message);
    }
  }
);

const leaderboardSlice = createSlice({
  name: "leaderboard",
  initialState: {
    global: [],
    quizSpecific: [],
    loading: false,
    error: null,
  },
  reducers: {
    clearLeaderboard: (state) => {
      state.global = [];
      state.quizSpecific = [];
      state.error = null;
    },
  },
  extraReducers: (builder) => {
    builder
      // Global Leaderboard
      .addCase(fetchGlobalLeaderboard.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(fetchGlobalLeaderboard.fulfilled, (state, action) => {
        state.loading = false;
        state.global = action.payload;
      })
      .addCase(fetchGlobalLeaderboard.rejected, (state, action) => {
        state.loading = false;
        state.error = action.payload;
      })

      // Quiz Leaderboard
      .addCase(fetchQuizLeaderboard.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(fetchQuizLeaderboard.fulfilled, (state, action) => {
        state.loading = false;
        state.quizSpecific = action.payload;
      })
      .addCase(fetchQuizLeaderboard.rejected, (state, action) => {
        state.loading = false;
        state.error = action.payload;
      });
  },
});

export const { clearLeaderboard } = leaderboardSlice.actions;
export default leaderboardSlice.reducer;
