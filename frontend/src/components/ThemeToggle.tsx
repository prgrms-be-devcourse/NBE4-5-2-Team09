'use client';

import { Moon, Sun } from 'lucide-react';
import { useTheme } from 'next-themes';

import { Button } from '@/components/ui/button';
import { useEffect, useState } from 'react';

export function ModeToggle() {
  const { theme, setTheme } = useTheme();

  const toggleTheme = () => {
    setTheme(theme === 'dark' ? 'light' : 'dark');
  };

  return (
    <Button variant="outline" size="icon" onClick={toggleTheme}>
      <Sun className={`${theme == 'dark' ? 'hidden' : ''} h-4 w-4`} />
      <Moon className={`${theme == 'light' ? 'hidden' : ''} h-4 w-4`} />
    </Button>
  );
}
