import { createSlice } from "@reduxjs/toolkit";

const roleSlice = createSlice({
  name: "role",
  initialState: {
    selectedRole: null,
  },
  reducers: {
    setRole: (state, action) => {
      state.selectedRole = action.payload;
    },
    clearRole: (state) => {
      state.selectedRole = null;
    },
  },
});

export const { setRole, clearRole } = roleSlice.actions;
export default roleSlice.reducer;
