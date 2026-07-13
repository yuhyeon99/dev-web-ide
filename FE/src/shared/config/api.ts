const trimTrailingSlash = (value: string) => value.replace(/\/+$/, '');

export const API_BASE_URL = trimTrailingSlash(
  import.meta.env.VITE_API_BASE_URL,
);

const toWebSocketUrl = (value: string) => {
  if (value.startsWith('https://')) {
    return value.replace(/^https:\/\//, 'wss://');
  }

  if (value.startsWith('http://')) {
    return value.replace(/^http:\/\//, 'ws://');
  }

  return value;
};

export const REALTIME_WS_URL = `${trimTrailingSlash(
  import.meta.env.VITE_REALTIME_WS_URL ?? toWebSocketUrl(API_BASE_URL),
)}/ws`;
