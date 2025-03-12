import { create } from 'zustand';

interface UserProfile {
  name: string;
  email: string;
}

interface UserState {
  user: UserProfile;
  setUser: (user: UserProfile) => void;
  deleteUser: () => void;
}

const defaultState: UserProfile = { name: '게스트', email: '' };

export const useUserStore = create<UserState>((set) => ({
  user: defaultState,
  setUser: (user: UserProfile) => set({ user }),
  deleteUser: () => set({ user: defaultState }),
}));
