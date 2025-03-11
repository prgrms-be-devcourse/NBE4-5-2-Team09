import ClientPage from '@/app/coin/[market]/ClientPage';
import WebSocketProvider from '@/context/WebSocketContext';

export default function Page() {
  return (
    <>
      <WebSocketProvider
        subscriptions={[
          { type: 'ticker', markets: [] },
          { type: 'trade', markets: [] },
          { type: 'orderbook', markets: [] },
        ]}
      >
        <ClientPage />
      </WebSocketProvider>
    </>
  );
}
