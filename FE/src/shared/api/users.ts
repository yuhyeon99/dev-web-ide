import { apiRequest } from './client';
import type { UserSearchResponse } from './types';

export const searchUsers = (accessToken: string, query: string) => {
  const searchParams = new URLSearchParams({
    query,
  });

  return apiRequest<UserSearchResponse[]>(
    `/api/users/search?${searchParams.toString()}`,
    {
      accessToken,
    },
  );
};
