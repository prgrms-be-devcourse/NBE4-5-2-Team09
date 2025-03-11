import React from 'react';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select';
import {
  Pagination,
  PaginationContent,
  PaginationItem,
  PaginationPrevious,
  PaginationLink,
  PaginationNext,
  PaginationEllipsis,
} from '@/components/ui/pagination';

interface PaginationComponentProps {
  currentPage: number;
  totalPages: number;
  maxPageButtons?: number;
  onPageChange: (page: number) => void;
  size: number;
  onSizeChange: (size: number) => void;
  totalElements: number;
  pageSizeList: number[];
}

export default function PaginationComponent({
  currentPage,
  totalPages,
  maxPageButtons = 5,
  onPageChange,
  size,
  onSizeChange,
  totalElements,
  pageSizeList,
}: PaginationComponentProps) {
  const currentPageGroup = Math.floor((currentPage - 1) / maxPageButtons);
  const startPage = currentPageGroup * maxPageButtons + 1;
  const endPage = Math.min(startPage + maxPageButtons - 1, totalPages);

  return (
    <div className="w-full flex flex-wrap items-center justify-between mt-6 gap-6">
      <div>
        <Select value={String(size)} onValueChange={(value) => onSizeChange(Number(value))}>
          <SelectTrigger className="w-20 rounded-lg bg-white cursor-pointer">
            <SelectValue>{size}개</SelectValue>
          </SelectTrigger>
          <SelectContent className="bg-white shadow-md rounded-lg">
            {pageSizeList.map((num) => (
              <SelectItem
                key={num}
                value={String(num)}
                className="px-4 py-2 transition rounded-md cursor-pointer"
              >
                {num}개
              </SelectItem>
            ))}
          </SelectContent>
        </Select>
      </div>

      <div>
        <Pagination>
          <PaginationContent>
            <PaginationItem>
              <PaginationPrevious
                className="cursor-pointer"
                onClick={() => onPageChange(Math.max(1, startPage - maxPageButtons))}
              />
            </PaginationItem>
            {startPage > 1 && (
              <PaginationItem>
                <PaginationEllipsis />
              </PaginationItem>
            )}
            {Array.from({ length: endPage - startPage + 1 }, (_, i) => startPage + i).map(
              (pageNum) => (
                <PaginationItem key={pageNum}>
                  <PaginationLink
                    className={
                      currentPage === pageNum
                        ? 'bg-primary text-primary-foreground px-3 py-1 rounded-md'
                        : 'px-3 py-1 cursor-pointer'
                    }
                    onClick={() => onPageChange(pageNum)}
                  >
                    {pageNum}
                  </PaginationLink>
                </PaginationItem>
              ),
            )}
            {endPage < totalPages && (
              <PaginationItem>
                <PaginationEllipsis />
              </PaginationItem>
            )}
            <PaginationItem>
              <PaginationNext
                className="cursor-pointer"
                onClick={() => onPageChange(Math.min(totalPages, startPage + maxPageButtons))}
              />
            </PaginationItem>
          </PaginationContent>
        </Pagination>
      </div>

      <div>
        <p className="text-primary text-sm">총 {totalElements}개 코인</p>
      </div>
    </div>
  );
}
