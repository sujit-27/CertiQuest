import { createSlice, createAsyncThunk } from '@reduxjs/toolkit';
import axios from 'axios';
import { fetchUserPoints } from './pointsSlice';

const plans = [
  {
    id: "premium",
    name: "Premium",
    points: 500,
    price: 99,
    features: [
      "Access to 500+ quizzes with all difficulty levels",
      "Personalized quiz recommendations",
      "Progress tracking & detailed analytics",
      "Printable certificates for completed quizzes",
      "Priority email support",
      "Create more quizzes"
    ],
    recommended: false
  },
  {
    id: "ultimate",
    name: "Ultimate",
    points: 5000,
    price: 299,
    features: [
      "Unlimited quizzes & categories",
      "Custom quiz creation & sharing",
      "AI-powered adaptive quizzes",
      "Advanced performance analytics",
      "Downloadable premium certificates",
      "24/7 priority support"
    ],
    recommended: true
  }
];


// Async thunk for payment initiation & verification
export const initiatePayment = createAsyncThunk(
  'payment/initiatePayment',
  async ({ plan, name, email, getToken, isSignedIn }, { dispatch, rejectWithValue }) => {
    try {
      const token = await getToken();
      const response = await axios.post("http://certiquest.up.railway.app/api/payments/create-order", {
        planId: plan.id,
        amount: plan.price * 100,
        currency: "INR",
        points: plan.points
      }, {
        headers: { 'Authorization': `Bearer ${token}` }
        ,withCredentials: true,
      });

      // Prepare Razorpay options
      return {
        plan, token, orderId: response.data.orderId, name, email
      };
    } catch (error) {
      return rejectWithValue("Failed to initiate payment. Please try again later.");
    }
  }
);

export const verifyPayment = createAsyncThunk(
  'payment/verifyPayment',
  async ({ response, plan, getToken, isSignedIn }, { dispatch, rejectWithValue }) => {
    try {
      const token = await getToken();
      const verifyResponse = await axios.post('http://certiquest.up.railway.app/api/payments/verify-payment', {
        razorpay_order_id: response.razorpay_order_id,
        razorpay_payment_id: response.razorpay_payment_id,
        razorpay_signature: response.razorpay_signature,
        planId: plan.id
      }, {
        headers: { 'Authorization': `Bearer ${token}` },
        withCredentials: true,
      });

      if (verifyResponse.data.success) {
        if (verifyResponse.data.points != null) {
            dispatch({ type: 'points/updatePoints', payload: verifyResponse.data.points });
        } else {
            dispatch(fetchUserPoints({ getToken, isSignedIn }));
        }
        return { success: true, planName: plan.name };
      } else {
            return rejectWithValue("Payment verification failed. Please contact support.");
      }
    } catch (error) {
        return rejectWithValue("Payment verification failed. Please contact support.");
    }
  }
);

const paymentSlice = createSlice({
  name: 'payment',
  initialState: {
    processingPayment: false,
    message: '',
    messageType: '',
    plans: plans,
    razorpayLoaded: false,
    razorScriptAttached: false
  },
  reducers: {
    setMessage: (state, action) => {
      state.message = action.payload.message;
      state.messageType = action.payload.type;
    },
    setRazorpayLoaded: (state, action) => {
      state.razorpayLoaded = action.payload;
    }
  },
  extraReducers: builder => {
    builder
      .addCase(initiatePayment.pending, (state) => {
        state.processingPayment = true;
        state.message = "";
        state.messageType = "";
      })
      .addCase(initiatePayment.fulfilled, (state) => {
        state.processingPayment = false;
      })
      .addCase(initiatePayment.rejected, (state, action) => {
        state.processingPayment = false;
        state.message = action.payload;
        state.messageType = "error";
      })
      .addCase(verifyPayment.pending, (state) => {
        state.processingPayment = true;
      })
      .addCase(verifyPayment.fulfilled, (state, action) => {
        state.processingPayment = false;
        state.message = `Payment successful! ${action.payload.planName} plan activated.`;
        state.messageType = "success";
      })
      .addCase(verifyPayment.rejected, (state, action) => {
        state.processingPayment = false;
        state.message = action.payload;
        state.messageType = "error";
      });
  }
});

export const { setMessage, setRazorpayLoaded } = paymentSlice.actions;
export default paymentSlice.reducer;
