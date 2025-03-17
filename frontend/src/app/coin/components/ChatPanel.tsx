'use client';

import React, { useEffect, useState, useRef, KeyboardEvent } from 'react';
import { useWebSocket } from '@/context/WebSocketContext';
import { useWebSocketStore } from '@/store/web-socket.store';
import { Input } from '@/components/ui/input';

interface ChatMessage {
    sender: string;
    content: string;
    timestamp: string;
}

interface ChatPanelProps {
    marketCode: string;
}

const ChatPanel: React.FC<ChatPanelProps> = ({ marketCode }) => {
    const { chatMessages, updateSubscriptions, publishMessage } = useWebSocket();
    const { publish } = useWebSocketStore();
    const [input, setInput] = useState<string>('');
    const messagesEndRef = useRef<HTMLDivElement>(null);

    useEffect(() => {
        updateSubscriptions([{ type: 'chat', markets: [marketCode] }]);
    }, [marketCode]);

    useEffect(() => {
        messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
    }, [chatMessages[marketCode]]);

    const sendMessage = () => {
        if (!input.trim()) return;
        const message: ChatMessage = {
            sender: '', // 프론트엔드에서는 빈 문자열로 전송 (백엔드에서 JWT 디코딩 후 설정)
            content: input,
            timestamp: '', // 백엔드에서 타임스탬프 추가
        };
        publishMessage(`/app/chat/${marketCode}`, JSON.stringify(message));
        setInput('');
    };

    const handleKeyDown = (e: KeyboardEvent<HTMLInputElement>) => {
        if (e.key === 'Enter') {
            sendMessage();
        }
    };

    const messages: ChatMessage[] = chatMessages[marketCode] || [];

    return (
        <div className="flex flex-col h-full">
            <div className="flex-1 overflow-y-auto p-2 border-b border-border">
                {messages.map((msg, index) => (
                    <div key={index} className="mb-2">
                        <strong>{msg.sender}:</strong> {msg.content}
                        <div className="text-xs text-muted-foreground">
                            {msg.timestamp ? new Date(parseInt(msg.timestamp)).toLocaleTimeString() : ''}
                        </div>
                    </div>
                ))}
                <div ref={messagesEndRef} />
            </div>
            <div className="flex items-center p-2 border-t border-border">
                <Input
                    type="text"
                    placeholder="메시지 입력"
                    value={input}
                    onChange={(e) => setInput(e.target.value)}
                    onKeyDown={handleKeyDown}
                    className="flex-1 mr-2"
                />
                <button
                    onClick={sendMessage}
                    className="bg-primary text-primary-foreground rounded-md px-4 py-2 transition-colors hover:bg-primary/90 dark:text-black dark:bg-primary-dark"
                >
                    전송
                </button>
            </div>
        </div>
    );
};

export default ChatPanel;
