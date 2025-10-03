import React from 'react'

const Footer = () => {
  return (
    <>
      <footer className='bg-gray-800'>
        <div className='max-w-7xl mx-auto py-8 px-4 sm:px-6 lg:px-8'>
          <div className='text-center'>
            <p className='text-base text-gray-400'>
              &copy; {new Date().getFullYear()} CertiQuest. All rights reserved.
            </p>
            <p className='mt-2 text-sm text-gray-500'>
              Empowering learners with AI-powered quizzes and certifications.
            </p>
          </div>
        </div>
      </footer>
    </>
  )
}

export default Footer
