import ClientPage from "@/app/coin/[market]/ClientPage";
import WebSocketProvider from "@/app/context/WebSocketContext";

export default function Page() {
  return (
    <WebSocketProvider subscriptions={[{ type: "ticker", markets: [] }]}>
      <ClientPage />
    </WebSocketProvider>
  );
}
