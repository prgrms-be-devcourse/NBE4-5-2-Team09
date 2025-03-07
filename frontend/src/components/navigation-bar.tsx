"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";
import {
  Home,
  Info,
  Phone,
  LayoutDashboard,
  User,
  Moon,
  Sun,
} from "lucide-react";
import { useTheme } from "next-themes";
import { useEffect, useState } from "react";
import { Button } from "@/components/ui/button";
import {
  NavigationMenu,
  NavigationMenuItem,
  NavigationMenuList,
} from "@/components/ui/navigation-menu";

export function Navbar() {
  const pathname = usePathname();
  const { setTheme, theme } = useTheme();
  const [mounted, setMounted] = useState(false);

  const currentTheme = mounted ? theme : "light";

  useEffect(() => {
    setMounted(true);
  }, []);

  const navItems = [
    { name: "Home", href: "/", icon: Home },
    { name: "Dashboard", href: "/dashboard", icon: LayoutDashboard },
  ];

  return (
    <nav className="border-b">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="flex justify-between h-16 items-center">
          <div className="text-xl font-bold">MyApp</div>

          <NavigationMenu>
            <NavigationMenuList className="hidden sm:flex space-x-4">
              {navItems.map((item) => {
                const isActive = pathname === item.href;
                return (
                  <NavigationMenuItem key={item.href}>
                    <Link
                      href={item.href}
                      className={`flex items-center px-3 py-2 text-sm font-medium rounded-md transition ${
                        isActive
                          ? "bg-primary/10 text-primary"
                          : "text-muted-foreground hover:bg-secondary"
                      }`}
                    >
                      <item.icon className="mr-2 h-4 w-4" />
                      {item.name}
                    </Link>
                  </NavigationMenuItem>
                );
              })}
            </NavigationMenuList>
          </NavigationMenu>

          {mounted && (
            <Button
              variant="ghost"
              size="icon"
              onClick={() =>
                setTheme(currentTheme === "light" ? "dark" : "light")
              }
            >
              <Sun
                className={`h-[1.2rem] w-[1.2rem] transition-all ${
                  theme === "dark" ? "-rotate-90 scale-0" : "rotate-0 scale-100"
                }`}
              />
              <Moon
                className={`absolute h-[1.2rem] w-[1.2rem] transition-all ${
                  theme === "dark" ? "rotate-0 scale-100" : "rotate-90 scale-0"
                }`}
              />
              <span className="sr-only">Toggle theme</span>
            </Button>
          )}
        </div>
      </div>
    </nav>
  );
}
