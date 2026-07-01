import { StrictMode } from "react";
import { createRoot } from "react-dom/client";
import { createBrowserRouter, RouterProvider } from "react-router";
import { MantineProvider } from "@mantine/core";
import { Notifications } from "@mantine/notifications";

import "@mantine/core/styles.css";
import "@mantine/notifications/styles.css";
import "./index.css";

import LandingPage from "./LandingPage.jsx";
import UsersPage from "./pages/UsersPage.jsx";
import AdminDashboard from "./pages/AdminDashboard.jsx";
import LoginPage from "./pages/auth/LoginPage.jsx";
import AppLayout from "./components/layout/AppLayout.jsx";
import ProtectedRoute from "./pages/auth/ProtectedRoute.jsx";

const router = createBrowserRouter([
    {
        path: "/login",
        element: <LoginPage />
    },
    {
        path: "/",
        element: <AppLayout />,
        children: [
            {
                index: true,
                element: <LandingPage />
            },
            {
                element: <ProtectedRoute allowedRoles={["ADMIN"]} />,
                children: [
                    {
                        path: "admin",
                        element: <AdminDashboard />,
                    },
                    {
                        path: "admin/users",
                        element: <UsersPage />
                    }
                ]
            }
        ]
    }
]);

createRoot(document.getElementById("root")).render(
    <StrictMode>
        <MantineProvider>
            <Notifications position="top-right" />
            <RouterProvider router={router} />
        </MantineProvider>
    </StrictMode>
);