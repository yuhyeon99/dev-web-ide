import { useEffect, useState, type MouseEvent } from 'react';

type AuthStep = 'oauth' | 'profile';

type AuthModalProps = {
  isOpen: boolean;
  onClose: () => void;
};

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
    description: '기존 Google 계정으로 바로 이어서 사용',
  },
  {
    id: 'kakao',
    badge: 'K',
    label: 'Kakao',
    description: '간편 가입과 로그인 흐름에 적합',
  },
] as const;

type OAuthProviderId = (typeof oauthProviders)[number]['id'];

const handleDialogClick = (event: MouseEvent<HTMLDivElement>) => {
  event.stopPropagation();
};

const inputClassName =
  'w-full rounded-md border border-[#3c3c3c] bg-[#1f1f1f] px-3 py-2.5 text-sm text-[#d4d4d4] transition placeholder:text-[#6b7280] focus:border-[#007acc] focus:outline-none focus:ring-2 focus:ring-[#007acc]/25';

const checkboxClassName =
  'mt-0.5 h-4 w-4 shrink-0 rounded border-[#3c3c3c] bg-[#1f1f1f] accent-[#007acc]';

export const AuthModal = ({ isOpen, onClose }: AuthModalProps) => {
  const [step, setStep] = useState<AuthStep>('oauth');
  const [selectedProviderId, setSelectedProviderId] =
    useState<OAuthProviderId | null>(null);
  const [nickname, setNickname] = useState('');
  const [agreedToTerms, setAgreedToTerms] = useState(false);
  const [agreedToPrivacy, setAgreedToPrivacy] = useState(false);

  const resetModalState = () => {
    setStep('oauth');
    setSelectedProviderId(null);
    setNickname('');
    setAgreedToTerms(false);
    setAgreedToPrivacy(false);
  };

  const handleClose = () => {
    resetModalState();
    onClose();
  };

  useEffect(() => {
    if (!isOpen) {
      return;
    }

    const previousOverflow = document.body.style.overflow;

    const handleEscape = (event: KeyboardEvent) => {
      if (event.key === 'Escape') {
        setStep('oauth');
        setSelectedProviderId(null);
        setNickname('');
        setAgreedToTerms(false);
        setAgreedToPrivacy(false);
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

  const isOAuthStep = step === 'oauth';
  const selectedProvider = oauthProviders.find(
    (provider) => provider.id === selectedProviderId,
  );
  const isProfileComplete =
    nickname.trim().length > 0 && agreedToTerms && agreedToPrivacy;

  const handleProviderSelect = (providerId: OAuthProviderId) => {
    setSelectedProviderId(providerId);
    setStep('profile');
  };

  const handleBack = () => {
    resetModalState();
  };

  return (
    <div
      className="fixed inset-0 z-[100] flex items-center justify-center bg-[rgba(0,0,0,0.6)] px-4 py-4 backdrop-blur-[2px] sm:py-6"
      onClick={handleClose}
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
                Login / Signup
              </span>
              <span className="text-xs text-[#858585]">VSCode Dark Theme</span>
            </div>
            <h2
              id="auth-modal-title"
              className="mt-2 text-lg font-semibold text-[#f3f3f3]"
            >
              {isOAuthStep
                ? '소셜 계정으로 로그인하거나 회원가입하세요'
                : '추가 정보를 입력하고 가입을 완료하세요'}
            </h2>
          </div>

          <button
            type="button"
            onClick={handleClose}
            className="inline-flex h-9 w-9 items-center justify-center rounded-md border border-transparent text-[#9da1a6] transition hover:border-[#3c3c3c] hover:bg-[#2a2d2e] hover:text-white focus-visible:ring-2 focus-visible:ring-[#007acc]/70 focus-visible:outline-none"
            aria-label="인증 팝업 닫기"
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
            {isOAuthStep ? (
              <div>
                <div>
                  <h3 className="text-base font-semibold text-[#f3f3f3]">
                    하나의 OAuth 진입점으로 로그인과 회원가입을 함께 처리합니다
                  </h3>
                  <p className="mt-1 text-sm leading-6 text-[#9da1a6]">
                    기존 사용자는 인증 후 바로 로그인되고, 신규 사용자는 같은
                    팝업 안에서 닉네임과 약관 동의만 입력해 회원가입을
                    마무리하는 흐름입니다.
                  </p>
                </div>

                <div className="mt-5 flex flex-wrap gap-2">
                  <span className="rounded-md border border-[#3c3c3c] bg-[#252526] px-3 py-1.5 text-xs font-medium text-[#d4d4d4]">
                    기존 회원은 바로 로그인
                  </span>
                  <span className="rounded-md border border-[#3c3c3c] bg-[#252526] px-3 py-1.5 text-xs font-medium text-[#d4d4d4]">
                    신규 회원은 추가 정보 입력 후 가입
                  </span>
                </div>

                <div className="mt-6 grid gap-3">
                  {oauthProviders.map((provider) => (
                    <button
                      key={provider.id}
                      type="button"
                      onClick={() => handleProviderSelect(provider.id)}
                      className="group rounded-xl border border-[#313131] bg-[#252526] px-4 py-4 text-left transition hover:border-[#0e639c] hover:bg-[#202224] focus-visible:ring-2 focus-visible:ring-[#007acc]/60 focus-visible:outline-none"
                    >
                      <div className="flex items-center gap-3">
                        <span className="inline-flex h-11 w-11 shrink-0 items-center justify-center rounded-lg border border-[#3c3c3c] bg-[#1e1e1e] text-sm font-semibold text-[#4fc1ff]">
                          {provider.badge}
                        </span>
                        <div className="min-w-0 flex-1">
                          <p className="text-sm font-semibold text-[#f3f3f3]">
                            {provider.label}로 로그인 / 회원가입
                          </p>
                          <p className="mt-1 text-xs leading-5 text-[#858585]">
                            {provider.description}
                          </p>
                        </div>
                        <span className="text-sm text-[#6b7280] transition group-hover:text-[#d4d4d4]">
                          시작
                        </span>
                      </div>
                    </button>
                  ))}
                </div>

                <div className="mt-6 rounded-xl border border-[#313131] bg-[#161616] px-4 py-4">
                  <p className="text-[11px] font-semibold tracking-[0.18em] text-[#4fc1ff] uppercase">
                    Account Flow
                  </p>
                  <div className="mt-3 grid gap-3 sm:grid-cols-2">
                    <div className="rounded-lg border border-[#2d2d30] bg-[#1e1e1e] px-4 py-3">
                      <p className="text-sm font-semibold text-[#d4d4d4]">
                        기존 사용자
                      </p>
                      <p className="mt-1 text-xs leading-5 text-[#858585]">
                        OAuth 인증이 끝나면 별도 입력 없이 바로 로그인 완료 처리
                      </p>
                    </div>
                    <div className="rounded-lg border border-[#2d2d30] bg-[#1e1e1e] px-4 py-3">
                      <p className="text-sm font-semibold text-[#d4d4d4]">
                        신규 사용자
                      </p>
                      <p className="mt-1 text-xs leading-5 text-[#858585]">
                        닉네임과 약관 동의만 입력받는 추가 정보 단계로 이동
                      </p>
                    </div>
                  </div>
                </div>

                <button
                  type="button"
                  className="mt-6 inline-flex w-full items-center justify-center rounded-md border border-[#3c3c3c] bg-[#252526] px-4 py-3 text-sm font-medium text-[#d4d4d4] transition hover:bg-[#2a2d2e] focus-visible:ring-2 focus-visible:ring-[#007acc]/50 focus-visible:outline-none"
                >
                  게스트 모드로 둘러보기
                </button>
              </div>
            ) : (
              <div className="mt-6">
                <div className="rounded-xl border border-[#313131] bg-[#161616] px-4 py-4">
                  <div className="flex items-center gap-3">
                    <span className="inline-flex h-10 w-10 shrink-0 items-center justify-center rounded-lg border border-[#3c3c3c] bg-[#1e1e1e] text-sm font-semibold text-[#4fc1ff]">
                      {selectedProvider?.badge}
                    </span>
                    <div>
                      <p className="text-sm font-semibold text-[#f3f3f3]">
                        {selectedProvider?.label} 계정 인증 완료
                      </p>
                      <p className="mt-1 text-xs leading-5 text-[#858585]">
                        이 상태에서 신규 사용자라면 추가 정보 입력 후 회원가입이
                        완료되는 흐름입니다.
                      </p>
                    </div>
                  </div>
                </div>

                <form className="mt-6">
                  <div>
                    <h3 className="text-base font-semibold text-[#f3f3f3]">
                      서비스에서 사용할 프로필을 설정하세요
                    </h3>
                    <p className="mt-1 text-sm leading-6 text-[#9da1a6]">
                      비밀번호 없이 닉네임과 약관 동의만으로 가입을 마무리하는
                      구조입니다.
                    </p>
                  </div>

                  <label htmlFor="signup-nickname" className="mt-6 block">
                    <span className="mb-2 block text-sm font-medium text-[#cccccc]">
                      닉네임
                    </span>
                    <input
                      id="signup-nickname"
                      type="text"
                      value={nickname}
                      onChange={(event) => setNickname(event.target.value)}
                      placeholder="워크스페이스에서 표시할 이름"
                      className={inputClassName}
                    />
                  </label>

                  <div className="mt-5 space-y-3 rounded-xl border border-[#313131] bg-[#161616] p-4">
                    <label className="flex items-start gap-3 text-sm leading-6 text-[#9da1a6]">
                      <input
                        type="checkbox"
                        checked={agreedToTerms}
                        onChange={(event) =>
                          setAgreedToTerms(event.target.checked)
                        }
                        className={checkboxClassName}
                      />
                      <span>
                        이용약관에 동의합니다.
                        <span className="ml-2 text-[#4fc1ff]">(필수)</span>
                      </span>
                    </label>
                    <label className="flex items-start gap-3 text-sm leading-6 text-[#9da1a6]">
                      <input
                        type="checkbox"
                        checked={agreedToPrivacy}
                        onChange={(event) =>
                          setAgreedToPrivacy(event.target.checked)
                        }
                        className={checkboxClassName}
                      />
                      <span>
                        개인정보 처리방침에 동의합니다.
                        <span className="ml-2 text-[#4fc1ff]">(필수)</span>
                      </span>
                    </label>
                  </div>

                  <div className="mt-6 flex flex-col gap-3 sm:flex-row">
                    <button
                      type="button"
                      onClick={handleBack}
                      className="inline-flex w-full items-center justify-center rounded-md border border-[#3c3c3c] bg-[#252526] px-4 py-3 text-sm font-medium text-[#d4d4d4] transition hover:bg-[#2a2d2e] focus-visible:ring-2 focus-visible:ring-[#007acc]/50 focus-visible:outline-none"
                    >
                      이전
                    </button>
                    <button
                      type="button"
                      disabled={!isProfileComplete}
                      className={`inline-flex w-full items-center justify-center rounded-md px-4 py-3 text-sm font-semibold transition focus-visible:ring-2 focus-visible:ring-[#007acc]/70 focus-visible:outline-none ${
                        isProfileComplete
                          ? 'border border-[#1177bb] bg-[#0e639c] text-white hover:bg-[#1177bb]'
                          : 'cursor-not-allowed border border-[#3c3c3c] bg-[#2a2a2a] text-[#6b7280]'
                      }`}
                    >
                      가입 완료
                    </button>
                  </div>

                  <div className="mt-5 rounded-xl border border-[#313131] bg-[#111111] px-4 py-3">
                    <p className="text-xs font-medium text-[#6a9955]">
                      퍼블리싱 전용 상태입니다. 실제 연동 시에는 OAuth 인증
                      결과로 기존 회원/신규 회원을 분기한 뒤 신규 회원만 이
                      단계로 전환하면 됩니다.
                    </p>
                  </div>
                </form>
              </div>
            )}
          </section>
        </div>
      </div>
    </div>
  );
};
