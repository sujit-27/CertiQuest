import React, { useEffect } from "react";
import { useDispatch, useSelector } from "react-redux";
import { useAuth, useUser } from "@clerk/clerk-react";
import { useNavigate, Outlet, useLocation } from "react-router-dom";
import DashboardLayout from "../layout/DashboardLayout";
import { clearCurrentCertificate, fetchCertificates } from "../features/certificateSlice";

const Certificates = () => {
  const dispatch = useDispatch();
  const navigate = useNavigate();
  const location = useLocation();

  const { getToken, isSignedIn } = useAuth();
  const { user } = useUser();

  const { certificates, loading, error } = useSelector((state) => state.certificates);
  

  useEffect(() => {
    if (isSignedIn) {
      dispatch(fetchCertificates({ getToken, isSignedIn }));
    }
    return () => {
      dispatch(clearCurrentCertificate());
    };
  }, [dispatch, getToken, isSignedIn]);

  const isNestedRoute = !location.pathname.endsWith("/certificates");

  return (
    <DashboardLayout activeMenu="Certificates">
      {isNestedRoute ? (
        <Outlet />
      ) : (
        <div className="px-6 py-8 max-w-7xl mx-auto">
          <h1 className="text-3xl font-extrabold text-purple-900 mb-6 border-b-4 border-purple-700 pb-2">
            My Certificates
          </h1>
          <p className="text-gray-700 mb-10 text-lg max-w-7xl">
            Explore your earned certificates from completed quizzes. Click on any item to see details or download your certificate.
          </p>

          {!isSignedIn ? (
            <div className="text-center text-gray-600 text-lg mt-20">
              Please{" "}
              <span className="font-semibold text-indigo-600 cursor-pointer hover:underline" onClick={() => navigate("/sign-in")}>
                sign in
              </span>{" "}
              to view your certificates.
            </div>
          ) : loading ? (
            <p className="text-center text-gray-500 italic text-lg">Loading certificates...</p>
          ) : error ? (
            <p className="text-center text-red-600 font-semibold text-lg">Error: {error}</p>
          ) : certificates.length === 0 ? (
            <p className="text-center text-gray-400 text-lg max-w-md mx-auto mt-30">
              You haven't earned any certificates yet. Participate in quizzes to get started!
            </p>
          ) : (
            <ul className="space-y-6">
              {certificates.map((cert) => (
                <li
                  key={cert.id}
                  tabIndex={0}
                  role="button"
                  onKeyPress={(e) => e.key === "Enter" && navigate(`certificate/${cert.id}`)}
                  className="border border-indigo-300 rounded-xl p-5 bg-white shadow-md hover:shadow-lg transition-shadow cursor-pointer focus:outline-none focus:ring-4 focus:ring-indigo-300"
                >
                  <h3 className="text-2xl font-semibold text-indigo-900 mb-2 truncate">{cert.quizTitle}</h3>
                  <div className="flex flex-wrap gap-6 text-gray-700 text-lg">
                    <p>
                      <span className="font-semibold">Score:</span> {cert.score} / {cert.totalQuestions} (
                      {cert.percentage.toFixed(2)}%)
                    </p>
                    <p>
                      <span className="font-semibold">Issued On:</span> {new Date(cert.issuedAt).toLocaleDateString()}
                    </p>
                  </div>

                  {cert.certificateUrl && (
                    <a
                      href={`${"https://certiquest.up.railway.app"}${cert.certificateUrl}`}
                      download
                      rel="noopener noreferrer"
                      onClick={(e) => e.stopPropagation()}
                      className="mt-3 inline-block text-indigo-700 font-semibold hover:underline"
                    >
                      Download Certificate
                    </a>
                  )}
                </li>
              ))}
            </ul>
          )}
        </div>
      )}
    </DashboardLayout>
  );
};

export default Certificates;
