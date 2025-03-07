"use client";
import { createContext, useContext, useState, ReactNode } from "react";

interface MultiValueContextType {
  values: Record<string, any>;
  setValues: (vals: Record<string, any>) => void;
}

const MultiValueContext = createContext<MultiValueContextType | undefined>(
  undefined
);

export function MultiValueProvider({ children }: { children: ReactNode }) {
  const [values, setValues] = useState<Record<string, any>>({});
  return (
    <MultiValueContext.Provider value={{ values, setValues }}>
      {children}
    </MultiValueContext.Provider>
  );
}

export function useMultiValue() {
  const context = useContext(MultiValueContext);
  if (!context) {
    throw new Error("useMultiValue must be used within a MultiValueProvider");
  }
  return context;
}
