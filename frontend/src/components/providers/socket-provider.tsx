"use client";

import React, { createContext, useContext, useEffect } from "react";
import {
  connectSocket,
  disconnect,
  subscribe,
  unsubscribe,
  sendMessage,
} from "@/lib/api/socket";

interface SocketContextType {
  subscribe: typeof subscribe;
  unsubscribe: typeof unsubscribe;
  sendMessage: typeof sendMessage;
}

const SocketContext = createContext<SocketContextType>({
  subscribe,
  unsubscribe,
  sendMessage,
});

export function SocketProvider({ children }: { children: React.ReactNode }) {
  useEffect(() => {
    connectSocket();

    return () => {
      disconnect();
    };
  }, []);

  const value: SocketContextType = {
    subscribe,
    unsubscribe,
    sendMessage,
  };

  return (
    <SocketContext.Provider value={value}>{children}</SocketContext.Provider>
  );
}

export function useSocket() {
  return useContext(SocketContext);
}
