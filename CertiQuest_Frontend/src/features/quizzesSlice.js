import { createSlice, createAsyncThunk } from '@reduxjs/toolkit';
import axios from 'axios';

export const fetchAllQuizzes = createAsyncThunk(
  'quizzes/fetchAll',
  async ({ getToken, isSignedIn }, { rejectWithValue }) => {
    try {
      if (!isSignedIn) return rejectWithValue('Not signed in');
      const token = await getToken();
      const response = await axios.get('http://localhost:8080/api/quiz', {
        headers: { Authorization: `Bearer ${token}` },
      });
      return response.data || [];
    } catch (err) {
      return rejectWithValue(err.response?.data || err.message);
    }
  }
);

export const createQuiz = createAsyncThunk(
  'quizzes/create',
  async ({ title, category, difficulty, noOfQuestions, token, createdBy }, { rejectWithValue }) => {
    try {
      const body = { title, category, difficulty, noOfQuestions, createdBy };
      const response = await axios.post('http://localhost:8080/api/quiz/create', body, {
        headers: { Authorization: `Bearer ${token}`, 'Content-Type': 'application/json' },
      });
      return response.data;
    } catch (err) {
      return rejectWithValue(err.response?.data || err.message);
    }
  }
);

export const createQuizFromPdf = createAsyncThunk(
  "quiz/createFromPdf",
  async ({ formData, getToken }, { rejectWithValue }) => {
    try {
      const token = await getToken();
      const response = await axios.post(
        "http://localhost:8080/api/quiz/create-quiz-pdf",
        formData,
        {
          headers: {
            "Authorization": `Bearer ${token}`,
            // Axios automatically sets Content-Type for FormData
          },
        }
      );
      return response.data;
    } catch (err) {
      console.error("PDF creation error in thunk:", err.response?.data || err.message);
      return rejectWithValue(err.response?.data || "Failed to create quiz from PDF");
    }
  }
);



// UPDATED submitQuiz to include userId
export const submitQuiz = createAsyncThunk(
  'quizzes/submit',
  async ({ quizId, answers,isSignedIn, token, userId }, { rejectWithValue }) => {
    try {
      if (!isSignedIn) return rejectWithValue('Not signed in');
      const response = await axios.post(
        `http://localhost:8080/api/quiz/${quizId}/submit?userId=${encodeURIComponent(userId)}`,
        { quizId,answers },
        { headers: { Authorization: `Bearer ${token}`, 'Content-Type': 'application/json' } }
      );
      return response.data;
    } catch (err) {
      // Backend returns 403 or other errors
      return rejectWithValue(err.response?.data?.error || err.message);
    }
  }
);

export const joinQuiz = createAsyncThunk(
  "quizzes/join",
  async ({ quizId, userId, getToken, isSignedIn }, { rejectWithValue }) => {
    try {
      if (!isSignedIn) return rejectWithValue("Not signed in");
      const token = await getToken();

      const response = await axios.post(
        `http://localhost:8080/api/quiz/${quizId}/join`,
        { userId },
        { headers: { Authorization: `Bearer ${token}` } }
      );

      return response.data;
    } catch (err) {
      return rejectWithValue(err.response?.data || err.message);
    }
  }
);

export const updateQuiz = createAsyncThunk(
  "quizzes/updateQuiz",
  async ({ id, title, difficulty, noOfQuestions, getToken }, { rejectWithValue }) => {
    try {
      // ✅ Get token if required (Clerk/Auth integration)
      const token = await getToken();

      const response = await axios.put(
        `http://localhost:8080/api/quiz/update`,
        null,
        {
          params: { id, title, difficulty, noOfQuestions },
          headers: {
            Authorization: `Bearer ${token}`,
          },
        }
      );

      return response.data; // updated quiz object
    } catch (error) {
      console.error("Error updating quiz:", error);
      return rejectWithValue(error.response?.data || "Failed to update quiz");
    }
  }
);

export const deleteQuiz = createAsyncThunk(
  "quizzes/deleteQuiz",
  async ({ id, getToken, isSignedIn }, { rejectWithValue }) => {
    try {
      if (!isSignedIn) return rejectWithValue("Not signed in");
      const token = await getToken();
      const response = await axios.delete(
        `http://localhost:8080/api/quiz/delete?id=${id}`,
        {
          headers: { Authorization: `Bearer ${token}` },
        }
      );
      return { id, message: response.data };
    } catch (error) {
      return rejectWithValue(error.response?.data || error.message);
    }
  }
);

const quizzesSlice = createSlice({
  name: 'quizzes',
  initialState: {
    allQuizzes: [],
    loading: false,
    error: null,
    submissionResult: null,
  },
  reducers: {
    clearError: (state) => { state.error = null; },
    clearSubmissionResult: (state) => { state.submissionResult = null; },
  },
  extraReducers: (builder) => {
    builder
      // fetchAllQuizzes
      .addCase(fetchAllQuizzes.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(fetchAllQuizzes.fulfilled, (state, action) => {
        state.loading = false;
        state.allQuizzes = action.payload;
      })
      .addCase(fetchAllQuizzes.rejected, (state, action) => {
        state.loading = false;
        state.error = action.payload || 'Failed to fetch quizzes';
      })
      // createQuiz
      .addCase(createQuiz.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(createQuiz.fulfilled, (state, action) => {
        state.loading = false;
        if (action.payload) state.allQuizzes.push(action.payload);
      })
      .addCase(createQuiz.rejected, (state, action) => {
        state.loading = false;
        state.error = action.payload || 'Failed to create quiz';
      })
      // submitQuiz
      .addCase(submitQuiz.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(submitQuiz.fulfilled, (state, action) => {
        state.loading = false;
        state.submissionResult = action.payload;
      })
      .addCase(submitQuiz.rejected, (state, action) => {
        state.loading = false;
        state.error = action.payload || 'Failed to submit quiz';
      })
      .addCase(joinQuiz.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(joinQuiz.fulfilled, (state, action) => {
        state.loading = false;
        // Optionally update state if you want to track joined quizzes
      })
      .addCase(joinQuiz.rejected, (state, action) => {
        state.loading = false;
        state.error = action.payload || "Failed to join quiz";
      })
      .addCase(updateQuiz.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(updateQuiz.fulfilled, (state, action) => {
        state.loading = false;
        const updatedQuiz = action.payload;

        state.quizzes = state.allQuizzes.map((quiz) =>
          quiz.id === updatedQuiz.id ? updatedQuiz : quiz
        );
      })
      .addCase(updateQuiz.rejected, (state, action) => {
        state.loading = false;
        state.error = action.payload;
      })
      .addCase(deleteQuiz.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(deleteQuiz.fulfilled, (state, action) => {
        state.loading = false;
        state.quizzes = state.allQuizzes.filter((q) => q.$id !== action.payload.id);
      })
      .addCase(deleteQuiz.rejected, (state, action) => {
        state.loading = false;
        state.error = action.payload;
      }).addCase(createQuizFromPdf.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(createQuizFromPdf.fulfilled, (state, action) => {
        state.loading = false;
        if (action.payload) state.allQuizzes.push(action.payload);
      })
      .addCase(createQuizFromPdf.rejected, (state, action) => {
        state.loading = false;
        state.error = action.payload || "Failed to create quiz from PDF";
      });
  },
});

export const { clearError, clearSubmissionResult } = quizzesSlice.actions;
export default quizzesSlice.reducer;
