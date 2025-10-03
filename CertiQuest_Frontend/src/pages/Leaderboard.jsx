import React, { useEffect, useState } from "react";
import { useDispatch, useSelector } from "react-redux";
import { useAuth } from "@clerk/clerk-react";
import { useNavigate, Outlet, useLocation } from "react-router-dom";
import DashboardLayout from "../layout/DashboardLayout";
import {
  fetchGlobalLeaderboard,
  fetchQuizLeaderboard,
  clearLeaderboard,
} from "../features/leaderboardSlice";
import { fetchAllQuizzes } from "../features/quizzesSlice";
import { Bar } from "react-chartjs-2";
import {
  Chart as ChartJS,
  CategoryScale,
  LinearScale,
  BarElement,
  Title,
  Tooltip,
  Legend,
} from "chart.js";

ChartJS.register(CategoryScale, LinearScale, BarElement, Title, Tooltip, Legend);

const Leaderboard = () => {
  const dispatch = useDispatch();
  const { getToken, isSignedIn } = useAuth();

  const { allQuizzes } = useSelector((state) => state.quizzes);
  const { global, quizSpecific, loading, error } = useSelector(
    (state) => state.leaderboard
  );
  console.log(quizSpecific)
  const [selectedQuizId, setSelectedQuizId] = useState("");

  // Fetch quizzes
  useEffect(() => {
    if (isSignedIn) {
      dispatch(fetchAllQuizzes({ getToken, isSignedIn }));
    }
  }, [dispatch, getToken, isSignedIn]);

  // Fetch leaderboards
  useEffect(() => {
    if (isSignedIn) {
      dispatch(fetchGlobalLeaderboard({ getToken, isSignedIn }));
      if (selectedQuizId) {
        dispatch(fetchQuizLeaderboard({ quizId: selectedQuizId, getToken, isSignedIn }));
      }
    }
    return () => dispatch(clearLeaderboard());
  }, [dispatch, getToken, isSignedIn, selectedQuizId]);

  const handleQuizSelect = (quizId) => {
    setSelectedQuizId(quizId);
  };

  // Chart data for global leaderboard
  const globalChartData = {
    labels: global.map((entry) => entry.userName),
    datasets: [
      {
        label: "Points",
        data: global.map((entry) => entry.totalPoints),
        backgroundColor: "rgba(131, 56, 236, 0.7)",
      },
    ],
  };

  // Chart data for quiz-specific leaderboard
  const quizChartData = {
  labels: quizSpecific.map((entry) => entry.userName || ""),
  datasets: [
    {
      label: "Score",
      data: quizSpecific.map((entry) => entry.totalPoints || 0),  // Changed from entry.score to entry.totalPoints
      backgroundColor: "rgba(56, 136, 236, 0.7)",
    },
  ],
};

  return (
    <DashboardLayout activeMenu="Leaderboard">
      <div className="py-6 max-w-6xl mx-auto space-y-8">
        <h1 className="text-3xl font-extrabold text-purple-900 mb-2">Leaderboard</h1>
        <p className="text-gray-700 mb-6">
          See how you rank globally or for a specific quiz.
        </p>

        <div className="flex flex-col md:flex-row gap-6">
          {/* Global Leaderboard */}
          <div className="flex-1 bg-white p-6 rounded-xl shadow space-y-4">
            <h2 className="text-xl font-semibold text-purple-800">Global Leaderboard</h2>
            {loading && <p className="text-gray-500">Loading...</p>}
            {error && <p className="text-red-500">{error}</p>}
            {!loading && global.length === 0 && (
              <p className="text-gray-600">No data available.</p>
            )}
            {!loading && global.length > 0 && (
              <Bar
                data={globalChartData}
                options={{
                  responsive: true,
                  plugins: {
                    legend: { display: false },
                    title: { display: true, text: "Global Leaderboard" },
                  },
                }}
              />
            )}
          </div>

          {/* Quiz-Specific Leaderboard */}
          <div className="flex-1 bg-white p-6 rounded-xl shadow space-y-4">
            <div className="flex items-center justify-between mb-4">
              <h2 className="text-xl font-semibold text-purple-800">Quiz Leaderboard</h2>
              <select
                className="border border-gray-300 rounded-md px-3 py-1 cursor-pointer"
                value={selectedQuizId}
                onChange={(e) => handleQuizSelect(e.target.value)}
              >
                <option value="">Select Quiz</option>
                {allQuizzes.map((quiz) => (
                  <option key={quiz.id} value={quiz.id}>
                    {quiz.title} - {quiz.category}
                  </option>
                ))}
              </select>
            </div>

            {selectedQuizId && quizSpecific.length > 0 ? (
              <Bar
                data={quizChartData}
                options={{
                  responsive: true,
                  plugins: {
                    legend: { display: false },
                    title: { display: true, text: "Quiz Leaderboard" },
                  },
                }}
              />
            ) : selectedQuizId && quizSpecific.length === 0 ? (
              <p className="text-gray-600">No data available for this quiz.</p>
            ) : (
              <p className="text-gray-500">Select a quiz to view its leaderboard.</p>
            )}
          </div>
        </div>
         <div className="mt-8 rounded-lg bg-purple-100 p-6 text-purple-900 font-semibold shadow-md text-center">
          <p>
            Ready to see your name here? Take quizzes, improve your skills, and climb the leaderboard!
            Join the challenge and become one of the top achievers today!
          </p>
        </div>
      </div>
    </DashboardLayout>
  );
};

export default Leaderboard;
