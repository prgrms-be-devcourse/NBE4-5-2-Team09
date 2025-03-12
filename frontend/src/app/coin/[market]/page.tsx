import ClientPage from '@/app/coin/[market]/ClientPage';
import WebSocketProvider from '@/context/WebSocketContext';

export default function Page() {
  return (
    <>
      <WebSocketProvider
        subscriptions={[{ type: 'ticker' }, { type: 'trade' }, { type: 'orderbook' }]}
      >
        <ClientPage />
      </WebSocketProvider>
    </>
  );
}
