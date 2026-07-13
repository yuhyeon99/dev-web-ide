import { useMemo, useState, type FormEvent } from 'react';
import { useNavigate } from 'react-router';

import {
  completeGoogleOAuthSignup,
  getPendingOAuthSignup,
  getStoredAuthSession,
  startGoogleOAuth,
  updateAuthenticatedProfile,
} from '@/shared/api/auth';

export const ProfileSetupPage = () => {
  const navigate = useNavigate();
  const pendingSignup = useMemo(() => getPendingOAuthSignup(), []);
  const authSession = useMemo(() => getStoredAuthSession(), []);
  const [nickname, setNickname] = useState(
    () => pendingSignup?.name ?? authSession?.nickname ?? '',
  );
  const [agreedToTerms, setAgreedToTerms] = useState(false);
  const [agreedToPrivacy, setAgreedToPrivacy] = useState(false);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);
  const [isSubmitting, setSubmitting] = useState(false);
  const isSignupCompletion = Boolean(pendingSignup);
  const isSubmitDisabled =
    nickname.trim().length === 0 ||
    isSubmitting ||
    (isSignupCompletion && (!agreedToTerms || !agreedToPrivacy));

  const handleSubmit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();

    if (isSubmitDisabled) {
      return;
    }

    setSubmitting(true);
    setErrorMessage(null);

    try {
      if (pendingSignup) {
        await completeGoogleOAuthSignup({
          nickname: nickname.trim(),
          privacyAgreed: agreedToPrivacy,
          signupToken: pendingSignup.signupToken,
          termsAgreed: agreedToTerms,
        });
      } else if (authSession) {
        await updateAuthenticatedProfile(authSession, nickname.trim());
      }

      navigate('/');
    } catch {
      setErrorMessage('닉네임을 저장하지 못했습니다. 잠시 후 다시 시도하세요.');
      setSubmitting(false);
    }
  };

  if (!pendingSignup && !authSession) {
    return (
      <div className="flex h-[calc(100vh-2.75rem)] items-center justify-center bg-[#1e1e1e] px-4 text-[#d4d4d4]">
        <section className="w-full max-w-md rounded-lg border border-[#313131] bg-[#252526] p-6">
          <h1 className="text-xl font-semibold text-[#f3f3f3]">
            로그인이 필요합니다
          </h1>
          <p className="mt-2 text-sm leading-6 text-[#9da1a6]">
            닉네임을 설정하려면 Google 계정으로 먼저 로그인하세요.
          </p>
          <button
            type="button"
            onClick={startGoogleOAuth}
            className="mt-5 inline-flex w-full items-center justify-center rounded-md bg-[#0e639c] px-4 py-3 text-sm font-semibold text-white transition hover:bg-[#1177bb]"
          >
            Google로 로그인 / 회원가입
          </button>
        </section>
      </div>
    );
  }

  return (
    <div className="flex h-[calc(100vh-2.75rem)] items-center justify-center bg-[#1e1e1e] px-4 text-[#d4d4d4]">
      <section className="w-full max-w-lg rounded-lg border border-[#313131] bg-[#252526] p-6">
        <p className="text-[11px] font-semibold tracking-[0.18em] text-[#4fc1ff] uppercase">
          Profile
        </p>
        <h1 className="mt-2 text-2xl font-semibold text-[#f3f3f3]">
          서비스에서 사용할 닉네임을 설정하세요
        </h1>
        <p className="mt-2 text-sm leading-6 text-[#9da1a6]">
          {pendingSignup?.email ?? authSession?.email}
        </p>

        <form className="mt-6" onSubmit={handleSubmit}>
          <label className="block">
            <span className="text-sm font-medium text-[#cccccc]">닉네임</span>
            <input
              type="text"
              value={nickname}
              onChange={(event) => setNickname(event.target.value)}
              maxLength={50}
              placeholder="닉네임을 입력하세요"
              className="mt-2 w-full rounded-lg border border-[#3c3c3c] bg-[#1f1f1f] px-3 py-2.5 text-sm text-[#d4d4d4] transition placeholder:text-[#6b7280] focus:border-[#007acc] focus:ring-2 focus:ring-[#007acc]/25 focus:outline-none"
            />
          </label>

          {isSignupCompletion ? (
            <div className="mt-5 space-y-3 rounded-lg border border-[#313131] bg-[#161616] p-4">
              <label className="flex items-start gap-3 text-sm leading-6 text-[#9da1a6]">
                <input
                  type="checkbox"
                  checked={agreedToTerms}
                  onChange={(event) => setAgreedToTerms(event.target.checked)}
                  className="mt-0.5 h-4 w-4 shrink-0 rounded border-[#3c3c3c] bg-[#1f1f1f] accent-[#007acc]"
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
                  onChange={(event) => setAgreedToPrivacy(event.target.checked)}
                  className="mt-0.5 h-4 w-4 shrink-0 rounded border-[#3c3c3c] bg-[#1f1f1f] accent-[#007acc]"
                />
                <span>
                  개인정보 처리방침에 동의합니다.
                  <span className="ml-2 text-[#4fc1ff]">(필수)</span>
                </span>
              </label>
            </div>
          ) : null}

          {errorMessage ? (
            <p className="mt-4 rounded-md border border-[#f85149]/30 bg-[#3b1f1f] px-3 py-2 text-sm text-[#ffb4ac]">
              {errorMessage}
            </p>
          ) : null}

          <div className="mt-6 flex flex-col gap-3 sm:flex-row">
            <button
              type="button"
              onClick={() => navigate('/')}
              className="inline-flex w-full items-center justify-center rounded-md border border-[#3c3c3c] bg-[#252526] px-4 py-3 text-sm font-medium text-[#d4d4d4] transition hover:bg-[#2a2d2e]"
            >
              나중에
            </button>
            <button
              type="submit"
              disabled={isSubmitDisabled}
              className={`inline-flex w-full items-center justify-center rounded-md px-4 py-3 text-sm font-semibold transition ${
                isSubmitDisabled
                  ? 'cursor-not-allowed border border-[#3c3c3c] bg-[#2a2a2a] text-[#6b7280]'
                  : 'bg-[#0e639c] text-white hover:bg-[#1177bb]'
              }`}
            >
              {isSubmitting ? '저장 중' : '저장'}
            </button>
          </div>
        </form>
      </section>
    </div>
  );
};
