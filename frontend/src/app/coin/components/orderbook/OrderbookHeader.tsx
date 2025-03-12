'use client';
import { Switch } from '@/components/ui/switch';
import { Label } from '@/components/ui/label';

interface OrderbookHeaderProps {
  isTotalMode: boolean;
  handleToggleMode: () => void;
  quote: string;
  base: string;
}

export function OrderbookHeader({
  isTotalMode,
  handleToggleMode,
  quote,
  base,
}: OrderbookHeaderProps) {
  return (
    <div className="p-4 border-b border-muted flex justify-between items-center">
      <h2 className="text-lg font-semibold">호가 정보</h2>
      <div className="flex items-center gap-2">
        <Label htmlFor="toggle-switch">{isTotalMode ? `총액(${quote})` : `수량(${base})`}</Label>
        <Switch
          id="toggle-switch"
          checked={isTotalMode}
          onCheckedChange={handleToggleMode}
          className="w-10 h-6 data-[state=checked]:bg-muted-foreground data-[state=unchecked]:bg-gray-300"
        />
      </div>
    </div>
  );
}
