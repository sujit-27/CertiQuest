import React, { useEffect, useState } from "react";
import { useDispatch, useSelector } from "react-redux";
import { useAuth, useUser } from "@clerk/clerk-react";
import { useNavigate, Outlet, useLocation } from "react-router-dom";
import DashboardLayout from "../layout/DashboardLayout";
import CreateQuizModal from "../components/Dashboard/CreateQuizModal";
import { fetchUserPoints } from "../features/pointsSlice";
import { fetchAllQuizzes } from "../features/quizzesSlice";

const Dashboard = () => {
  const dispatch = useDispatch();
  const navigate = useNavigate();
  const location = useLocation();

  const { getToken, isSignedIn } = useAuth();
  const { user } = useUser();

  const [isModalOpen, setIsModalOpen] = useState(false);

  const points = useSelector((state) => state.points.points);
  const quizzes = useSelector((state) => state.quizzes.allQuizzes);
  const recentlyCreatedQuizId = useSelector(
    (state) => state.quizzes.recentlyCreatedQuizId
  );
  const status = useSelector((state) => state.quizzes.status);

  // Fetch points
  useEffect(() => {
    if (isSignedIn) {
      dispatch(fetchUserPoints({ getToken, isSignedIn }));
    }
  }, [dispatch, getToken, isSignedIn]);

  // Fetch quizzes
  useEffect(() => {
    if (isSignedIn) {
      dispatch(fetchAllQuizzes({ getToken, isSignedIn }));
    }
  }, [dispatch, getToken, isSignedIn]);

  // Check if we're on a nested route
  const isNestedRoute = location.pathname !== "/dashboard";

  // Filter quizzes for "My Quizzes"
  const myQuizzes = quizzes.filter(
    (quiz) =>
      quiz.createdBy === user?.id || quiz.participants?.includes(user?.id)
  );

  return (
    <DashboardLayout activeMenu="Dashboard">
      {isNestedRoute ? (
        <Outlet />
      ) : (
        <div className="p-6">
          <h1 className="text-2xl font-bold mb-4">My Dashboard</h1>
          <p className="text-gray-600 mb-6">
            Create, attend, and track your quizzes easily.
          </p>

          {/* Create & Attend Quiz Cards */}
          <div className="grid grid-cols-1 sm:grid-cols-2 gap-6 mb-8">
            <div className="bg-white p-6 rounded-lg shadow hover:shadow-lg transition">
              <h2 className="text-xl font-semibold mb-4">Create Quiz</h2>
              <p className="text-gray-600 mb-4">
                Design new quizzes and share them with others.
              </p>
              <button
                onClick={() => setIsModalOpen(true)}
                className="bg-blue-800 text-white px-4 py-2 rounded hover:bg-blue-900 transition cursor-pointer"
              >
                Create Now
              </button>
            </div>

            <div className="bg-white p-6 rounded-lg shadow hover:shadow-lg transition">
              <h2 className="text-xl font-semibold mb-4">Attend Quiz</h2>
              <p className="text-gray-600 mb-4">
                Participate in available quizzes and test your knowledge.
              </p>
              <button
                onClick={() => navigate("allquizzes")}
                className="bg-green-700 text-white px-4 py-2 rounded hover:bg-green-900 transition cursor-pointer"
              >
                Check Available Quizzes
              </button>
            </div>
          </div>

          {/* My Quizzes Section */}
          <div className="bg-white p-8 rounded-xl shadow-md hover:shadow-xl transition-shadow duration-300">
            <h2 className="text-2xl font-bold mb-6 text-gray-900">My Quizzes</h2>

            {status === "loading" ? (
              <p className="text-gray-500 italic">Loading quizzes...</p>
            ) : myQuizzes.length === 0 ? (
              <p className="text-gray-600 text-base leading-relaxed w-full">
                You haven't created or attended any quizzes yet.{" "}
                <span
                  className="text-blue-600 font-semibold cursor-pointer hover:underline transition-colors duration-200 ml-1"
                  onClick={() => setIsModalOpen(true)}
                >
                  Create
                </span>{" "}
                or{" "}
                <span
                  className="text-green-600 font-semibold cursor-pointer hover:underline transition-colors duration-200 ml-1"
                  onClick={() => navigate("allquizzes")}
                >
                  attend
                </span>{" "}
                a quiz to get started!
              </p>
            ) : (
              <ul className="space-y-3 w-full">
                {myQuizzes.slice(0, 5).map((quiz) => (
                  <li
                    key={quiz.id}
                    className={`p-4 border rounded-lg shadow-sm cursor-pointer transition duration-300 flex flex-col hover:bg-purple-50 hover:shadow-lg focus:outline-none focus:ring-2 focus:ring-purple-400 ${
                      quiz.id === recentlyCreatedQuizId
                        ? "border-2 border-green-500 bg-green-50"
                        : "border-gray-200"
                    }`}
                    onClick={() => navigate(`quiz/${quiz.id}`, { state: { quiz } })}
                    tabIndex={0}
                    role="button"
                    onKeyPress={(e) =>
                      e.key === "Enter" && navigate(`quiz/${quiz.id}`)
                    }
                  >
                    <h3 className="text-lg font-semibold text-indigo-900 mb-1 truncate">
                      {quiz.title} - <span className="text-gray-500">{quiz.category}</span>
                    </h3>
                    <p className="text-sm text-gray-600">
                      {quiz.createdBy === user?.id ? (
                        <span className="font-medium text-green-700">
                          Created by you
                        </span>
                      ) : (
                        <span className="font-medium text-blue-700">Attended</span>
                      )}
                    </p>
                  </li>
                ))}
              </ul>
            )}
          </div>
        </div>
      )}

      {isModalOpen && (
        <CreateQuizModal
          onClose={() => setIsModalOpen(false)}
          getToken={getToken}
        />
      )}
    </DashboardLayout>
  );
};

export default Dashboard;
