import logo from '@/assets/images/logo.svg';

export const Header = () => {
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
              <li className="px-3 py-1.5 text-sm whitespace-nowrap text-[#cccccc] hover:bg-[#04395e] hover:text-white">
                New Projects
              </li>
              <li className="px-3 py-1.5 text-sm whitespace-nowrap text-[#cccccc] hover:bg-[#04395e] hover:text-white">
                Open Projects test
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
      <div className="user-profile absolute right-3 flex h-full cursor-pointer items-center">
        <div className="h-6.5 w-6.5 rounded-full bg-[#cccccc]">
          {/* TODO: 사용자 아이콘을 여기에 추가 */}
        </div>
      </div>
    </header>
  );
};
