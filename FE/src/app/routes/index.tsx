import { createBrowserRouter, Outlet, RouterProvider } from 'react-router';

import { DashboardPage } from '@pages/dashboard';
import { ProfileSetupPage } from '@pages/profile-setup';
import { WorkspacePage } from '@pages/workspace';
import { Header } from '@widget/header';

const AppLayout = () => {
  return (
    <div className="app-layout">
      <Header />
      <main>
        <Outlet />
      </main>
    </div>
  );
};

const router = createBrowserRouter([
  {
    path: '/',
    Component: AppLayout,
    children: [
      {
        index: true,
        Component: DashboardPage,
      },
      {
        path: 'profile/setup',
        Component: ProfileSetupPage,
      },
      {
        path: 'workspace',
        Component: WorkspacePage,
      },
    ],
  },
]);

export default function Router() {
  return <RouterProvider router={router} />;
}
