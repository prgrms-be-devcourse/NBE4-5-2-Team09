'use client';

import { Moon, Sun } from 'lucide-react';
import { useTheme } from 'next-themes';

import { Button } from '@/components/ui/button';
import { useState } from 'react';

export function ModeToggle() {
  const { theme, setTheme } = useTheme();
  const [icon, setIcon] = useState({ type: Moon });

  const toggleTheme = () => {
    setTheme((prev) => (prev === 'light' ? 'dark' : 'light'));
    if (theme === 'light') {
      setIcon({ type: Moon });
    } else {
      setIcon({ type: Sun });
    }
  };

  return (
    <Button variant="outline" size="icon" onClick={toggleTheme}>
      <icon.type className="h-4 w-4" />
    </Button>
  );
}
