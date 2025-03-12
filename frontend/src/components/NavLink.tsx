'use client';

import Link from 'next/link';
import { usePathname } from 'next/navigation';
import React from 'react';

interface NavLinkProps {
  href: string;
  children: React.ReactNode;
}

export default function NavLink({ href, children }: NavLinkProps) {
  const pathname = usePathname();
  const isActive = pathname === href;

  return (
    <Link
      href={href}
      className={`flex gap-2 ${
        isActive
          ? 'text-card-foreground font-medium border-b-2 border-primary pb-1'
          : 'text-primary hover:text-card-foreground'
      }`}
    >
      {children}
    </Link>
  );
}
