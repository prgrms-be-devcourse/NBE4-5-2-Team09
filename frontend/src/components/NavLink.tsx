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
  // 현재 경로가 정확히 일치하면 활성화 상태로 간주
  const isActive = pathname === href;

  return (
    <Link
      href={href}
      className={`${
        isActive
          ? 'text-gray-900 font-medium border-b-2 border-blue-500 pb-1'
          : 'text-gray-500 hover:text-gray-900'
      }`}
    >
      {children}
    </Link>
  );
}
