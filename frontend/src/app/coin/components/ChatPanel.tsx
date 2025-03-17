'use client';

import React, { useEffect, useState, useRef, KeyboardEvent } from 'react';
import { useWebSocket } from '@/context/WebSocketContext';
import { Input } from '@/components/ui/input';

interface ChatMessage {
  id?: number; // 메시지 식별자 (백엔드에서 부여)
  sender: string;
  content: string;
  timestamp: string;
}

interface ChatPanelProps {
  marketCode: string;
}

const ChatPanel: React.FC<ChatPanelProps> = ({ marketCode }) => {
  const { chatMessages, updateSubscriptions, publishMessage } = useWebSocket();
  const [input, setInput] = useState<string>('');
  const [cachedMessages, setCachedMessages] = useState<ChatMessage[]>([]);
  const messagesEndRef = useRef<HTMLDivElement>(null);

  // 채팅 구독 업데이트: marketCode 변경 시 구독 설정
  useEffect(() => {
    updateSubscriptions([{ type: 'chat', markets: [marketCode] }]);
  }, [marketCode]);

  // 컴포넌트 마운트 시 캐시된 채팅 기록 불러오기
  useEffect(() => {
    const loadCachedMessages = async () => {
      try {
        const res = await fetch(
          `${process.env.NEXT_PUBLIC_API_URL}/api/chat/rooms/${marketCode}/messages`,
        );
        if (res.ok) {
          const data = await res.json();
          setCachedMessages(data);
        } else {
          console.error('캐시된 채팅 기록 불러오기 실패');
        }
      } catch (error) {
        console.error('채팅 기록 불러오기 오류:', error);
      }
    };
    loadCachedMessages();
  }, [marketCode]);

  // 새로운 메시지가 추가되면 자동 스크롤
  useEffect(() => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  }, [chatMessages[marketCode], cachedMessages]);

  const sendMessage = () => {
    if (!input.trim()) return;
    // 세션 스토리지에서 액세스 토큰 읽기
    const token = sessionStorage.getItem('accessToken');
    const message: ChatMessage = {
      sender: '', // 백엔드에서 JWT 디코딩 후 설정됨
      content: input,
      timestamp: '', // 백엔드에서 타임스탬프 추가 예정
    };
    // 토큰이 있을 경우 Authorization 헤더에 포함시킴
    publishMessage(
      `/app/chat/${marketCode}`,
      JSON.stringify(message),
      token ? { Authorization: `Bearer ${token}` } : {},
    );
    setInput('');
  };

  const handleKeyDown = (e: KeyboardEvent<HTMLInputElement>) => {
    if (e.key === 'Enter') {
      sendMessage();
    }
  };

  // 실시간 메시지와 캐시된 메시지 합치기 (중복 제거)
  const realtimeMessages: ChatMessage[] = chatMessages[marketCode] || [];
  const allMessages: ChatMessage[] = Array.from(
    new Map(
      [...cachedMessages, ...realtimeMessages].map((msg) => [
        `${msg.timestamp}-${msg.content}`,
        msg,
      ]),
    ).values(),
  );

  return (
    <div className="flex flex-col h-full">
      <div className="flex-1 overflow-y-auto p-2 border-b border-border">
        {allMessages.map((msg, index) => (
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
