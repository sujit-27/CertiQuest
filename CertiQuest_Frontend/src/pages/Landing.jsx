import React, { useEffect } from 'react';
import Hero from '../components/Landing/Hero';
import Features from '../components/Landing/Features';
import Testimonials from '../components/Landing/Testimonials';
import CTA from '../components/Landing/CTA';
import Footer from '../components/Landing/Footer';
import Pricing from '../components/Landing/Pricing';
import {features, testimonials, plans } from '../assets/data';
import { useClerk, useUser } from "@clerk/clerk-react";
import { useNavigate } from 'react-router-dom';

const Landing = () => {
  const { openSignUp } = useClerk();
  const { isSignedIn } = useUser();
  const navigate = useNavigate();

  useEffect(() => {
    if (isSignedIn) {
      navigate("/dashboard"); // everyone goes to the same dashboard
    }
  }, [isSignedIn, navigate]);

  return (
    <div className='landing-page bg-gradient-to-b from-gray-50 to-gray-100'>
      {/* Hero Section */}
      <Hero openSignUp={openSignUp} />

      {/* Features Section */}
      <Features features={features} />

      {/* Pricing Section */}
      <Pricing plans={plans} openSignUp={openSignUp} />

      {/* Testimonials Section */}
      <Testimonials testimonials={testimonials} />

      {/* CTA Section */}
      <CTA openSignUp={openSignUp} />

      {/* Footer Section */}
      <Footer />
    </div>
  );
};

export default Landing;
