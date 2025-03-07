import Link from "next/link";

export default function Header() {
  return (
    <header className="bg-white border-b border-gray-200">
      <div className="container mx-auto px-4 py-4">
        <div className="flex justify-between items-center">
          <div className="flex items-center space-x-8">
            <nav className="hidden md:flex space-x-8">
              <Link
                href="/dashboard"
                className="text-gray-900 font-medium border-b-2 border-blue-500 pb-1"
              >
                코인 대시보드
              </Link>
              <Link
                href="/market"
                className="text-gray-500 hover:text-gray-900"
              >
                북마크 대시보드
              </Link>
              <Link href="/etc" className="text-gray-500 hover:text-gray-900">
                기타 메뉴
              </Link>
            </nav>
          </div>
          <div className="flex items-center space-x-4">
            <span className="text-sm text-gray-500">최근 업데이트: 5초 전</span>
            <div className="flex items-center">
              <span className="ml-2 text-sm font-medium">홍길동님</span>
            </div>
            <button className="bg-blue-500 hover:bg-blue-600 text-white px-4 py-2 rounded-md text-sm">
              로그아웃
            </button>
          </div>
        </div>
      </div>
    </header>
  );
}
