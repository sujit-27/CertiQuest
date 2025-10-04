import React, { useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import { useSelector, useDispatch } from "react-redux";
import { toast } from "react-hot-toast";
import { submitQuiz } from "../../features/quizzesSlice";
import { X, Timer } from "lucide-react";
import { useAuth, useUser } from "@clerk/clerk-react";

const QuizPage = () => {
  const { quizId } = useParams();
  const dispatch = useDispatch();
  const navigate = useNavigate();
  const { getToken, isSignedIn } = useAuth();
  const { user } = useUser();

  const quizzes = useSelector((state) => state.quizzes.allQuizzes);

  const [quiz, setQuiz] = useState(null);
  const [answers, setAnswers] = useState({});
  const [timeLeft, setTimeLeft] = useState(0);
  const [currentQuestionIndex, setCurrentQuestionIndex] = useState(0);

  // 1️⃣ Fetch quiz either from API or Redux store
  useEffect(() => {
    if (!quizId) return;

    const fetchQuiz = async () => {
      try {
        // First try Redux store
        const selectedQuiz = quizzes.find((q) => q.id === parseInt(quizId));
        if (selectedQuiz) {
          setQuiz(selectedQuiz);
        } else {
          const response = await fetch(`https://certiquest.onrender.com/api/quizzes/${quizId}`);
          if (!response.ok) throw new Error("Quiz not found");
          const data = await response.json();
          setQuiz(data);
        }

        // Set timer based on difficulty
        let minutes = 2;
        const difficulty = selectedQuiz?.difficulty?.toLowerCase() || data?.difficulty?.toLowerCase();
        if (difficulty === "medium") minutes = 4;
        else if (difficulty === "hard") minutes = 5;
        setTimeLeft(minutes * 60);
      } catch (err) {
        console.error("Failed to load quiz:", err);
        toast.error("Quiz not found. Redirecting to dashboard.");
        navigate("/dashboard");
      }
    };

    fetchQuiz();
  }, [quizId, quizzes, navigate]);

  // 2️⃣ Timer countdown
  useEffect(() => {
    if (timeLeft <= 0) return;

    const interval = setInterval(() => {
      setTimeLeft((prev) => {
        if (prev <= 1) {
          clearInterval(interval);
          handleSubmit();
          return 0;
        }
        return prev - 1;
      });
    }, 1000);

    return () => clearInterval(interval);
  }, [timeLeft]);

  const handleAnswerChange = (questionId, value) => {
    setAnswers((prev) => ({ ...prev, [questionId]: value }));
  };

  const handleNext = () => {
    if (currentQuestionIndex < quiz.questions.length - 1) {
      setCurrentQuestionIndex(currentQuestionIndex + 1);
    }
  };

  const handlePrev = () => {
    if (currentQuestionIndex > 0) {
      setCurrentQuestionIndex(currentQuestionIndex - 1);
    }
  };

  const handleSubmit = async (quiz) => {
    
    try {        
        if (!quiz?.id || quiz.id <= 0) {
            toast.error("Quiz not loaded properly. Please refresh the page.");
            return;
        }

        const answerArray = Object.entries(answers).map(([questionId, selectedAnswer]) => ({
            questionId: parseInt(questionId),
            selectedAnswer,
        }));

        if (!isSignedIn) {
            toast.error("You must be signed in to submit the quiz");
            return;
        }

      const token = await getToken();

      const result = await dispatch(
        submitQuiz({
          quizId: quiz.id,
          answers: answerArray,
          isSignedIn,
          token,
          userId: user.id,
        })
      ).unwrap();

      toast.success("Quiz submitted successfully!");

      navigate(`/quiz/${quiz.id}/result`, {
        state: { quiz, answers, backendResult: result },
      });
    } catch (err) {
      console.error(err);
      toast.error(err?.message || "Failed to submit quiz");
    }
  };

  const handleExit = () => {
    navigate("/dashboard/allquizzes");
  };

  if (!quiz) return <p className="text-center mt-16 text-lg text-purple-400">Loading quiz...</p>;

  const currentQuestion = quiz.questions[currentQuestionIndex];

  return (
    <div className="min-h-screen bg-gradient-to-tr from-white via-blue-50 to-purple-100 flex items-center justify-center p-4">
      <div className="w-full max-w-2xl bg-white rounded-3xl shadow-2xl px-8 pt-7 pb-6 relative flex flex-col border border-purple-100">
        {/* Title and Timer */}
        <div className="flex justify-between items-center mb-4">
          <h2 className="text-2xl font-bold text-purple-700">{quiz.title}</h2>
          <div className="flex gap-2 items-center">
            <span className="flex items-center gap-2 px-3 py-1 bg-blue-100 text-purple-700 font-semibold rounded-xl shadow-sm">
              <Timer size={20} className="text-indigo-500" />
              <span className="font-mono">{Math.floor(timeLeft / 60)}:{String(timeLeft % 60).padStart(2, "0")}</span>
            </span>
          </div>
        </div>

        {/* Progress Bar */}
        <div className="w-full mb-6">
          <div className="h-2 rounded-xl bg-blue-100">
            <div
              className="h-2 rounded-xl bg-gradient-to-r from-purple-400 via-indigo-400 to-blue-400 transition-all"
              style={{ width: `${((currentQuestionIndex + 1) / quiz.questions.length) * 100}%` }}
            />
          </div>
        </div>

        <div className="flex-1">
          {/* Question */}
          <div className="mb-5">
            <p className="font-semibold text-indigo-600 text-lg mb-2">
              Question {currentQuestionIndex + 1} of {quiz.questions.length}
            </p>
            <p className="mb-4 text-purple-800 text-base">{currentQuestion.question}</p>
            <div className="space-y-3">
              {currentQuestion.options.map((opt) => (
                <label
                  key={opt}
                  className={`block px-4 py-2 rounded-xl shadow-sm border cursor-pointer transition
                    ${answers[currentQuestion.id] === opt
                      ? "bg-blue-100 border-indigo-400 text-indigo-900 font-semibold"
                      : "bg-white hover:bg-purple-50 border-blue-100 text-purple-700"
                    }`}
                >
                  <input
                    type="radio"
                    name={`question-${currentQuestion.id}`}
                    value={opt}
                    checked={answers[currentQuestion.id] === opt}
                    onChange={() => handleAnswerChange(currentQuestion.id, opt)}
                    className="mr-2 accent-indigo-600"
                  />
                  {opt}
                </label>
              ))}
            </div>
          </div>
        </div>

        {/* Navigation */}
        <div className="flex justify-between mt-7 mb-3">
          {currentQuestionIndex > 0 ? (
            <button
              onClick={handlePrev}
              className="px-5 py-2 rounded-lg bg-purple-50 text-indigo-700 font-medium shadow transition hover:bg-blue-100"
            >
              Previous
            </button>
          ) : <div />}

          {currentQuestionIndex < quiz.questions.length - 1 ? (
            <button
              onClick={handleNext}
              className="px-5 py-2 rounded-lg bg-gradient-to-r from-blue-500 to-purple-500 text-white font-medium shadow transition hover:from-blue-600 hover:to-purple-600 cursor-pointer"
              disabled={!answers[currentQuestion.id]}
            >
              Next
            </button>
          ) : (
            <button
              onClick={() => handleSubmit(quiz)}
              className="px-5 py-2 rounded-lg bg-gradient-to-r from-indigo-600 to-purple-800 text-white font-semibold shadow transition hover:from-indigo-700 hover:to-purple-900 cursor-pointer"
              disabled={!answers[currentQuestion.id]}
            >
              Submit Quiz
            </button>
          )}
        </div>

        {/* Exit Button */}
        <button
          onClick={handleExit}
          className="flex items-center justify-center gap-2 mt-5 px-4 py-2 w-full rounded-lg font-medium bg-gray-100 text-gray-600 hover:bg-purple-100 transition shadow cursor-pointer"
        >
          <X size={18} />
          Exit Quiz
        </button>
      </div>
    </div>
  );
};

export default QuizPage;
