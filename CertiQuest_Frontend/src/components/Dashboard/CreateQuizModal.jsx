import React, { useState } from "react";
import axios from "axios";
import { X } from "lucide-react";
import { useAuth, useUser } from "@clerk/clerk-react";
import { useDispatch } from "react-redux";
import { createQuiz, createQuizFromPdf } from "../../features/quizzesSlice";
import toast from "react-hot-toast";

const difficulties = [
  { value: "easy", label: "Easy" },
  { value: "medium", label: "Medium" },
  { value: "hard", label: "Hard" },
];

const CreateQuizModal = ({onClose}) => {

    const [title, setTitle] = useState("");
    const [category, setCategory] = useState("");
    const [difficulty, setDifficulty] = useState("easy");
    const [noOfQuestions, setNoOfQuestions] = useState(5);
    const [loading, setLoading] = useState(false);
    const [pdfFile, setPdfFile] = useState(null);
    const dispatch = useDispatch();
    const { user } = useUser();
    const { getToken } = useAuth();

    const handleCreate = async () => {
        if (!title || !category || !noOfQuestions) {
            toast.error("Please fill all required fields");
            return;
        }

        try {
            setLoading(true);
            const token = await getToken();

            const quizData = {
                title,
                category,
                difficulty,
                noOfQuestions,
                token,
                createdBy: user.id,
            };

            const createdQuiz = await dispatch(createQuiz(quizData)).unwrap();

            toast.success(`Quiz "${createdQuiz.title}" created successfully!`);
            onClose();
        } catch (err) {
            let errorMessage;

            if (err) {
                if (err.error) {
                    errorMessage = err.error;
                } 
                else if (typeof err === "string") {
                    errorMessage = err;
                }
            }

            toast.error(errorMessage);
            console.error("Quiz creation error:", err);
        } finally {
            setLoading(false);
        }
    };

    const handleUploadPdf = async () => {
    if (!title || !category || !noOfQuestions) {
        toast.error("Please fill all required fields");
        return;
    }

    if (!pdfFile) {
        toast.error("Please select a PDF file");
        return;
    }

    try {
        setLoading(true);

        // Prepare FormData for multipart request
        const formData = new FormData();
        formData.append("pdfFile", pdfFile); // must match @RequestParam("pdfFile")
        formData.append("title", title);
        formData.append("category", category);
        formData.append("difficulty", difficulty || "Medium");
        formData.append("createdBy", user.id);

        // Dispatch thunk with formData
        await dispatch(createQuizFromPdf({ formData, getToken })).unwrap();

        toast.success("Quiz created successfully from PDF!");
        onClose();
    } catch (err) {
        // Handle different error shapes safely
        let errorMessage = "Failed to create quiz from PDF";

        if (err) {
            if (typeof err === "string") errorMessage = err;
            else if (err.error) errorMessage = err.error;
            else if (err.message) errorMessage = err.message;
        }

        toast.error(errorMessage);
        console.error("PDF quiz creation error:", err);
    } finally {
        setLoading(false);
    }
};



    return (
        <div className="fixed inset-0 flex justify-center items-center z-50 bg-gradient-to-br from-blue-200/30 via-white/30 to-purple-200/30 backdrop-blur-lg">
            <div className="bg-white rounded-2xl shadow-2xl p-8 w-full max-w-lg relative border border-blue-100">
                {/* Close Button */}
                <button
                className="absolute top-4 right-4 text-gray-500 hover:text-blue-700 transition"
                onClick={onClose}
                aria-label="Close"
                >
                <X size={22} />
                </button>

                <h2 className="text-2xl font-bold mb-7 text-blue-900 text-center bg-gradient-to-r from-blue-800 via-purple-700 to-blue-500 bg-clip-text text-transparent drop-shadow">
                Create a New Quiz
                </h2>

                <div className="space-y-5">
                {/* Title */}
                <div>
                    <label className="block text-sm font-semibold text-blue-800 mb-1">Quiz Title</label>
                    <input
                    type="text"
                    placeholder="Enter a quiz title"
                    className="w-full border-2 border-blue-200 rounded-xl p-3 focus:outline-none focus:ring-2 focus:ring-blue-500/30 transition placeholder:text-blue-400 bg-blue-50"
                    value={title}
                    onChange={(e) => setTitle(e.target.value)}
                    />
                </div>

                {/* Category */}
                <div>
                    <label className="block text-sm font-semibold text-blue-800 mb-1">Topic</label>
                    <input
                    type="text"
                    placeholder="e.g. Java Programming, Science"
                    className="w-full border-2 border-blue-200 rounded-xl p-3 focus:outline-none focus:ring-2 focus:ring-purple-500/30 transition placeholder:text-purple-400 bg-purple-50"
                    value={category}
                    onChange={(e) => setCategory(e.target.value)}
                    />
                </div>

                {/* Difficulty & # Questions */}
                <div className="flex gap-5 flex-col md:flex-row">
                    <div className="flex-1">
                    <label className="block text-sm font-semibold text-blue-800 mb-1">Difficulty</label>
                    <select
                        className="w-full border-2 border-blue-200 rounded-xl p-3 focus:outline-none focus:ring-2 focus:ring-blue-500/30 transition bg-white"
                        value={difficulty}
                        onChange={(e) => setDifficulty(e.target.value)}
                    >
                        {difficulties.map((d) => (
                        <option key={d.value} value={d.value}>{d.label}</option>
                        ))}
                    </select>
                    </div>
                    <div className="flex-1">
                    <label className="block text-sm font-semibold text-blue-800 mb-1">Number of Questions</label>
                    <input
                        type="number"
                        min={1}
                        max={20}
                        placeholder="e.g. 5"
                        className="w-full border-2 border-blue-200 rounded-xl p-3 focus:outline-none focus:ring-2 focus:ring-indigo-500/30 transition bg-blue-50"
                        value={noOfQuestions}
                        onChange={(e) => setNoOfQuestions(e.target.value)}
                    />
                    </div>
                </div>

                {/* PDF Upload Section */}
                <div className="mt-5 p-4 border-2 border-dashed border-purple-400 rounded-xl text-center cursor-pointer hover:bg-purple-50 transition">
                    <label className="text-purple-700 font-semibold cursor-pointer">
                    📄 Have a PDF? Click to upload and create quiz
                    <input
                        type="file"
                        accept="application/pdf"
                        className="hidden"
                        onChange={(e) => setPdfFile(e.target.files[0])}
                    />
                    </label>
                    {pdfFile && <p className="mt-2 text-sm text-purple-600">{pdfFile.name}</p>}
                    {pdfFile && (
                    <button
                        onClick={handleUploadPdf}
                        disabled={loading}
                        className="mt-2 px-4 py-2 bg-purple-700 text-white rounded-lg font-semibold hover:bg-purple-800 transition disabled:opacity-50 disabled:cursor-not-allowed"
                    >
                        {loading ? "Creating..." : "Create from PDF"}
                    </button>
                    )}
                </div>
                </div>

                {/* Create Quiz Button */}
                <button
                onClick={handleCreate}
                disabled={loading}
                className="mt-10 w-full px-4 py-3 rounded-xl bg-gradient-to-r from-blue-700 via-purple-600 to-blue-900 text-white font-bold text-lg shadow-md hover:from-blue-800 hover:to-purple-700 transition flex items-center justify-center gap-2 disabled:opacity-50 disabled:cursor-not-allowed cursor-pointer"
                >
                {loading && (
                    <svg className="animate-spin mr-2 h-5 w-5 text-white" viewBox="0 0 24 24">
                    <circle
                        className="opacity-25"
                        cx="12"
                        cy="12"
                        r="10"
                        stroke="currentColor"
                        strokeWidth="4"
                        fill="none"
                    />
                    <path
                        className="opacity-75"
                        fill="currentColor"
                        d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"
                    />
                    </svg>
                )}
                {loading ? "Creating..." : "Create Quiz"}
                </button>
            </div>
        </div>
    );
};

export default CreateQuizModal;
