import { API_BASE_URL } from '@/shared/config/api';

const AUTH_SESSION_STORAGE_KEY = 'dev-web-ide.auth-session';
const OAUTH_SIGNUP_STORAGE_KEY = 'dev-web-ide.oauth-signup';

type CompleteGoogleOAuthSignupParams = {
  nickname: string;
  termsAgreed: boolean;
  privacyAgreed: boolean;
};

export type StoredAuthSession = {
  accessToken: string;
  tokenType: string;
  expiresIn: number;
  accessTokenExpiresAt: string;
  userId: number;
  email: string;
  nickname: string;
  role: string;
  status: string;
  newUser: boolean;
};

export type PendingOAuthSignup = {
  provider: 'google';
  email: string;
  name: string;
};

const isStoredAuthSession = (value: unknown): value is StoredAuthSession => {
  if (!value || typeof value !== 'object') {
    return false;
  }

  const session = value as Partial<StoredAuthSession>;

  return (
    typeof session.accessToken === 'string' &&
    typeof session.tokenType === 'string' &&
    typeof session.expiresIn === 'number' &&
    typeof session.accessTokenExpiresAt === 'string' &&
    typeof session.userId === 'number' &&
    typeof session.email === 'string' &&
    typeof session.nickname === 'string' &&
    typeof session.role === 'string' &&
    typeof session.status === 'string' &&
    typeof session.newUser === 'boolean'
  );
};

export const startGoogleOAuth = () => {
  window.location.assign(`${API_BASE_URL}/api/auth/oauth/google/authorize`);
};

export const getStoredAuthSession = () => {
  const rawSession = window.localStorage.getItem(AUTH_SESSION_STORAGE_KEY);

  if (!rawSession) {
    return null;
  }

  try {
    const parsedSession = JSON.parse(rawSession) as unknown;

    if (!isStoredAuthSession(parsedSession)) {
      return null;
    }

    if (new Date(parsedSession.accessTokenExpiresAt).getTime() <= Date.now()) {
      window.localStorage.removeItem(AUTH_SESSION_STORAGE_KEY);
      return null;
    }

    return parsedSession;
  } catch {
    window.localStorage.removeItem(AUTH_SESSION_STORAGE_KEY);
    return null;
  }
};

export const saveAuthSession = (session: StoredAuthSession) => {
  window.localStorage.setItem(
    AUTH_SESSION_STORAGE_KEY,
    JSON.stringify(session),
  );
};

export const clearAuthSession = () => {
  window.localStorage.removeItem(AUTH_SESSION_STORAGE_KEY);
};

export const getPendingOAuthSignup = () => {
  const rawSignup = window.localStorage.getItem(OAUTH_SIGNUP_STORAGE_KEY);

  if (!rawSignup) {
    return null;
  }

  try {
    const parsedSignup = JSON.parse(rawSignup) as Partial<PendingOAuthSignup>;

    if (
      parsedSignup.provider !== 'google' ||
      typeof parsedSignup.email !== 'string' ||
      typeof parsedSignup.name !== 'string'
    ) {
      return null;
    }

    return parsedSignup as PendingOAuthSignup;
  } catch {
    window.localStorage.removeItem(OAUTH_SIGNUP_STORAGE_KEY);
    return null;
  }
};

export const clearPendingOAuthSignup = () => {
  window.localStorage.removeItem(OAUTH_SIGNUP_STORAGE_KEY);
};

export const completeGoogleOAuthSignup = async ({
  nickname,
  termsAgreed,
  privacyAgreed,
}: CompleteGoogleOAuthSignupParams) => {
  const response = await fetch(`${API_BASE_URL}/api/auth/oauth/google/signup`, {
    method: 'POST',
    credentials: 'include',
    headers: {
      Accept: 'application/json',
      'Content-Type': 'application/json',
    },
    body: JSON.stringify({
      nickname,
      termsAgreed,
      privacyAgreed,
    }),
  });

  if (!response.ok) {
    throw new Error(
      `Google 회원가입에 실패했습니다. status=${response.status}`,
    );
  }

  const session = (await response.json()) as StoredAuthSession;

  saveAuthSession(session);
  clearPendingOAuthSignup();

  return session;
};

export const consumeOAuthRedirect = () => {
  const searchParams = new URLSearchParams(window.location.search);

  if (!searchParams.has('oauth')) {
    return null;
  }

  if (searchParams.get('oauth') === 'signup_required') {
    window.localStorage.setItem(
      OAUTH_SIGNUP_STORAGE_KEY,
      JSON.stringify({
        provider: 'google',
        email: searchParams.get('email') ?? '',
        name: searchParams.get('name') ?? '',
      } satisfies PendingOAuthSignup),
    );
    removeOAuthRedirectParams();
    return null;
  }

  if (searchParams.get('oauth') !== 'success') {
    clearPendingOAuthSignup();
    removeOAuthRedirectParams();
    return null;
  }

  const accessToken = searchParams.get('accessToken');
  const tokenType = searchParams.get('tokenType');
  const expiresIn = Number(searchParams.get('expiresIn'));
  const accessTokenExpiresAt = searchParams.get('accessTokenExpiresAt');
  const userId = Number(searchParams.get('userId'));
  const email = searchParams.get('email');
  const nickname = searchParams.get('nickname');
  const role = searchParams.get('role');
  const status = searchParams.get('status');
  const newUser = searchParams.get('newUser') === 'true';

  if (
    !accessToken ||
    !tokenType ||
    !Number.isFinite(expiresIn) ||
    !accessTokenExpiresAt ||
    !Number.isFinite(userId) ||
    !email ||
    !nickname ||
    !role ||
    !status
  ) {
    removeOAuthRedirectParams();
    return null;
  }

  const session: StoredAuthSession = {
    accessToken,
    tokenType,
    expiresIn,
    accessTokenExpiresAt,
    userId,
    email,
    nickname,
    role,
    status,
    newUser,
  };

  saveAuthSession(session);
  removeOAuthRedirectParams();

  return session;
};

const removeOAuthRedirectParams = () => {
  const url = new URL(window.location.href);

  [
    'oauth',
    'reason',
    'provider',
    'name',
    'accessToken',
    'tokenType',
    'expiresIn',
    'accessTokenExpiresAt',
    'userId',
    'email',
    'nickname',
    'role',
    'status',
    'newUser',
  ].forEach((key) => url.searchParams.delete(key));

  window.history.replaceState(
    null,
    '',
    `${url.pathname}${url.search}${url.hash}`,
  );
};
