import { useEffect, useMemo, useState } from 'react';

import logo from '@/assets/images/logo.svg';
import { AuthModal } from '@/features/auth';
import { OpenProjectsModal } from '@/features/open-projects';
import {
  clearAuthSession,
  getStoredAuthSession,
  subscribeAuthSession,
} from '@/shared/api/auth';

export const Header = () => {
  const [authSession, setAuthSession] = useState(() => getStoredAuthSession());
  const [isAuthModalOpen, setIsAuthModalOpen] = useState(false);
  const [isOpenProjectsModalOpen, setOpenProjectsModalOpen] = useState(false);
  const [isProfileMenuOpen, setProfileMenuOpen] = useState(false);
  const authModalKey = `${authSession?.userId ?? 'none'}`;
  const profileInitial = useMemo(() => {
    if (!authSession?.nickname) {
      return '';
    }

    return authSession.nickname.trim().charAt(0).toUpperCase();
  }, [authSession]);

  useEffect(() => {
    return subscribeAuthSession(() => {
      setAuthSession(getStoredAuthSession());
    });
  }, []);

  const handleProfileButtonClick = () => {
    if (!authSession) {
      setIsAuthModalOpen(true);
      return;
    }

    setProfileMenuOpen((isOpen) => !isOpen);
  };

  const handleLogout = () => {
    clearAuthSession();
    setProfileMenuOpen(false);
  };

  return (
    <header className="relative flex h-11 w-full bg-[#333333] pl-3 text-sm text-[#cccccc] select-none">
      <div className="logo flex items-center">
        <img className="w-6.5" src={logo} alt="Logo" />
      </div>
      <div className="dropdown-menu relative flex h-full items-center pl-4">
        <ul className="flex h-full">
          <li className="group relative flex h-full items-center px-2">
            <p>Projects</p>
            <ul className="absolute top-full left-0 z-50 hidden min-w-52 cursor-pointer bg-[#252525] py-1 shadow-lg group-hover:block">
              <li>
                <button
                  type="button"
                  className="w-full px-3 py-1.5 text-left text-sm whitespace-nowrap text-[#cccccc] hover:bg-[#04395e] hover:text-white"
                >
                  New Projects
                </button>
              </li>
              <li>
                <button
                  type="button"
                  onClick={() => setOpenProjectsModalOpen(true)}
                  aria-haspopup="dialog"
                  aria-expanded={isOpenProjectsModalOpen}
                  className="w-full px-3 py-1.5 text-left text-sm whitespace-nowrap text-[#cccccc] hover:bg-[#04395e] hover:text-white"
                >
                  Open Projects test
                </button>
              </li>
            </ul>
          </li>
          <li className="group relative flex h-full items-center px-2">
            <p>Teams</p>
            <ul className="absolute top-full left-0 z-50 hidden min-w-52 cursor-pointer bg-[#252525] py-1 shadow-lg group-hover:block">
              <li className="px-3 py-1.5 text-sm whitespace-nowrap text-[#cccccc] hover:bg-[#04395e] hover:text-white">
                Invite Member
              </li>
              <li className="px-3 py-1.5 text-sm whitespace-nowrap text-[#cccccc] hover:bg-[#04395e] hover:text-white">
                Team Settings
              </li>
              <li className="px-3 py-1.5 text-sm whitespace-nowrap text-[#cccccc] hover:bg-[#04395e] hover:text-white">
                Permissions
              </li>
            </ul>
          </li>
        </ul>
      </div>
      <div className="project-name absolute left-1/2 flex h-full -translate-x-1/2 items-center px-4">
        <p>Project Name</p>
      </div>
      <div className="user-profile absolute right-3 flex h-full items-center">
        <button
          type="button"
          onClick={handleProfileButtonClick}
          aria-haspopup="dialog"
          aria-expanded={authSession ? isProfileMenuOpen : isAuthModalOpen}
          aria-label={authSession ? '프로필 메뉴 열기' : '인증 팝업 열기'}
          className={`flex h-8 w-8 items-center justify-center rounded-full text-xs font-semibold text-white transition focus-visible:ring-2 focus-visible:ring-[#007acc]/70 focus-visible:outline-none ${
            authSession
              ? 'bg-[#0e639c] hover:bg-[#1177bb]'
              : 'bg-[#555555] hover:bg-[#6a6a6a]'
          }`}
        >
          {authSession ? (
            profileInitial
          ) : (
            <svg
              xmlns="http://www.w3.org/2000/svg"
              fill="none"
              viewBox="0 0 24 24"
              strokeWidth={1.5}
              stroke="currentColor"
              className="h-5 w-5"
            >
              <path
                strokeLinecap="round"
                strokeLinejoin="round"
                d="M15 7.5a3 3 0 11-6 0 3 3 0 016 0ZM4.5 19.5a7.5 7.5 0 0115 0"
              />
              <path strokeLinecap="round" d="M4.3 20.5h15.4" />
            </svg>
          )}
        </button>
        {authSession && isProfileMenuOpen ? (
          <div className="absolute top-full right-0 z-50 mt-1 w-56 border border-[#3c3c3c] bg-[#252526] py-2 shadow-lg">
            <div className="border-b border-[#3c3c3c] px-3 pb-2">
              <p className="truncate text-sm font-semibold text-[#f3f3f3]">
                {authSession.nickname}
              </p>
              <p className="mt-1 truncate text-xs text-[#858585]">
                {authSession.email}
              </p>
            </div>
            <button
              type="button"
              onClick={() => {
                setProfileMenuOpen(false);
                window.location.assign('/profile/setup');
              }}
              className="mt-1 w-full px-3 py-1.5 text-left text-sm text-[#cccccc] hover:bg-[#04395e] hover:text-white"
            >
              닉네임 설정
            </button>
            <button
              type="button"
              onClick={handleLogout}
              className="w-full px-3 py-1.5 text-left text-sm text-[#cccccc] hover:bg-[#04395e] hover:text-white"
            >
              로그아웃
            </button>
          </div>
        ) : null}
      </div>
      <AuthModal
        key={authModalKey}
        isOpen={isAuthModalOpen}
        pendingSignup={null}
        profileSetupSession={null}
        onClose={() => setIsAuthModalOpen(false)}
      />
      <OpenProjectsModal
        isOpen={isOpenProjectsModalOpen}
        onClose={() => setOpenProjectsModalOpen(false)}
      />
    </header>
  );
};
