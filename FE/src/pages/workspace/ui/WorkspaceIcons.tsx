type IconProps = {
  className?: string;
};

const baseIconClassName = 'h-4 w-4';

export const ExplorerIcon = ({ className = baseIconClassName }: IconProps) => {
  return (
    <svg
      className={className}
      viewBox="0 0 24 24"
      fill="none"
      stroke="currentColor"
      strokeWidth="1.8"
      strokeLinecap="round"
      strokeLinejoin="round"
      aria-hidden="true"
    >
      <path d="M3.75 6.75A2.25 2.25 0 0 1 6 4.5h4.2l1.8 1.8H18A2.25 2.25 0 0 1 20.25 8.55v8.7A2.25 2.25 0 0 1 18 19.5H6a2.25 2.25 0 0 1-2.25-2.25v-10.5Z" />
      <path d="M3.75 9h16.5" />
    </svg>
  );
};

export const SearchIcon = ({ className = baseIconClassName }: IconProps) => {
  return (
    <svg
      className={className}
      viewBox="0 0 24 24"
      fill="none"
      stroke="currentColor"
      strokeWidth="1.8"
      strokeLinecap="round"
      strokeLinejoin="round"
      aria-hidden="true"
    >
      <circle cx="11" cy="11" r="6.75" />
      <path d="m16.25 16.25 3.5 3.5" />
    </svg>
  );
};

export const UsersIcon = ({ className = baseIconClassName }: IconProps) => {
  return (
    <svg
      className={className}
      viewBox="0 0 24 24"
      fill="none"
      stroke="currentColor"
      strokeWidth="1.8"
      strokeLinecap="round"
      strokeLinejoin="round"
      aria-hidden="true"
    >
      <path d="M15.5 18.5a5.5 5.5 0 0 0-11 0" />
      <circle cx="10" cy="8" r="3" />
      <path d="M18 18.5a4 4 0 0 0-3.5-3.95" />
      <path d="M14.5 5.5a3 3 0 0 1 0 5" />
    </svg>
  );
};

export const SettingsIcon = ({ className = baseIconClassName }: IconProps) => {
  return (
    <svg
      className={className}
      viewBox="0 0 24 24"
      fill="none"
      stroke="currentColor"
      strokeWidth="1.8"
      strokeLinecap="round"
      strokeLinejoin="round"
      aria-hidden="true"
    >
      <path d="M9.5 4.5h5" />
      <path d="M7.25 7.5 5 9.75" />
      <path d="m16.75 7.5 2.25 2.25" />
      <path d="M4.5 12h3" />
      <path d="M16.5 12h3" />
      <path d="M7.25 16.5 5 14.25" />
      <path d="m16.75 16.5 2.25-2.25" />
      <path d="M9.5 19.5h5" />
      <circle cx="12" cy="12" r="3.25" />
    </svg>
  );
};

export const ChevronDownIcon = ({
  className = baseIconClassName,
}: IconProps) => {
  return (
    <svg
      className={className}
      viewBox="0 0 24 24"
      fill="none"
      stroke="currentColor"
      strokeWidth="2"
      strokeLinecap="round"
      strokeLinejoin="round"
      aria-hidden="true"
    >
      <path d="m6 9 6 6 6-6" />
    </svg>
  );
};

export const ChevronRightIcon = ({
  className = baseIconClassName,
}: IconProps) => {
  return (
    <svg
      className={className}
      viewBox="0 0 24 24"
      fill="none"
      stroke="currentColor"
      strokeWidth="2"
      strokeLinecap="round"
      strokeLinejoin="round"
      aria-hidden="true"
    >
      <path d="m9 6 6 6-6 6" />
    </svg>
  );
};

export const SaveIcon = ({ className = baseIconClassName }: IconProps) => {
  return (
    <svg
      className={className}
      viewBox="0 0 24 24"
      fill="none"
      stroke="currentColor"
      strokeWidth="1.8"
      strokeLinecap="round"
      strokeLinejoin="round"
      aria-hidden="true"
    >
      <path d="M5.25 4.5h11.1l2.15 2.15V18a1.5 1.5 0 0 1-1.5 1.5H6.75A1.5 1.5 0 0 1 5.25 18V4.5Z" />
      <path d="M8 4.5v5h7v-5" />
      <path d="M8.25 19.5v-5.25h7.5v5.25" />
    </svg>
  );
};

export const ShareIcon = ({ className = baseIconClassName }: IconProps) => {
  return (
    <svg
      className={className}
      viewBox="0 0 24 24"
      fill="none"
      stroke="currentColor"
      strokeWidth="1.8"
      strokeLinecap="round"
      strokeLinejoin="round"
      aria-hidden="true"
    >
      <circle cx="18" cy="5.5" r="2.5" />
      <circle cx="6" cy="12" r="2.5" />
      <circle cx="18" cy="18.5" r="2.5" />
      <path d="m8.2 10.8 7.6-4.1" />
      <path d="m8.2 13.2 7.6 4.1" />
    </svg>
  );
};

export const FolderIcon = ({ className = baseIconClassName }: IconProps) => {
  return (
    <svg
      className={className}
      viewBox="0 0 24 24"
      fill="currentColor"
      aria-hidden="true"
    >
      <path d="M3.75 6.5A1.75 1.75 0 0 1 5.5 4.75h4.05l1.6 1.6h7.35a1.75 1.75 0 0 1 1.75 1.75V17.5a1.75 1.75 0 0 1-1.75 1.75H5.5A1.75 1.75 0 0 1 3.75 17.5v-11Z" />
    </svg>
  );
};

export const FileIcon = ({ className = baseIconClassName }: IconProps) => {
  return (
    <svg
      className={className}
      viewBox="0 0 24 24"
      fill="currentColor"
      aria-hidden="true"
    >
      <path d="M6.25 3.75h7.6l4.15 4.15V18a2 2 0 0 1-2 2H8.25a2 2 0 0 1-2-2V3.75Z" />
      <path d="M13.5 3.75v4.75H18" fill="#1f1f1f" />
    </svg>
  );
};

export const PinIcon = ({ className = baseIconClassName }: IconProps) => {
  return (
    <svg
      className={className}
      viewBox="0 0 24 24"
      fill="none"
      stroke="currentColor"
      strokeWidth="1.8"
      strokeLinecap="round"
      strokeLinejoin="round"
      aria-hidden="true"
    >
      <path d="m14.5 4.75 4.75 4.75-3 1.5-2 5-1.5-1.5-5 2-1.75-1.75 2-5-1.5-1.5 5-2 1.5-3Z" />
      <path d="m8 16-3.5 3.5" />
    </svg>
  );
};

export const TerminalIcon = ({ className = baseIconClassName }: IconProps) => {
  return (
    <svg
      className={className}
      viewBox="0 0 24 24"
      fill="none"
      stroke="currentColor"
      strokeWidth="1.8"
      strokeLinecap="round"
      strokeLinejoin="round"
      aria-hidden="true"
    >
      <path d="m5.5 7.5 4 4-4 4" />
      <path d="M11.5 15.5h7" />
      <rect x="3.75" y="4.5" width="16.5" height="15" rx="2.25" />
    </svg>
  );
};
