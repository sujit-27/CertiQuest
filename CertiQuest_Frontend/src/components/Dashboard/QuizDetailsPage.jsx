import React, { useEffect, useState } from "react";
import { useLocation, useNavigate, useParams } from "react-router-dom";
import { useSelector, useDispatch } from "react-redux";
import { XCircle } from "lucide-react";
import { useAuth, useUser } from "@clerk/clerk-react";
import { fetchUserResults } from "../../features/resultsSlice";
import { Pie, Bar } from "react-chartjs-2";
import {
  Chart as ChartJS,
  ArcElement,
  Tooltip,
  Legend,
  CategoryScale,
  LinearScale,
  BarElement,
} from "chart.js";
import { deleteQuiz, updateQuiz } from "../../features/quizzesSlice";
import { toast } from "react-hot-toast";
import { generateCertificate } from "../../features/certificateSlice";

ChartJS.register(ArcElement, Tooltip, Legend, CategoryScale, LinearScale, BarElement);

const QuizDetailPage = () => {
  const navigate = useNavigate();
  const { state } = useLocation();
  const { id } = useParams();
  const dispatch = useDispatch();
  const { isSignedIn, getToken } = useAuth();
  const { user } = useUser();

  const { results, loading, error } = useSelector((state) => state.results);
  const { loading: certLoading } = useSelector((state) => state.certificates);

  const { quiz: passedQuiz } = state || {};
  const [quiz, setQuiz] = useState(passedQuiz || null);

  // Delete Modal State
  const [isDeleteModalOpen, setIsDeleteModalOpen] = useState(false);
  const [quizToDelete, setQuizToDelete] = useState(null);

  // Edit Modal State
  const [showEditModal, setShowEditModal] = useState(false);
  const [editData, setEditData] = useState({
    title: quiz?.title || "",
    difficulty: quiz?.difficulty || "easy",
    noOfQuestions: quiz?.questions?.length || 0,
  });

  useEffect(() => {
    if (!passedQuiz) setQuiz(null);
  }, [passedQuiz]);

  useEffect(() => {
    if (isSignedIn && user) {
      dispatch(fetchUserResults({ getToken, isSignedIn, userId: user.id }));
    }
  }, [dispatch, getToken, isSignedIn, user]);

  if (!quiz) {
    return (
      <div className="flex items-center justify-center h-screen bg-gray-50">
        <p className="text-gray-500">Quiz not found.</p>
      </div>
    );
  }

  const isCreator = quiz.createdBy === user?.id;
  const isParticipant = quiz.participants?.includes(user?.id);

  const userResult = results.find(
    (r) => r.quizId?.toString() === id.toString() && r.participants?.includes(user?.id)
  );

  const attemptedResults = results.filter((r) => r.quizId?.toString() === id.toString());

  // Handle Generate Certificate
  const handleGenerateCertificate = async () => {
    if (!userResult) {
      toast.error("You need to attempt the quiz before generating a certificate.");
      return;
    }

    const totalQuestions = userResult.total || quiz.questions.length;

    try {
      await dispatch(
        generateCertificate({
          userId: user.id,
          userName: user.fullName,
          quizTitle: quiz.title,
          score: userResult.score,
          totalQuestions,
          quizId: quiz.id,
          difficulty: quiz.difficulty,
          getToken,
          isSignedIn,
        })
      ).unwrap();

      toast.success("🎉 Your Certificate is generated successfully! Check the Certificates section.");
    } catch (err) {
      toast.error("Failed to generate certificate: " + (err?.message || err));
    }
  };

  // Update Quiz
  const handleUpdateQuiz = async () => {
    try {
      await dispatch(
        updateQuiz({
          id: quiz.id || quiz.$id,
          title: editData.title,
          difficulty: editData.difficulty,
          noOfQuestions: editData.noOfQuestions,
          getToken,
        })
      ).unwrap();

      setQuiz({
        ...quiz,
        title: editData.title,
        difficulty: editData.difficulty,
        questions: Array(editData.noOfQuestions).fill({}),
      });

      setShowEditModal(false);
      toast.success("Quiz updated successfully!");
    } catch (err) {
      toast.error("Failed to update quiz: " + err);
    }
  };

  // Open Delete Modal
  const openDeleteModal = (quizId) => {
    setQuizToDelete(quizId);
    setIsDeleteModalOpen(true);
  };

  // Confirm Delete
    const confirmDeleteQuiz = async () => {
        console.log(quizToDelete)
        if (!quizToDelete) return;

        try {
            await dispatch(deleteQuiz({ id: quizToDelete, getToken, isSignedIn })).unwrap();

            toast.success("Quiz deleted successfully!");
            setIsDeleteModalOpen(false);
            setQuizToDelete(null);

            // Navigate away after deletion
            navigate("/dashboard");
        } catch (error) {
            toast.error("Failed to delete quiz: " + (error.message || error));
            console.log(error);
            
        }
    };
  // Pie Chart for Participant
  let pieData, pieOptions;
  if (userResult) {
    pieData = {
      labels: ["Correct", "Incorrect"],
      datasets: [
        {
          data: [userResult.score, (userResult.total || quiz.questions.length) - userResult.score],
          backgroundColor: ["#7c3aed", "#f87171"],
          hoverBackgroundColor: ["#a78bfa", "#fca5a5"],
          borderWidth: 2,
        },
      ],
    };
    pieOptions = { plugins: { legend: { display: true, position: "bottom" } } };
  }

  // Bar Chart for Creator
  let barData, barOptions;
  if (isCreator && attemptedResults.length > 0) {
    barData = {
      labels: attemptedResults.map((r, i) => `User ${i + 1}`),
      datasets: [
        {
          label: "Score",
          data: attemptedResults.map((r) => r.score),
          backgroundColor: "#7c3aed",
        },
      ],
    };
    barOptions = {
      scales: { y: { beginAtZero: true, max: quiz.questions.length } },
      plugins: { legend: { display: false } },
    };
  }

  return (
    <div className="min-h-screen flex justify-center mt-7">
      <div className="w-full max-w-6xl bg-white flex flex-col gap-5">
        {/* Header */}
        <div className="flex justify-between items-start">
          <h2 className="text-2xl md:text-3xl font-bold text-purple-700">{quiz.title}</h2>
          <button
            onClick={() => navigate(-1)}
            className="flex items-center gap-2 px-3 py-2 rounded-lg bg-gray-100 text-gray-600 hover:bg-purple-100 hover:text-purple-700 transition shadow-md"
          >
            <XCircle size={20} /> Back
          </button>
        </div>

        <p className="text-gray-600 text-lg">{quiz.description}</p>

        <div className={`grid gap-6 ${isCreator && isParticipant ? "md:grid-cols-2" : ""}`}>
          {/* Participant View */}
            {isParticipant && (
            <div className="bg-purple-50 p-6 rounded-2xl shadow-md">
                <h3 className="text-xl font-semibold text-indigo-700 mb-4">Your Results</h3>

                {loading ? (
                <p className="text-gray-500">Loading your results...</p>
                ) : error ? (
                <p className="text-red-500">{error}</p>
                ) : results.filter(r => r.quizId?.toString() === id.toString()).length > 0 ? (
                (() => {
                    // All attempts of current user for this quiz
                    const userAttempts = results
                    .filter((r) => r.quizId?.toString() === id.toString())
                    .map((r, idx) => ({
                        score: r.score,
                        total: r.total || quiz.questions.length,
                        submittedAt: r.submittedAt,
                        attemptNumber: idx + 1,
                    }));

                    // Bar chart data for attempts
                    const barData = {
                    labels: userAttempts.map((a) => `Attempt ${a.attemptNumber}`),
                    datasets: [
                        {
                        label: "Score",
                        data: userAttempts.map((a) => a.score),
                        backgroundColor: "#7c3aed",
                        },
                    ],
                    };

                    const barOptions = {
                    scales: { y: { beginAtZero: true, max: quiz.questions.length } },
                    plugins: { legend: { display: false } },
                    };

                    return (
                    <>
                        <ul className="grid gap-2 mb-4">
                        {userAttempts.map((a) => (
                            <li
                            key={a.attemptNumber}
                            className="flex justify-between items-center p-2 bg-white rounded shadow"
                            >
                            <span className="text-gray-600">
                                Attempt {a.attemptNumber} - {new Date(a.submittedAt).toLocaleString()}
                            </span>
                            <span className="flex items-center gap-2">
                                <span className="bg-indigo-100 text-indigo-700 px-2 py-1 rounded-md text-sm font-medium">
                                {((a.score / a.total) * 100).toFixed(0)}%
                                </span>
                                <span className="font-bold text-purple-700">
                                {a.score} / {a.total}
                                </span>
                            </span>
                            </li>
                        ))}
                        </ul>

                        <div className="w-full h-40 mb-4">
                        <Bar data={barData} options={barOptions} />
                        </div>

                        <button
                        onClick={handleGenerateCertificate}
                        disabled={certLoading}
                        className={`w-full px-4 py-3 rounded-xl font-semibold text-white shadow-md transition cursor-pointer ${
                            certLoading
                            ? "bg-gray-400 cursor-not-allowed"
                            : "bg-green-600 hover:bg-green-700"
                        }`}
                        >
                        {certLoading ? "Generating..." : "Generate Certificate"}
                        </button>
                    </>
                    );
                })()
                ) : (
                <p className="text-gray-500">You have not attempted this quiz yet.</p>
                )}
            </div>
            )}
          {/* Creator View */}
          {isCreator && (
            <div className="bg-indigo-50 p-6 rounded-2xl shadow-md">
              {/* Quiz Meta Info */}
              <div className="flex justify-between items-start mb-6">
                <div>
                  <h3 className="text-xl font-semibold text-indigo-700">{quiz.title}</h3>
                  <p className="text-gray-600 mt-1">{quiz.category}</p>
                  <div className="mt-2 text-sm text-gray-500 space-y-1">
                    <p>
                      <span className="font-semibold">Total Questions:</span> {quiz.questions?.length}
                    </p>
                    <p>
                      <span className="font-semibold">Difficulty:</span> {quiz.difficulty}
                    </p>
                    <p>
                      <span className="font-semibold">Created On:</span>{" "}
                      {new Date(quiz.createdAt).toLocaleDateString()}
                    </p>
                    <p>
                      <span className="font-semibold">Expiry Date:</span>{" "}
                      {new Date(quiz.expiryDate).toLocaleDateString()}
                    </p>
                  </div>
                </div>

                <div className="flex flex-col gap-2">
                  <button
                    onClick={() => setShowEditModal(true)}
                    className="px-4 py-2 bg-indigo-600 text-white rounded-xl shadow hover:bg-indigo-700 transition cursor-pointer"
                  >
                    Edit Quiz
                  </button>
                  <button
                    onClick={() => openDeleteModal(quiz.id)}
                    className="px-4 py-2 bg-red-600 text-white rounded-xl shadow hover:bg-red-700 transition cursor-pointer"
                  >
                    Delete Quiz
                  </button>
                </div>
              </div>

              <hr className="my-4 border-indigo-200" />

              {/* Participants Section */}
            <h4 className="text-lg font-semibold text-indigo-700 mb-4">Participants</h4>
            {quiz.participants && quiz.participants.length > 0 ? (
            <>
                {(() => {
                const [participantResults, setParticipantResults] = useState([]);

                useEffect(() => {
                    const fetchAllParticipantsResults = async () => {
                    const allResults = [];

                    for (let i = 0; i < quiz.participants.length; i++) {
                        const userId = quiz.participants[i];
                        try {
                        const action = await dispatch(
                            fetchUserResults({ getToken, isSignedIn, userId })
                        ).unwrap();

                        // Filter for current quiz
                        const userQuizResult = action.find(
                            (r) => r.quizId?.toString() === id.toString()
                        );

                        allResults.push({
                            userId,
                            displayName: `User ${i + 1}`,
                            score: userQuizResult?.score || 0,
                            total: userQuizResult?.total || quiz.questions.length,
                        });
                        } catch (err) {
                        allResults.push({
                            userId,
                            displayName: `User ${i + 1}`,
                            score: 0,
                            total: quiz.questions.length,
                        });
                        }
                    }

                    setParticipantResults(allResults);
                    };

                    fetchAllParticipantsResults();
                }, [quiz.participants, dispatch, getToken, isSignedIn, id]);

                // Bar chart data
                const barData = {
                    labels: participantResults.map((p) => p.displayName),
                    datasets: [
                    {
                        label: "Score",
                        data: participantResults.map((p) => p.score),
                        backgroundColor: "#7c3aed",
                    },
                    ],
                };

                const barOptions = {
                    scales: { y: { beginAtZero: true, max: quiz.questions.length } },
                    plugins: { legend: { display: false } },
                };

                return participantResults.length > 0 ? (
                    <>
                    <Bar data={barData} options={barOptions} />
                    <ul className="mt-4 grid gap-2">
                        {participantResults.map((p) => (
                        <li
                            key={p.userId}
                            className="p-3 bg-white rounded-lg shadow flex justify-between items-center"
                        >
                            <span className="font-medium text-gray-700">{p.displayName}</span>
                            <span className="flex items-center gap-3">
                            <span className="bg-indigo-100 text-indigo-700 px-2 py-1 rounded-md text-sm font-medium">
                                {((p.score / p.total) * 100).toFixed(0)}%
                            </span>
                            <span className="font-bold text-purple-700">
                                {p.score} / {p.total}
                            </span>
                            </span>
                        </li>
                        ))}
                    </ul>
                    </>
                ) : (
                    <p className="text-gray-500">Loading participants...</p>
                );
                })()}
            </>
            ) : (
            <p className="text-gray-500">No participants have been added to this quiz yet.</p>
            )}

            </div>
          )}
        </div>
      </div>

      {/* Edit Modal */}
      {showEditModal && (
        <div className="fixed inset-0 bg-black/50 flex justify-center items-center z-50">
          <div className="bg-white p-6 rounded-2xl shadow-2xl w-full max-w-md">
            <h2 className="text-2xl font-bold text-purple-700 mb-4">Edit Quiz</h2>

            <label className="block mb-2 text-gray-700">
              Title
              <input
                type="text"
                value={editData.title}
                onChange={(e) => setEditData({ ...editData, title: e.target.value })}
                className="w-full border rounded p-2 mt-1"
              />
            </label>

            <label className="block mb-2 text-gray-700">
              Difficulty
              <select
                value={editData.difficulty}
                onChange={(e) => setEditData({ ...editData, difficulty: e.target.value })}
                className="w-full border rounded p-2 mt-1"
              >
                <option value="easy">Easy</option>
                <option value="medium">Medium</option>
                <option value="hard">Hard</option>
              </select>
            </label>

            <label className="block mb-4 text-gray-700">
              Number of Questions
              <input
                type="number"
                value={editData.noOfQuestions}
                onChange={(e) =>
                  setEditData({ ...editData, noOfQuestions: Number(e.target.value) })
                }
                className="w-full border rounded p-2 mt-1"
              />
            </label>

            <div className="flex justify-between gap-3">
              <button
                onClick={() => setShowEditModal(false)}
                className="px-4 py-2 rounded-lg bg-gray-200 hover:bg-gray-300"
              >
                Cancel
              </button>
              <button
                onClick={handleUpdateQuiz}
                className="px-4 py-2 rounded-lg bg-purple-600 text-white hover:bg-purple-700"
              >
                Save Changes
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Delete Modal */}
      {isDeleteModalOpen && (
        <div className="fixed inset-0 flex items-center justify-center backdrop-blur-lg bg-opacity-50 z-50">
          <div className="bg-white p-6 rounded-lg shadow-lg w-96">
            <h2 className="text-xl font-semibold mb-4">Confirm Deletion</h2>
            <p className="mb-6">
              Are you sure you want to delete this quiz? This action cannot be undone.
            </p>
            <div className="flex justify-end gap-3">
              <button
                className="px-4 py-2 bg-gray-300 rounded hover:bg-gray-400 cursor-pointer"
                onClick={() => setIsDeleteModalOpen(false)}
              >
                Cancel
              </button>
              <button
                className="px-4 py-2 bg-red-600 text-white rounded hover:bg-red-700 cursor-pointer"
                onClick={() => confirmDeleteQuiz()}
              >
                Delete
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default QuizDetailPage;
