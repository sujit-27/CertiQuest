import { BrowserRouter, Route, Routes, Navigate } from "react-router-dom";
import Landing from "./pages/Landing";
import Dashboard from "./pages/Dashboard";
import toast, { Toaster } from "react-hot-toast";
import { useUser, SignedIn, SignedOut, RedirectToSignIn} from "@clerk/clerk-react";
import Leaderboard from "./pages/Leaderboard";
import Certificates from "./pages/Certificates";
import Subscription from "./pages/Subscription";
import Transactions from "./pages/Transactions";
import AllQuizzes from "./components/Dashboard/AllQuizzes";
import QuizPage from "./components/Dashboard/QuizPage";
import QuizResultPage from "./components/Dashboard/QuizResultPage";
import QuizDetailPage from "./components/Dashboard/QuizDetailsPage";

function App() {

  return (
    <BrowserRouter>
      <Toaster />
      <Routes>
        <Route path="/" element={<Landing />} />

        <Route
          path="/dashboard"
          element={
            <>
              <SignedIn>
                <Dashboard />
              </SignedIn>
              <SignedOut>
                <RedirectToSignIn />
              </SignedOut>
            </>
          }
        >
          <Route path="allquizzes" element={<AllQuizzes />} />
          <Route path="quiz/:id" element={<QuizDetailPage />} />
        </Route>
        <Route path='/leaderboard' element={
          <>
            <SignedIn><Leaderboard/></SignedIn>
            <SignedOut><RedirectToSignIn/></SignedOut>
          </>
          }
        />
        <Route path='/certificates' element={
          <>
            <SignedIn><Certificates/></SignedIn>
            <SignedOut><RedirectToSignIn/></SignedOut>
          </>
          }
        />
        <Route path='/subscriptions' element={
          <>
            <SignedIn><Subscription/></SignedIn>
            <SignedOut><RedirectToSignIn/></SignedOut>
          </>
          }
        />
        <Route path='/transactions' element={
          <>
            <SignedIn><Transactions/></SignedIn>
            <SignedOut><RedirectToSignIn/></SignedOut>
          </>
          }
        />
        <Route path="/quiz/:quizId" element={<QuizPage />} />
        <Route path="/quiz/:quizId/result" element={<QuizResultPage />} />
        <Route path="*" element={<RedirectToSignIn />} />
      </Routes>
    </BrowserRouter>
  );
}

export default App;
