import { apiRequest } from './client';
import type { RuntimeResponse } from './types';

export const getRuntimes = () => {
  return apiRequest<RuntimeResponse[]>('/api/runtimes');
};
