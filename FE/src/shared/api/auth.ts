import { API_BASE_URL } from '@/shared/config/api';

const AUTH_SESSION_STORAGE_KEY = 'dev-web-ide.auth-session';

type StartGoogleOAuthParams = {
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

export const startGoogleOAuth = ({
  nickname,
  termsAgreed,
  privacyAgreed,
}: StartGoogleOAuthParams) => {
  const params = new URLSearchParams({
    nickname,
    termsAgreed: String(termsAgreed),
    privacyAgreed: String(privacyAgreed),
  });

  window.location.assign(
    `${API_BASE_URL}/api/auth/oauth/google/authorize?${params.toString()}`,
  );
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

export const consumeOAuthRedirect = () => {
  const searchParams = new URLSearchParams(window.location.search);

  if (!searchParams.has('oauth')) {
    return null;
  }

  if (searchParams.get('oauth') !== 'success') {
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
