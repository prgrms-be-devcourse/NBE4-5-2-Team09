import type React from "react";
import type { Metadata } from "next";
import { Inter } from "next/font/google";
import "@/app/globals.css";
import Header from "@/app/components/Header";
import { AuthProvider } from "@/app/context/auth-context";

const inter = Inter({ subsets: ["latin"] });

export const metadata: Metadata = {
  title: "Coing",
  description: "Real-time Coin Dashboard",
};

export default function RootLayout({
                                     children,
                                   }: {
  children: React.ReactNode;
}) {
  return (
      <html lang="en">
      <body className={inter.className}>
      <AuthProvider>
        <Header />
        <main className="container mx-auto px-4 py-8">{children}</main>
      </AuthProvider>
      </body>
      </html>
  );
}
