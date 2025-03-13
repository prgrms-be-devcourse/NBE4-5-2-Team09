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

const storedUser =
  typeof window !== 'undefined' ? JSON.parse(sessionStorage.getItem('user') || 'null') : null;

export const defaultState: UserProfile = { name: '게스트', email: '' };

export const useUserStore = create<UserState>((set) => ({
  user: storedUser || defaultState,
  setUser: (user: UserProfile) => {
    set({ user });
    if (typeof window !== 'undefined') {
      sessionStorage.setItem('user', JSON.stringify(user));
    }
  },
  deleteUser: () => {
    set({ user: defaultState });
    if (typeof window !== 'undefined') {
      sessionStorage.removeItem('user');
    }
  },
}));
