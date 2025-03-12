// hooks/useBookmarkToggle.ts
import { useAuth } from '@/context/AuthContext';
import { MarketDto } from '@/types';

export const useBookmarkToggle = (market: MarketDto) => {
  const { accessToken } = useAuth();

  const handleBookmarkToggle = async () => {
    const { isBookmarked } = market;
    const endpoint = isBookmarked ? `/api/bookmark/${market.code}` : '/api/bookmark';
    const requestBody = { coinCode: market.code };

    try {
      const response = await fetch(process.env.NEXT_PUBLIC_API_URL + endpoint, {
        method: isBookmarked ? 'DELETE' : 'POST',
        headers: {
          'Content-Type': 'application/json',
          Authorization: `Bearer ${accessToken}`,
        },
        credentials: 'include',
        body: isBookmarked ? null : JSON.stringify(requestBody),
      });

      if (response.ok) {
        market.isBookmarked = !isBookmarked;
      } else {
        console.error('Failed to update bookmark');
      }
    } catch (error) {
      console.error('Error occurred while updating bookmark:', error);
    }
  };

  return handleBookmarkToggle;
};
