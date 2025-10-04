import { Check } from "lucide-react";
import React from "react";
import { plans } from "../../assets/data";

const Pricing = ({ openSignUp }) => {
  return (
    <div className="py-15 bg-white">
      <div className="max-w-7xl mx-auto px-4 sm:px-5 ld:px-8">
        <div className="text-left mb-12">
          <h2 className="text-3xl font-extrabold text-gray-900 sm:text-4xl">
            Simple, Transparent Pricing
          </h2>
          <p className="mt-4 max-w-2xl text-xl text-gray-500">
            Choose the plan that suits your learning and quiz creation needs.
          </p>
        </div>

        {/* Pricing Cards */}
        <div className="mt-16 space-y-2 lg:space-y-0 lg:grid lg:grid-cols-3 lg:gap-8">
          {plans.map((plan, index) => (
            <div
              key={index}
              className={`flex flex-col rounded-lg shadow-lg overflow-hidden ${
                plan.highlighted
                  ? "border-2 border-purple-800 transform scale-105"
                  : "border border-gray-200"
              }`}
            >
              <div
                className={`px-6 py-8 ${
                  plan.highlighted ? "bg-gradient-to-br from-purple-100 to-white" : "bg-white"
                }`}
              >
                <div className="flex justify-between items-center">
                  <h3 className="text-2xl font-medium text-gray-900">{plan.name}</h3>
                  {plan.highlighted && (
                    <span className="inline-flex items-center px-3 py-0.5 rounded-full text-sm font-medium bg-purple-100 text-purple-900">
                      Popular
                    </span>
                  )}
                </div>
                <p className="mt-4 text-sm text-gray-500">{plan.description}</p>
                <p className="mt-8">
                  <span className="text-4xl font-extrabold text-gray-800">₹{plan.price}</span>
                </p>
              </div>

              <div className="flex-1 flex flex-col justify-between px-6 pt-6 pb-8 bg-gray-50 space-y-6">
                <ul className="space-y-4">
                  {plan.features.map((feature, featureIndex) => (
                    <li key={featureIndex} className="flex items-start">
                      <div className="flex-shrink-0">
                        <Check className="h-5 w-5 text-purple-600" />
                      </div>
                      <p className="ml-3 text-base text-gray-700">{feature.feature}</p>
                    </li>
                  ))}
                </ul>
                <div className="rounded-md shadow">
                  <button
                    onClick={() => openSignUp()}
                    className={`w-full flex items-center justify-center px-5 py-3 border border-transparent font-semibold text-base rounded-md ${
                      plan.highlighted
                        ? "text-white bg-purple-700 hover:bg-purple-500"
                        : "text-purple-800 bg-white hover:bg-gray-100 border-purple-700"
                    } transition-colors duration-200 cursor-pointer`}
                  >
                    {plan.cta}
                  </button>
                </div>
              </div>
            </div>
          ))}
        </div>
      </div>
    </div>
  );
};

export default Pricing;
