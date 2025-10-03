import { createSlice, createAsyncThunk } from "@reduxjs/toolkit";
import axios from "axios";

// Async thunk to fetch user results
export const fetchUserResults = createAsyncThunk(
  "results/fetchUserResults",
  async ({ getToken, isSignedIn, userId }, { rejectWithValue }) => {
    try {
      if (!isSignedIn) return rejectWithValue("Not signed in");
      const token = await getToken();

      const response = await axios.get(`http://localhost:8080/api/results/${userId}`,
        {
          headers: {
            Authorization: `Bearer ${token}`,
          },
        }
      );
      
      return response.data;
    } catch (error) {
      return rejectWithValue(error.response?.data || error.message);
    }
  }
);

const resultsSlice = createSlice({
  name: "results",
  initialState: {
    results: [],
    loading: false,
    error: null,
  },
  reducers: {
    clearResults: (state) => {
      state.results = [];
      state.loading = false;
      state.error = null;
    },
  },
  extraReducers: (builder) => {
    builder
      .addCase(fetchUserResults.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(fetchUserResults.fulfilled, (state, action) => {
        state.loading = false;
        state.results = action.payload;
      })
      .addCase(fetchUserResults.rejected, (state, action) => {
        state.loading = false;
        state.error = action.payload || "Failed to fetch results";
      });
  },
});

export const { clearResults } = resultsSlice.actions;
export default resultsSlice.reducer;
