import { apiRequest } from './client';
import { getStoredAuthSession } from './auth';
import type { GuestSessionCreateResponse } from './types';
import type { ProjectType } from './types';

const GUEST_SESSION_STORAGE_KEY = 'dev-web-ide.guest-session';
const PROJECT_SESSION_STORAGE_KEY_PREFIX = 'dev-web-ide.project-session.';
const TOKEN_EXPIRATION_SKEW_MS = 30 * 1000;

export type StoredGuestSession = GuestSessionCreateResponse;
export type ProjectApiSession = {
  accessToken: string;
  guestSessionId: number | null;
  userId: number | null;
};
type StoredProjectSessionOwner = {
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
  projectId?: number,
): Promise<ProjectApiSession> => {
  const storedProjectSession = resolveStoredProjectSession(projectId);

  if (storedProjectSession) {
    return storedProjectSession;
  }

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

export const saveProjectApiSession = (
  projectId: number,
  session: ProjectApiSession,
) => {
  window.localStorage.setItem(
    getProjectSessionStorageKey(projectId),
    JSON.stringify({
      guestSessionId: session.guestSessionId,
      userId: session.userId,
    } satisfies StoredProjectSessionOwner),
  );
};

const resolveStoredProjectSession = (projectId?: number) => {
  if (!projectId) {
    return null;
  }

  const rawSession = window.localStorage.getItem(
    getProjectSessionStorageKey(projectId),
  );

  if (!rawSession) {
    return null;
  }

  try {
    const parsedSession = JSON.parse(rawSession) as StoredProjectSessionOwner;

    if (parsedSession.userId) {
      const authSession = getStoredAuthSession();

      if (authSession?.userId === parsedSession.userId) {
        return {
          accessToken: authSession.accessToken,
          guestSessionId: null,
          userId: authSession.userId,
        } satisfies ProjectApiSession;
      }
    }

    if (parsedSession.guestSessionId) {
      const guestSession = getStoredGuestSession();

      if (guestSession?.guestSessionId === parsedSession.guestSessionId) {
        return {
          accessToken: guestSession.accessToken,
          guestSessionId: guestSession.guestSessionId,
          userId: null,
        } satisfies ProjectApiSession;
      }
    }
  } catch {
    window.localStorage.removeItem(getProjectSessionStorageKey(projectId));
  }

  return null;
};

const getProjectSessionStorageKey = (projectId: number) =>
  `${PROJECT_SESSION_STORAGE_KEY_PREFIX}${projectId}`;
