import React, { useEffect } from "react";
import { useDispatch, useSelector } from "react-redux";
import { fetchAllQuizzes, joinQuiz } from "../../features/quizzesSlice";
import { useAuth, useUser } from "@clerk/clerk-react";
import { Loader2, PlayCircle } from "lucide-react";
import { motion } from "framer-motion";
import { useNavigate } from "react-router-dom";
import toast from "react-hot-toast";

const DIFFICULTY_COLORS = {
  easy: "bg-green-100 text-green-700 dark:bg-green-800/20 dark:text-green-300",
  medium: "bg-yellow-100 text-yellow-700 dark:bg-yellow-800/20 dark:text-yellow-300",
  hard: "bg-red-100 text-red-700 dark:bg-red-800/20 dark:text-red-300"
};

const AllQuizzes = ({ onTakeQuiz }) => {
  const dispatch = useDispatch();
  const navigate = useNavigate();
  const { getToken, isSignedIn } = useAuth();
  const { allQuizzes, loading, error } = useSelector((state) => state.quizzes);
  const {user} = useUser();

  useEffect(() => {
    dispatch(fetchAllQuizzes({ getToken, isSignedIn }));
  }, [dispatch, getToken, isSignedIn]);

  const handleStartQuiz = async (quiz) => {
    try {
      await dispatch(
        joinQuiz({
          quizId: quiz.id,
          userId: user.id,
          getToken,
          isSignedIn,
        })
      ).unwrap();

      navigate(`/quiz/${quiz.id}`);
    } catch (err) {
      console.error("Failed to join quiz", err);
      toast.error(err?.message || "Failed to join quiz");
    }
  };

  if (loading) {
    return (
      <div className="flex items-center justify-center h-64">
        <Loader2 className="animate-spin w-8 h-8 text-blue-500" />
        <span className="ml-2 text-gray-600 dark:text-gray-300 text-lg">Loading quizzes...</span>
      </div>
    );
  }

  if (error) {
    return (
      <div className="flex items-center justify-center h-64">
        <p className="text-red-500 font-semibold">{error}</p>
      </div>
    );
  }

  if (!allQuizzes.length) {
    return (
      <div className="flex items-center justify-center h-64">
        <p className="text-gray-500 dark:text-gray-400 text-lg">No quizzes available yet.</p>
      </div>
    );
  }

  return (
    <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-8 p-6">
      {allQuizzes.map((quiz, index) => (
        <motion.div
          key={quiz.id || index}
          initial={{ opacity: 0, y: 40 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.4, delay: index * 0.07 }}
          className="group bg-white dark:bg-gradient-to-br dark:from-gray-900 dark:to-gray-800 shadow-lg hover:shadow-xl transition-all duration-300 rounded-3xl border border-gray-100 dark:border-gray-700 p-7 flex flex-col justify-between ring-1 ring-transparent hover:ring-blue-400/50 hover:-translate-y-1"
        >
          {/* Quiz Title & Category */}
          <div className="mb-4">
            <h2 className="text-2xl font-extrabold text-gray-900 dark:text-white tracking-tight mb-2 group-hover:text-blue-600 transition-colors duration-200">
              {quiz.title}
            </h2>
            <span className="inline-block px-2 py-1 rounded-full bg-blue-100 dark:bg-blue-900/30 text-blue-700 dark:text-blue-200 text-xs font-semibold mb-3">
              {quiz.category}
            </span>
          </div>

          {/* Quiz Info Row */}
          <div className="flex items-center justify-between gap-3 mb-6">
            <span className={`px-2 py-0.5 rounded-full text-xs font-bold capitalize ${DIFFICULTY_COLORS[quiz.difficulty]}`}>
              {quiz.difficulty}
            </span>
            <span className="text-sm text-gray-500 dark:text-gray-300">
              <span className="font-semibold">{quiz.noOfQuestions}</span> Questions
            </span>
          </div>

          {/* Take Quiz Button */}
          <button
            onClick={() => handleStartQuiz(quiz)}
            className="w-full flex items-center justify-center gap-2 bg-gradient-to-r from-blue-600 to-violet-600 hover:from-blue-700 hover:to-violet-700 text-white rounded-xl py-3 font-semibold tracking-wide shadow-md transition-all duration-300 focus:ring-2 focus:ring-blue-400 dark:focus:ring-blue-700 ring-offset-2 ring-offset-white dark:ring-offset-gray-900 cursor-pointer"
          >
            <PlayCircle size={22} />
            Take Quiz
          </button>
        </motion.div>
      ))}
    </div>
  );
};

export default AllQuizzes;
