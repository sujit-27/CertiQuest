import {LayoutDashboard, UploadCloudIcon, Files, CreditCardIcon, Receipt, Brain, Layers, Award, BarChart, Users, Clock } from 'lucide-react';
import {FileText,Trophy, PlusCircle } from 'lucide-react';
import testimonial1 from "../assets/testimonials/testimonial1.jpeg"
import testimonial2 from "../assets/testimonials/testimonial2.jpeg"
import testimonial3 from "../assets/testimonials/testimonial3.jpeg"

export const features = [
    {
        icon: Brain,
        title: "AI-Generated Questions",
        description: "Get unique, category-based quiz questions generated instantly with AI."
    },
    {
        icon: Layers,
        title: "Custom Quizzes",
        description: "Create quizzes tailored to specific topics, categories, and difficulty levels."
    },
    {
        icon: Award,
        title: "Certification",
        description: "Earn verifiable certificates upon quiz completion to showcase your skills."
    },
    {
        icon: BarChart,
        title: "Performance Tracking",
        description: "Track your progress, view detailed results, and analyze your strengths."
    },
    {
        icon: Users,
        title: "Collaborative Learning",
        description: "Invite friends or colleagues to take quizzes and learn together."
    },
    {
        icon: Clock,
        title: "Timed Quizzes",
        description: "Challenge yourself with time-bound quizzes for a real exam experience."
    },
]

export const testimonials = [
    {
        image: testimonial1,
        name: "Ananya Sharma",
        role: "Student",
        company: "Kalyani Government Engineering College",
        rating: 4.5,
        quote: "CertiQuest made exam prep so much easier! The AI-generated questions matched my syllabus perfectly and helped me gain confidence before tests."
    },
    {
        image: testimonial2,
        name: "Rahul Mehta",
        role: "Software Engineer",
        company: "TechSolutions Ltd.",
        rating: 5,
        quote: "I love how CertiQuest creates quizzes instantly based on categories. The performance tracking and certifications are a big boost to my career growth."
    },
    {
        image: testimonial3,
        name: "Priya Verma",
        role: "Teacher",
        company: "Springfield High School",
        rating: 4,
        quote: "As a teacher, I use CertiQuest to quickly generate quizzes for my students. It saves me hours of work and keeps students engaged with varied questions."
    },
]

export const SIDE_MENU_DATA = [
  {
    id: 1,
    label: "Dashboard",
    icon: LayoutDashboard,
    path: "/dashboard",
  },
  {
    id: 2,
    label: "Leaderboard",
    icon: Trophy,
    path: "/leaderboard",
  },
  {
    id: 3,
    label: "Certificates",
    icon: FileText,
    path: "/certificates",
  },
  {
    id: 4,
    label: "Subscription",
    icon: CreditCardIcon,
    path: "/subscriptions",
  },
  {
    id: 5,
    label: "Transactions",
    icon: Receipt,
    path: "/transactions",
  },
];

export const plans = [
  {
    name: "Free",
    planKey: "free",
    description: "Perfect for learners exploring quizzes and learning the basics.",
    price: 0,
    limits: { maxQuizzes: 50, difficulty: "standard" },
    features: [
      { featureIndex: 1, feature: "Access to 50+ quizzes across categories" },
      { featureIndex: 2, feature: "Standard difficulty levels" },
      { featureIndex: 3, feature: "Score tracking & feedback" },
      { featureIndex: 4, feature: "Community support" },
      { featureIndex: 5, feature: "Create limited quizzes" }, // merged admin feature
    ],
    cta: "Get Started",
  },
  {
    name: "Premium",
    planKey: "premium",
    description: "For learners who want in-depth practice and advanced features.",
    price: 99,
    limits: { maxQuizzes: 500 },
    highlighted: true,
    features: [
      { featureIndex: 1, feature: "Access to 500+ quizzes with all difficulty levels" },
      { featureIndex: 2, feature: "Personalized quiz recommendations" },
      { featureIndex: 3, feature: "Progress tracking & detailed analytics" },
      { featureIndex: 4, feature: "Printable certificates for completed quizzes" },
      { featureIndex: 5, feature: "Priority email support" },
      { featureIndex: 6, feature: "Create more quizzes" }, // merged admin feature
    ],
    cta: "Go Premium",
  },
  {
    name: "Ultimate",
    planKey: "ultimate",
    description: "Tailored for professionals who want complete access to quizzes and creation features.",
    price: 299,
    limits: { maxQuizzes: Infinity },
    features: [
      { featureIndex: 1, feature: "Unlimited quizzes & categories" },
      { featureIndex: 2, feature: "Custom quiz creation & sharing" },
      { featureIndex: 3, feature: "AI-powered adaptive quizzes" },
      { featureIndex: 4, feature: "Advanced performance analytics" },
      { featureIndex: 5, feature: "Downloadable premium certificates" },
      { featureIndex: 6, feature: "24/7 priority support" },
    ],
    cta: "Go Ultimate",
  },
];
