import { API_BASE_URL } from '@/shared/config/api';

type ApiRequestOptions = NonNullable<Parameters<typeof fetch>[1]> & {
  accessToken?: string;
};

export class ApiError extends Error {
  status: number;
  body: unknown;

  constructor(message: string, status: number, body: unknown) {
    super(message);
    this.name = 'ApiError';
    this.status = status;
    this.body = body;
  }
}

const parseResponseBody = async (response: Response) => {
  const text = await response.text();

  if (!text) {
    return null;
  }

  try {
    return JSON.parse(text) as unknown;
  } catch {
    return text;
  }
};

export const apiRequest = async <T>(
  path: string,
  { accessToken, headers, ...options }: ApiRequestOptions = {},
): Promise<T> => {
  const response = await fetch(`${API_BASE_URL}${path}`, {
    ...options,
    headers: {
      Accept: 'application/json',
      ...(options.body ? { 'Content-Type': 'application/json' } : {}),
      ...(accessToken ? { Authorization: `Bearer ${accessToken}` } : {}),
      ...headers,
    },
  });
  const body = await parseResponseBody(response);

  if (!response.ok) {
    throw new ApiError(
      `API 요청에 실패했습니다. status=${response.status}`,
      response.status,
      body,
    );
  }

  return body as T;
};
