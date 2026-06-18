import {StrictMode} from 'react';
import {createRoot} from 'react-dom/client';
import { createBrowserRouter, RouterProvider } from "react-router";
import './index.css';
import App from './App.jsx';
import UsersPage from "./pages/UsersPage.jsx";
import AdminDashboard from "./pages/AdminDashboard.jsx";
import LoginPage from "./pages/auth/LoginPage.jsx";

const router = createBrowserRouter([
    {
        path: '/',
        element: <App />
    },
    {
        path: '/login',
        element: <LoginPage />
    },
    {
        path: '/admin',
        element: <AdminDashboard />,
        children: [
            {path: 'users', element: <UsersPage />},
        ]
    }
]);

createRoot(document.getElementById('root')).render(
    <StrictMode>
        <RouterProvider router={router}></RouterProvider>
    </StrictMode>
);
