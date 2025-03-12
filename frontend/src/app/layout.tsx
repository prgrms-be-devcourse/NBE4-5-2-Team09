import type React from 'react';
import type { Metadata } from 'next';
import { Inter } from 'next/font/google';
import '@/app/globals.css';
import { AuthProvider } from '@/context/AuthContext';
import Header from '@/components/Header';
import { ThemeProvider } from '@/context/ThemeContext';

const inter = Inter({ subsets: ['latin'] });

export const metadata: Metadata = {
  title: 'Coing',
  description: 'Real-time Coin Dashboard',
  icons: '/logo.svg',
};

export default function RootLayout({ children }: { children: React.ReactNode }) {
  return (
    <html lang="en" suppressHydrationWarning>
      <body className={inter.className}>
        <ThemeProvider
          attribute="class"
          defaultTheme="system"
          enableSystem
          disableTransitionOnChange
        >
          <AuthProvider>
            <Header />
            <main className="container mx-auto px-4 py-8">{children}</main>
          </AuthProvider>
        </ThemeProvider>
      </body>
    </html>
  );
}
