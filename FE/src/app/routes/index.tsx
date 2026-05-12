import { createBrowserRouter, RouterProvider } from "react-router";
import { DashboardPage } from "@pages/dashboard/ui";
import { WorkspacePage } from "@pages/workspace";

const router = createBrowserRouter([
    {
        path: '/',
        Component: DashboardPage
    },
    {
        path: '/workspace',
        Component: WorkspacePage
    }
]);

export default function Router() {
    return <RouterProvider router={router} />
}