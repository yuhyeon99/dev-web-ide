import logo from '@/assets/images/logo.svg';

export const Header = () => {
  return (
    <header className="flex h-11 w-full bg-[#333333] pl-3 text-sm text-[#cccccc] select-none">
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
    </header>
  );
};
