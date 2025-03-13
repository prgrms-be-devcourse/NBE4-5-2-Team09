'use client';

import { ThemeProvider as NextThemesProvider } from 'next-themes';
import { useState, useEffect } from 'react';

export function ThemeProvider({
  children,
  ...props
}: React.ComponentProps<typeof NextThemesProvider>) {
  const [isMount, setMount] = useState(false);

  useEffect(() => {
    setMount(true);
  }, []);

  if (!isMount) {
    return null;
  }
  return <NextThemesProvider {...props}>{children}</NextThemesProvider>;
}
