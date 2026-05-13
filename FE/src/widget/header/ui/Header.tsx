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
                  <ul className="absolute top-full left-0 w-2xs hidden group-hover:block cursor-pointer">
                    <li>New Projects</li>
                    <li>Open Projects</li>
                  </ul>
                </li>
                <li className="h-full relative flex items-center group px-2">
                  <p>Teams</p>
                  <ul className="absolute top-full left-0 w-2xs hidden group-hover:block cursor-pointer">
                    <li>Invite Member</li>
                    <li>Team Settings</li>
                    <li>Permissions</li>
                  </ul>
                </li>
              </ul>
            </div>
        </header>
    );
}