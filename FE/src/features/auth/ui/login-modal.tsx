import { useEffect, useState, type MouseEvent } from 'react';

type AuthMode = 'login' | 'signup';

type LoginModalProps = {
  isOpen: boolean;
  onClose: () => void;
};

type AuthField = {
  id: string;
  label: string;
  type: 'email' | 'password' | 'text';
  placeholder: string;
};

const loginFields: AuthField[] = [
  {
    id: 'login-email',
    label: '이메일',
    type: 'email',
    placeholder: 'you@workspace.dev',
  },
  {
    id: 'login-password',
    label: '비밀번호',
    type: 'password',
    placeholder: '영문, 숫자, 특수문자 조합',
  },
];

const signupFields: AuthField[] = [
  {
    id: 'signup-name',
    label: '이름',
    type: 'text',
    placeholder: '홍길동',
  },
  {
    id: 'signup-email',
    label: '이메일',
    type: 'email',
    placeholder: 'you@workspace.dev',
  },
  {
    id: 'signup-password',
    label: '비밀번호',
    type: 'password',
    placeholder: '8자 이상 입력',
  },
  {
    id: 'signup-password-confirm',
    label: '비밀번호 확인',
    type: 'password',
    placeholder: '비밀번호를 다시 입력',
  },
];

const oauthProviders = [
  {
    id: 'github',
    badge: 'GH',
    label: 'GitHub',
    description: '저장소와 연동해 빠르게 시작',
  },
  {
    id: 'google',
    badge: 'G',
    label: 'Google',
    description: '기존 Google 계정으로 로그인',
  },
  {
    id: 'kakao',
    badge: 'K',
    label: 'Kakao',
    description: '간편 가입과 소셜 로그인',
  },
] as const;

const handleDialogClick = (event: MouseEvent<HTMLDivElement>) => {
  event.stopPropagation();
};

const tabBaseClass =
  'flex-1 rounded-md px-3 py-2 text-sm font-medium transition focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-[#007acc]/70';

const inputClassName =
  'w-full rounded-md border border-[#3c3c3c] bg-[#1f1f1f] px-3 py-2.5 text-sm text-[#d4d4d4] transition placeholder:text-[#6b7280] focus:border-[#007acc] focus:outline-none focus:ring-2 focus:ring-[#007acc]/25';

export const LoginModal = ({ isOpen, onClose }: LoginModalProps) => {
  const [mode, setMode] = useState<AuthMode>('login');

  useEffect(() => {
    if (!isOpen) {
      return;
    }

    const previousOverflow = document.body.style.overflow;

    const handleEscape = (event: KeyboardEvent) => {
      if (event.key === 'Escape') {
        onClose();
      }
    };

    document.body.style.overflow = 'hidden';
    window.addEventListener('keydown', handleEscape);

    return () => {
      document.body.style.overflow = previousOverflow;
      window.removeEventListener('keydown', handleEscape);
    };
  }, [isOpen, onClose]);

  if (!isOpen) {
    return null;
  }

  const fields = mode === 'login' ? loginFields : signupFields;
  const isLoginMode = mode === 'login';

  return (
    <div
      className="fixed inset-0 z-[100] flex items-center justify-center bg-[rgba(0,0,0,0.6)] px-4 py-4 backdrop-blur-[2px] sm:py-6"
      onClick={onClose}
      role="presentation"
    >
      <div
        role="dialog"
        aria-modal="true"
        aria-labelledby="auth-modal-title"
        className="flex max-h-[calc(100dvh-2rem)] w-full max-w-2xl flex-col overflow-hidden rounded-2xl border border-[#313131] bg-[#1e1e1e] shadow-[0_28px_90px_rgba(0,0,0,0.58)] sm:max-h-[calc(100dvh-3rem)]"
        onClick={handleDialogClick}
      >
        <div className="flex items-center justify-between border-b border-[#313131] bg-[linear-gradient(180deg,#252526_0%,#1f1f1f_100%)] px-5 py-4">
          <div className="min-w-0">
            <div className="flex items-center gap-2">
              <span className="rounded-md border border-[#3c3c3c] bg-[#1e1e1e] px-2 py-1 text-[10px] font-semibold tracking-[0.2em] text-[#4fc1ff] uppercase">
                Account
              </span>
              <span className="text-xs text-[#858585]">VSCode Dark Theme</span>
            </div>
            <h2
              id="auth-modal-title"
              className="mt-2 text-lg font-semibold text-[#f3f3f3]"
            >
              Dev Web IDE에 로그인하고 작업을 이어가세요
            </h2>
          </div>

          <button
            type="button"
            onClick={onClose}
            className="inline-flex h-9 w-9 items-center justify-center rounded-md border border-transparent text-[#9da1a6] transition hover:border-[#3c3c3c] hover:bg-[#2a2d2e] hover:text-white focus-visible:ring-2 focus-visible:ring-[#007acc]/70 focus-visible:outline-none"
            aria-label="로그인 팝업 닫기"
          >
            <svg
              xmlns="http://www.w3.org/2000/svg"
              viewBox="0 0 24 24"
              fill="none"
              stroke="currentColor"
              strokeWidth="1.8"
              className="h-4 w-4"
            >
              <path
                strokeLinecap="round"
                strokeLinejoin="round"
                d="M6 6l12 12"
              />
              <path
                strokeLinecap="round"
                strokeLinejoin="round"
                d="M18 6L6 18"
              />
            </svg>
          </button>
        </div>

        <div className="min-h-0 overflow-y-auto">
          <section className="flex flex-col px-5 py-5 sm:px-6 sm:py-6">
            <div className="rounded-xl border border-[#313131] bg-[#252526] p-1">
              <div className="flex gap-1">
                <button
                  type="button"
                  onClick={() => setMode('login')}
                  className={`${tabBaseClass} ${
                    isLoginMode
                      ? 'bg-[#0e639c] text-white shadow-[inset_0_0_0_1px_rgba(255,255,255,0.08)]'
                      : 'text-[#9da1a6] hover:bg-[#2a2d2e] hover:text-[#d4d4d4]'
                  }`}
                >
                  로그인
                </button>
                <button
                  type="button"
                  onClick={() => setMode('signup')}
                  className={`${tabBaseClass} ${
                    isLoginMode
                      ? 'text-[#9da1a6] hover:bg-[#2a2d2e] hover:text-[#d4d4d4]'
                      : 'bg-[#0e639c] text-white shadow-[inset_0_0_0_1px_rgba(255,255,255,0.08)]'
                  }`}
                >
                  회원가입
                </button>
              </div>
            </div>

            <div className="mt-6">
              <h3 className="text-base font-semibold text-[#f3f3f3]">
                {isLoginMode ? '기존 계정으로 로그인' : '새 계정 만들기'}
              </h3>
              <p className="mt-1 text-sm leading-6 text-[#9da1a6]">
                {isLoginMode
                  ? '저장된 프로젝트와 팀 작업 공간을 이어서 열 수 있습니다.'
                  : '회원가입 후 프로젝트 저장, 팀 초대, 클라우드 동기화를 사용할 수 있습니다.'}
              </p>
            </div>

            <form className="mt-6 flex flex-1 flex-col">
              <div className="space-y-4">
                {fields.map((field) => (
                  <label key={field.id} htmlFor={field.id} className="block">
                    <span className="mb-2 block text-sm font-medium text-[#cccccc]">
                      {field.label}
                    </span>
                    <input
                      id={field.id}
                      type={field.type}
                      placeholder={field.placeholder}
                      className={inputClassName}
                    />
                  </label>
                ))}
              </div>

              {isLoginMode ? (
                <div className="mt-4 flex flex-col gap-3 text-sm text-[#9da1a6] sm:flex-row sm:items-center sm:justify-between">
                  <label className="inline-flex items-center gap-2">
                    <input
                      type="checkbox"
                      className="h-4 w-4 rounded border-[#3c3c3c] bg-[#1f1f1f] accent-[#007acc]"
                    />
                    로그인 상태 유지
                  </label>
                  <button
                    type="button"
                    className="text-left text-[#4fc1ff] transition hover:text-[#7fd7ff]"
                  >
                    비밀번호를 잊으셨나요?
                  </button>
                </div>
              ) : (
                <label className="mt-4 inline-flex items-start gap-2 text-sm leading-6 text-[#9da1a6]">
                  <input
                    type="checkbox"
                    className="mt-1 h-4 w-4 rounded border-[#3c3c3c] bg-[#1f1f1f] accent-[#007acc]"
                  />
                  <span>
                    이용약관과 개인정보 처리방침에 동의하고 OAuth 기반 추가 인증
                    절차가 연결될 수 있음을 확인합니다.
                  </span>
                </label>
              )}

              <div className="mt-6 space-y-3">
                <button
                  type="button"
                  className="inline-flex w-full items-center justify-center rounded-md border border-[#1177bb] bg-[#0e639c] px-4 py-3 text-sm font-semibold text-white transition hover:bg-[#1177bb] focus-visible:ring-2 focus-visible:ring-[#007acc]/70 focus-visible:outline-none"
                >
                  {isLoginMode ? '이메일로 로그인' : '이메일로 회원가입'}
                </button>
                <button
                  type="button"
                  className="inline-flex w-full items-center justify-center rounded-md border border-[#3c3c3c] bg-[#252526] px-4 py-3 text-sm font-medium text-[#d4d4d4] transition hover:bg-[#2a2d2e] focus-visible:ring-2 focus-visible:ring-[#007acc]/50 focus-visible:outline-none"
                >
                  게스트 모드로 둘러보기
                </button>
              </div>

              <div className="my-6 flex items-center gap-3 text-xs text-[#6b7280]">
                <div className="h-px flex-1 bg-[#313131]" />
                <span className="shrink-0 tracking-[0.16em] uppercase">
                  OAuth
                </span>
                <div className="h-px flex-1 bg-[#313131]" />
              </div>

              <div className="grid gap-3 sm:grid-cols-3">
                {oauthProviders.map((provider) => (
                  <button
                    key={provider.id}
                    type="button"
                    className="group rounded-xl border border-[#313131] bg-[#252526] px-4 py-4 text-left transition hover:border-[#0e639c] hover:bg-[#202224] focus-visible:ring-2 focus-visible:ring-[#007acc]/60 focus-visible:outline-none"
                  >
                    <div className="flex items-center gap-3">
                      <span className="inline-flex h-10 w-10 items-center justify-center rounded-lg border border-[#3c3c3c] bg-[#1e1e1e] text-sm font-semibold text-[#4fc1ff]">
                        {provider.badge}
                      </span>
                      <div>
                        <p className="text-sm font-semibold text-[#f3f3f3]">
                          {provider.label}
                        </p>
                        <p className="mt-1 text-xs leading-5 text-[#858585]">
                          {provider.description}
                        </p>
                      </div>
                    </div>
                  </button>
                ))}
              </div>

              <div className="mt-5 rounded-xl border border-[#313131] bg-[#111111] px-4 py-3">
                <p className="text-xs font-medium text-[#6a9955]">
                  퍼블리싱 전용 상태입니다. 실제 OAuth API, 토큰 처리,
                  가입/로그인 요청은 이후 연결하면 됩니다.
                </p>
              </div>

              <p className="mt-5 text-center text-sm text-[#858585]">
                {isLoginMode ? '아직 계정이 없나요?' : '이미 계정이 있나요?'}{' '}
                <button
                  type="button"
                  onClick={() => setMode(isLoginMode ? 'signup' : 'login')}
                  className="font-medium text-[#4fc1ff] transition hover:text-[#7fd7ff]"
                >
                  {isLoginMode ? '회원가입으로 이동' : '로그인으로 이동'}
                </button>
              </p>
            </form>
          </section>
        </div>
      </div>
    </div>
  );
};
