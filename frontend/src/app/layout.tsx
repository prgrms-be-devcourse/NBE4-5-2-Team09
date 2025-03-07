import type React from "react";
import type { Metadata } from "next";
import { Inter } from "next/font/google";
import "./globals.css";
import { SocketProvider } from "@/components/providers/socket-provider";
import { ThemeProvider } from "@/components/providers/theme-provider";
import Header from "@/components/header";
import { MultiValueProvider } from "@/components/providers/multivalue-provider";

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
        <SocketProvider>
          <MultiValueProvider>
            {/* <ThemeProvider attribute="class" defaultTheme="system" enableSystem> */}
            <Header />
            <main className="container mx-auto px-4 py-8">{children}</main>
            {/* </ThemeProvider> */}
          </MultiValueProvider>
        </SocketProvider>
      </body>
    </html>
  );
}
