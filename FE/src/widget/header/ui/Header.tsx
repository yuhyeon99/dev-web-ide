import logo from '@/assets/images/logo.svg';

export const Header = () => {
    return (
        <header className="w-full h-11 flex pl-3 bg-[#333333] text-[#cccccc] text-sm select-none">
            <div className="logo flex items-center">
              <img className="w-6.5" src={logo} alt="Logo" />
            </div>
            <div className="dropdown-menu flex relative h-full items-center pl-4">
              <ul className="flex h-full">
                <li className="h-full relative flex items-center group px-2">
                  <p>Projects</p>
                  <ul className="absolute top-full left-0 hidden group-hover:block cursor-pointer bg-[#252525] py-1 z-50 shadow-lg min-w-52">
                    <li className="px-3 py-1.5 text-sm text-[#cccccc] whitespace-nowrap hover:bg-[#04395e] hover:text-white">New Projects</li>
                    <li className="px-3 py-1.5 text-sm text-[#cccccc] whitespace-nowrap hover:bg-[#04395e] hover:text-white">Open Projects</li>
                  </ul>
                </li>
                <li className="h-full relative flex items-center group px-2">
                  <p>Teams</p>
                  <ul className="absolute top-full left-0 hidden group-hover:block cursor-pointer bg-[#252525] py-1 z-50 shadow-lg min-w-52">
                    <li className="px-3 py-1.5 text-sm text-[#cccccc] whitespace-nowrap hover:bg-[#04395e] hover:text-white">Invite Member</li>
                    <li className="px-3 py-1.5 text-sm text-[#cccccc] whitespace-nowrap hover:bg-[#04395e] hover:text-white">Team Settings</li>
                    <li className="px-3 py-1.5 text-sm text-[#cccccc] whitespace-nowrap hover:bg-[#04395e] hover:text-white">Permissions</li>
                  </ul>
                </li>
              </ul>
            </div>
        </header>
    );
}