import React from "react";
import { useLocation, useNavigate } from "react-router-dom";
import { useDispatch, useSelector } from "react-redux";
import { useAuth, useUser } from "@clerk/clerk-react";
import toast from "react-hot-toast";
import { XCircle } from "lucide-react";
import { Pie } from "react-chartjs-2";

import {
  Chart as ChartJS,
  ArcElement,
  Tooltip,
  Legend,
} from "chart.js";
import { generateCertificate } from "../../features/certificateSlice";

ChartJS.register(ArcElement, Tooltip, Legend);

const QuizResultPage = () => {
  const navigate = useNavigate();
  const dispatch = useDispatch();
  const { getToken, isSignedIn } = useAuth();
  const {user} = useUser();
  const { loading } = useSelector((state) => state.certificates);

  const { state } = useLocation();
  const { quiz, answers, backendResult } = state || {};

  if (!quiz || !answers || !backendResult) {
    return (
      <div className="flex items-center justify-center h-screen bg-gradient-to-tr from-white via-blue-50 to-purple-100">
        <p className="text-gray-500 text-lg">No result data available.</p>
      </div>
    );
  }

  const { score, total } = backendResult;
  const percentage = ((score / total) * 100).toFixed(0);

  const pieData = {
    labels: ["Correct", "Wrong"],
    datasets: [
      {
        data: [score, total - score],
        backgroundColor: ["#7c3aed", "#f87171"],
        hoverBackgroundColor: ["#a78bfa", "#fca5a5"],
        borderWidth: 1,
      },
    ],
  };

  const handleGenerateCertificate = async () => {
    try {
      const resultAction = await dispatch(
        generateCertificate({
          userId: user.id,
          userName: user.fullName,
          quizTitle: quiz.title,
          score,
          totalQuestions: total,
          difficulty: quiz.difficulty,
          getToken,
          isSignedIn,
          quizId: quiz.id
        })
      );

      if (generateCertificate.fulfilled.match(resultAction)) {
        toast.success("🎉 Your certificate is generated successfully! Redirecting...");
        setTimeout(() => {
          navigate("/certificates");
        }, 2000);
      } else {
        toast.error(resultAction.payload || "Failed to generate certificate.");
      }
    } catch (err) {
      toast.error("Something went wrong while generating the certificate.");
    }
  };

  return (
    <div className="min-h-screen w-full flex items-center justify-center bg-gradient-to-tr from-white via-blue-50 to-purple-100 p-4">
      <div className="flex flex-col md:flex-row w-full max-w-7xl gap-8">
        {/* Left Panel: Quiz Info + Chart */}
        <div className="flex flex-col items-center bg-white rounded-3xl shadow-2xl p-8 md:w-1/3 w-full border border-purple-200">
          <h2 className="text-3xl md:text-4xl font-extrabold text-purple-700 mb-6 text-center tracking-tight">
            {quiz.title}
          </h2>

          <div className="w-48 h-48 md:w-56 md:h-56 mb-6">
            <Pie data={pieData} />
          </div>

          <p className="text-3xl md:text-4xl text-indigo-700 font-bold mb-2">
            {score} / {total}
          </p>
          <p className="text-xl md:text-2xl text-purple-600 font-semibold tracking-wide">
            {percentage}% Correct
          </p>

          {/* ✅ Generate Certificate Button */}
          {percentage >= 70 && (
            <button
              onClick={handleGenerateCertificate}
              disabled={loading}
              className="mt-4 w-full bg-green-600 text-white py-2 px-4 rounded-xl shadow hover:bg-green-700 transition-all duration-200"
            >
              {loading ? "Generating..." : "Generate Certificate"}
            </button>
          )}
        </div>

        {/* Right Panel: Question Review */}
        <div className="flex-1 bg-white rounded-3xl shadow-2xl p-6 md:p-8 border border-purple-200 overflow-auto max-h-[85vh]">
          <div className="flex justify-between items-center mb-6 sticky top-0 bg-white pt-4 pb-4 z-10">
            <h2 className="text-2xl md:text-3xl font-extrabold text-purple-700 tracking-tight">
              Question Review
            </h2>
            <button
              onClick={() => navigate("/dashboard/allquizzes")}
              className="flex items-center gap-2 px-4 py-2 md:px-5 md:py-3 rounded-lg bg-gray-100 text-gray-600 hover:bg-purple-100 hover:text-purple-700 transition shadow-md focus:outline-none focus:ring-2 focus:ring-purple-400 cursor-pointer"
            >
              <XCircle size={20} />
              Exit
            </button>
          </div>

          <div className="grid grid-cols-1 sm:grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
            {quiz.questions.map((q, idx) => {
              console.log(q)
              const userAnswer = answers[q.id];
              const isCorrect = userAnswer === q.correctAnswer;

              return (
                <div
                  key={q.id}
                  className={`p-5 md:p-6 rounded-2xl border shadow-sm transition-transform transform hover:scale-[1.02] ${
                    isCorrect
                      ? "bg-green-50 border-green-300"
                      : "bg-red-50 border-red-300"
                  }`}
                >
                  <p className="font-semibold text-indigo-700 mb-2 text-lg md:text-base tracking-wide">
                    Q{idx + 1}: {q.question}
                  </p>
                  <p className="text-base">
                    Your Answer:{" "}
                    <span
                      className={`font-semibold ${
                        isCorrect ? "text-green-700" : "text-red-700"
                      }`}
                    >
                      {userAnswer ?? "Not answered"}
                    </span>
                  </p>
                  {!isCorrect && (
                    <p className="text-sm text-gray-700 mt-2">
                      Correct Answer:{" "}
                      <span className="font-semibold text-green-800">
                        {q.correctAnswer}
                      </span>
                    </p>
                  )}
                </div>
              );
            })}
          </div>
        </div>
      </div>
    </div>
  );
};

export default QuizResultPage;
