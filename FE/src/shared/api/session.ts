import { apiRequest } from './client';
import { getStoredAuthSession } from './auth';
import type { GuestSessionCreateResponse } from './types';
import type { ProjectType } from './types';

const GUEST_SESSION_STORAGE_KEY = 'dev-web-ide.guest-session';
const TOKEN_EXPIRATION_SKEW_MS = 30 * 1000;

export type StoredGuestSession = GuestSessionCreateResponse;
export type ProjectApiSession = {
  accessToken: string;
  guestSessionId: number | null;
  userId: number | null;
};

const isExpired = (expiresAt: string) => {
  const expiresAtMs = new Date(expiresAt).getTime();

  return (
    !Number.isFinite(expiresAtMs) ||
    expiresAtMs <= Date.now() + TOKEN_EXPIRATION_SKEW_MS
  );
};

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

    if (
      isExpired(parsedSession.accessTokenExpiresAt) ||
      isExpired(parsedSession.expiresAt)
    ) {
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

export const resolveProjectApiSession = async (
  projectType?: ProjectType,
): Promise<ProjectApiSession> => {
  const authSession = getStoredAuthSession();

  if (authSession && projectType !== 'GUEST') {
    return {
      accessToken: authSession.accessToken,
      guestSessionId: null,
      userId: authSession.userId,
    };
  }

  const guestSession = await ensureGuestSession();

  return {
    accessToken: guestSession.accessToken,
    guestSessionId: guestSession.guestSessionId,
    userId: null,
  };
};
