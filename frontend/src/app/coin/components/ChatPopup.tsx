'use client';

import React, { useState, useRef, useEffect, MouseEvent } from 'react';
import ChatPanel from './ChatPanel';

interface ChatPopupProps {
    marketCode: string;
    onClose: () => void;
}

const ChatPopup: React.FC<ChatPopupProps> = ({ marketCode, onClose }) => {
    const [position, setPosition] = useState({ x: 100, y: 100 });
    const [size, setSize] = useState({ width: 350, height: 500 });
    const [dragging, setDragging] = useState(false);
    const [resizing, setResizing] = useState(false);
    const [dragOffset, setDragOffset] = useState({ x: 0, y: 0 });
    const modalRef = useRef<HTMLDivElement>(null);

    const handleDragMouseDown = (e: MouseEvent<HTMLDivElement>) => {
        setDragging(true);
        setDragOffset({
            x: e.clientX - position.x,
            y: e.clientY - position.y,
        });
        e.stopPropagation();
    };

    const handleResizeMouseDown = (e: MouseEvent<HTMLDivElement>) => {
        setResizing(true);
        e.stopPropagation();
        e.preventDefault();
    };

    useEffect(() => {
        const handleMouseMove = (e: MouseEvent<Document>) => {
            if (dragging) {
                setPosition({
                    x: e.clientX - dragOffset.x,
                    y: e.clientY - dragOffset.y,
                });
            } else if (resizing) {
                setSize({
                    width: Math.max(300, e.clientX - position.x),
                    height: Math.max(200, e.clientY - position.y),
                });
            }
        };

        const handleMouseUp = () => {
            setDragging(false);
            setResizing(false);
        };

        document.addEventListener('mousemove', handleMouseMove as any);
        document.addEventListener('mouseup', handleMouseUp);
        return () => {
            document.removeEventListener('mousemove', handleMouseMove as any);
            document.removeEventListener('mouseup', handleMouseUp);
        };
    }, [dragging, resizing, dragOffset, position.x, position.y]);

    return (
        <div
            ref={modalRef}
            style={{
                position: 'fixed',
                top: position.y,
                left: position.x,
                width: size.width,
                height: size.height,
                zIndex: 1000,
            }}
        >
            <div className="flex flex-col h-full bg-card shadow-lg rounded-md overflow-hidden">
                {/* 헤더: Tailwind 클래스로 색상 지정 */}
                <div
                    onMouseDown={handleDragMouseDown}
                    className="flex items-center justify-between bg-primary text-primary-foreground p-3 cursor-move select-none dark:text-black dark:bg-primary-dark"
                >
                    <span>채팅방 ({marketCode})</span>
                    <button
                        onClick={onClose}
                        className="text-xl bg-transparent border-none cursor-pointer dark:text-black dark:bg-primary-dark"
                        aria-label="Close"
                    >
                        ✕
                    </button>
                </div>
                {/* 채팅 내용 영역 */}
                <div className="flex-1 overflow-auto p-3">
                    <ChatPanel marketCode={marketCode} />
                </div>
                {/* 리사이즈 핸들 */}
                <div
                    onMouseDown={handleResizeMouseDown}
                    className="absolute bottom-0 right-0 w-4 h-4 cursor-nwse-resize"
                />
            </div>
        </div>
    );
};

export default ChatPopup;
