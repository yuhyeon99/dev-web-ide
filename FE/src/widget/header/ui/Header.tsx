import { useState } from 'react';

import logo from '@/assets/images/logo.svg';
import { AuthModal } from '@/features/auth';
import { OpenProjectsModal } from '@/features/open-projects';
import { getPendingOAuthSignup } from '@/shared/api/auth';

export const Header = () => {
  const [isAuthModalOpen, setIsAuthModalOpen] = useState(() =>
    Boolean(getPendingOAuthSignup()),
  );
  const [isOpenProjectsModalOpen, setOpenProjectsModalOpen] = useState(false);

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
          onClick={() => setIsAuthModalOpen(true)}
          aria-haspopup="dialog"
          aria-expanded={isAuthModalOpen}
          aria-label="인증 팝업 열기"
          className="flex h-8 w-8 items-center justify-center rounded-full bg-[#555555] text-xs text-white transition hover:bg-[#6a6a6a] focus-visible:ring-2 focus-visible:ring-[#007acc]/70 focus-visible:outline-none"
        >
          {/* 인증 전 */}
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
          {/* TODO: 인증 후: 회원 이미지 */}
        </button>
      </div>
      <AuthModal
        isOpen={isAuthModalOpen}
        onClose={() => setIsAuthModalOpen(false)}
      />
      <OpenProjectsModal
        isOpen={isOpenProjectsModalOpen}
        onClose={() => setOpenProjectsModalOpen(false)}
      />
    </header>
  );
};
