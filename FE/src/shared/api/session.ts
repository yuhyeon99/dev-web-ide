import { apiRequest } from './client';
import type { GuestSessionCreateResponse } from './types';

const GUEST_SESSION_STORAGE_KEY = 'dev-web-ide.guest-session';

export type StoredGuestSession = GuestSessionCreateResponse;

const isStoredGuestSession = (value: unknown): value is StoredGuestSession => {
  if (!value || typeof value !== 'object') {
    return false;
  }

  const session = value as Partial<StoredGuestSession>;

  return (
    typeof session.guestSessionId === 'number' &&
    typeof session.accessToken === 'string' &&
    typeof session.expiresAt === 'string'
  );
};

export const getStoredGuestSession = () => {
  const rawSession = window.localStorage.getItem(GUEST_SESSION_STORAGE_KEY);

  if (!rawSession) {
    return null;
  }

  try {
    const parsedSession = JSON.parse(rawSession) as unknown;

    if (!isStoredGuestSession(parsedSession)) {
      return null;
    }

    if (new Date(parsedSession.expiresAt).getTime() <= Date.now()) {
      window.localStorage.removeItem(GUEST_SESSION_STORAGE_KEY);
      return null;
    }

    return parsedSession;
  } catch {
    window.localStorage.removeItem(GUEST_SESSION_STORAGE_KEY);
    return null;
  }
};

export const saveGuestSession = (session: StoredGuestSession) => {
  window.localStorage.setItem(
    GUEST_SESSION_STORAGE_KEY,
    JSON.stringify(session),
  );
};

export const createGuestSession = async () => {
  const session = await apiRequest<GuestSessionCreateResponse>(
    '/api/guest-sessions',
    {
      method: 'POST',
    },
  );

  saveGuestSession(session);

  return session;
};

export const ensureGuestSession = async () => {
  const storedSession = getStoredGuestSession();

  if (storedSession) {
    return storedSession;
  }

  return createGuestSession();
};
