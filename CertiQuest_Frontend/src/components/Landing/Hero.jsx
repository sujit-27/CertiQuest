import React from 'react'
import dashboard from "../../assets/quiz_photo-removebg-preview.png"

const Hero = ({ openSignUp }) => {

  return (
    <section
      className="landing-page-content relative bg-gradient-to-r from-purple-200 via-blue-100 to-indigo-200 min-h-screen flex items-center"
      aria-labelledby="hero-heading"
      role="region"
    >
      <div className="max-w-7xl mx-auto px-6 lg:px-8 w-full">
        <div className="grid grid-cols-1 lg:grid-cols-2 gap-20 items-center">

          {/* Left Side - Text */}
          <div className="text-center lg:text-left space-y-8 px-4 sm:px-6 md:px-0">
            <h1
              id="hero-heading"
              className="text-5xl font-extrabold tracking-wide leading-tight text-gradient bg-gradient-to-r from-purple-700 via-pink-600 to-red-500 bg-clip-text text-transparent"
            >
              Test Your Skills with <br />
              <span className="block text-6xl font-black text-purple-900">CertiQuest</span>
            </h1>

            <p className="max-w-lg mx-auto lg:mx-0 text-gray-700 text-lg md:text-xl font-medium leading-relaxed drop-shadow-sm">
              Create and attempt <span className="font-semibold text-purple-700">AI-powered quizzes</span>, track your progress, 
              and earn <span className="font-semibold text-pink-600">certifications</span> to showcase your expertise.
            </p>

            {/* CTA Buttons (Role Selection) */}
            <div className="flex flex-col sm:flex-row justify-center lg:justify-start gap-6 max-w-md mx-auto lg:mx-0">
              <button
                onClick={() => openSignUp()}
                type="button"
                aria-label="Create Quiz"
                className="relative inline-flex items-center justify-center px-10 py-4 overflow-hidden font-semibold tracking-wide text-white rounded-xl shadow-lg group
                          bg-gradient-to-r from-indigo-600 via-purple-600 to-pink-600
                          hover:from-indigo-700 hover:via-purple-700 hover:to-pink-700
                          focus:outline-none focus:ring-4 focus:ring-purple-300 transition-all duration-300 ease-out active:scale-95 cursor-pointer"
              >
                <span className="relative z-10">Get Started</span>
                <span className="absolute inset-0 w-full h-full bg-white opacity-0 group-hover:opacity-10 transition duration-500"></span>
                <span className="absolute w-0 h-0 rounded-full bg-white opacity-20 group-hover:w-40 group-hover:h-40 transition-all duration-700 ease-out"></span>
              </button>

              <button
                onClick={() => openSignUp()}
                type="button"
                className="px-8 py-4 border-2 border-purple-700 rounded-lg font-semibold text-purple-800 hover:bg-purple-50 hover:text-purple-900 transition-colors shadow-sm focus:outline-none focus:ring-4 focus:ring-purple-300 focus:ring-opacity-50 cursor-pointer"
                aria-label="Attend Quiz"
              >
                Sign in
              </button>
            </div>
          </div>

          {/* Right Side - Image */}
          <div className="relative rounded-3xl shadow-2xl overflow-hidden max-w-lg mx-auto lg:mx-0">
            <img
              src={dashboard}
              alt="CertiQuest Dashboard overview"
              className="w-full h-auto object-cover transform hover:scale-105 transition-transform duration-500 ease-in-out rounded-3xl"
              loading="lazy"
              decoding="async"
              fetchPriority="low"
            />
            <div className="absolute inset-0 bg-gradient-to-t from-black opacity-20 rounded-3xl pointer-events-none"></div>
          </div>

        </div>
      </div>
    </section>
  )
}

export default Hero
