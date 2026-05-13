import logo from '@/assets/images/logo.png';

export const Header = () => {
    return (
        <header className="w-full border border-red-500 h-11 flex pl-3">
            <div className="logo flex items-center">
              <img className="w-6.5 h-6.5" src={logo} alt="Logo" />
            </div>
            <div className="dropdown-menu flex relative h-full items-center pl-4">
              <ul className="flex h-full">
                <li className="h-full relative border flex items-center group cursor-pointer px-2">
                  <p>Projects</p>
                  <ul className="absolute top-full left-0 w-2xs border hidden group-hover:block">
                    <li>New Projects</li>
                    <li>Open Projects</li>
                  </ul>
                </li>
                <li className="h-full relative border flex items-center group cursor-pointer px-2">
                  <p>Teams</p>
                  <ul className="absolute top-full left-0 w-2xs border hidden group-hover:block">
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