import { createSlice, createAsyncThunk } from "@reduxjs/toolkit";
import axios from "axios";

// Generate a certificate (calls backend generateCertificate)
export const generateCertificate = createAsyncThunk(
  "certificates/generate",
  async (
    { userId, userName, quizTitle, score, totalQuestions, difficulty, getToken, isSignedIn, quizId },
    { rejectWithValue }
  ) => {
    try {
      if (!isSignedIn) return rejectWithValue("Not signed in");
      const token = await getToken();

      console.log(userId, userName, quizTitle, score, totalQuestions, difficulty)
      const response = await axios.post(
        "http://certiquest.up.railway.app/api/certificates/generate",
        null,
        {
          params: { userId, userName, quizTitle, score, totalQuestions, difficulty, quizId },
          headers: { Authorization: `Bearer ${token}` },
        }
      );

      return response.data;
    } catch (err) {
      return rejectWithValue(err.response?.data || err.message);
    }
  }
);

export const fetchCertificates = createAsyncThunk(
  "certificates/fetchAll",
  async ({ getToken, isSignedIn }, { rejectWithValue }) => {
    try {
      if (!isSignedIn) return rejectWithValue("Not signed in");
      const token = await getToken();

      const response = await axios.get("http://certiquest.up.railway.app/api/certificates", {
        headers: { Authorization: `Bearer ${token}` },
      });

      return response.data;
    } catch (err) {
      return rejectWithValue(err.response?.data || err.message);
    }
  }
);

const certificateSlice = createSlice({
  name: "certificates",
  initialState: {
    certificates: [],
    currentCertificate: null,
    loading: false,
    error: null,
  },
  reducers: {
    clearCurrentCertificate: (state) => {
      state.currentCertificate = null;
    },
  },
  extraReducers: (builder) => {
    builder
      // Generate Certificate
      .addCase(generateCertificate.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(generateCertificate.fulfilled, (state, action) => {
        state.loading = false;
        state.currentCertificate = action.payload;

        // Avoid duplicates in certificates list
        const exists = state.certificates.find(
          (cert) => cert.id === action.payload.id
        );
        if (!exists) {
          state.certificates.push(action.payload);
        }
      })
      .addCase(generateCertificate.rejected, (state, action) => {
        state.loading = false;
        state.error = action.payload;
      })

      // Fetch Certificates
      .addCase(fetchCertificates.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(fetchCertificates.fulfilled, (state, action) => {
        state.loading = false;
        state.certificates = action.payload;
      })
      .addCase(fetchCertificates.rejected, (state, action) => {
        state.loading = false;
        state.error = action.payload;
      });
  },
});

export const { clearCurrentCertificate } = certificateSlice.actions;
export default certificateSlice.reducer;
