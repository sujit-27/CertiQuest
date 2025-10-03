import { configureStore } from "@reduxjs/toolkit";
import roleReducer from "../features/roleSlice";
import pointsReducer from "../features/pointsSlice"
import quizzesReducer from "../features/quizzesSlice"
import resultsReducer from "../features/resultsSlice"
import certificatesReducer from "../features/certificateSlice"
import leaderboardReducer from "../features/leaderboardSlice"
import paymentReducer from "../features/paymentSlice"
import transactionReducer from "../features/transactionSlice"

const store = configureStore({
  reducer: {
    role: roleReducer,
    points: pointsReducer,
    quizzes: quizzesReducer,
    results: resultsReducer,
    certificates: certificatesReducer,
    leaderboard: leaderboardReducer,
    payment: paymentReducer,
    transactions: transactionReducer,
  },
});

export default store;
