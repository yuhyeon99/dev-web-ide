import { createBrowserRouter, RouterProvider } from 'react-router';

import { DashboardPage } from '@pages/dashboard';
import { ProfileSetupPage } from '@pages/profile-setup';
import { WorkspacePage } from '@pages/workspace';

const router = createBrowserRouter([
  {
    path: '/',
    Component: DashboardPage,
  },
  {
    path: '/profile/setup',
    Component: ProfileSetupPage,
  },
  {
    path: '/workspace',
    Component: WorkspacePage,
  },
]);

export default function Router() {
  return <RouterProvider router={router} />;
}
