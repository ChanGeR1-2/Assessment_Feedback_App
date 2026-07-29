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
import LecturerDashboard from "./pages/LecturerDashboard.jsx";
import AssessmentsPage from "./pages/AssessmentsPage.jsx";
import AssessmentSubmissionsPage from "./pages/AssessmentSubmissionsPage.jsx";
import LecturerFeedbackPage from "./pages/LecturerFeedbackPage.jsx";
import AssessmentDetailsPage from "./pages/AssessmentDetailsPage.jsx";
import StudentDashboard from "./pages/StudentDashboard.jsx";
import StudentFeedbackPage from "./pages/StudentFeedbackPage.jsx";
import LecturerQueriesPage from "./pages/LecturerQueriesPage.jsx";
import StudentModuleFeedbackPage from "./pages/StudentModuleFeedbackPage.jsx";
import StudentModulesPage from "./pages/StudentModulesPage.jsx";

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
                        path: "admin-dashboard",
                        element: <AdminDashboard />,
                    },
                    {
                        path: "users",
                        element: <UsersPage />
                    }
                ]
            },
            {
                element: <ProtectedRoute allowedRoles={["LECTURER"]} />,
                children: [
                    {
                        path: "lecturer-dashboard",
                        element: <LecturerDashboard />
                    },
                    {
                        path: "modules/:moduleId/assessments",
                        element: <AssessmentsPage />
                    },
                    {
                        path: "modules/:moduleId/assessments/:assessmentId",
                        element: <AssessmentDetailsPage />
                    },
                    {
                        path: "modules/:moduleId/assessments/:assessmentId/students",
                        element: <AssessmentSubmissionsPage />
                    },
                    {
                        path: "modules/:moduleId/assessments/:assessmentId/students/:studentId/feedback",
                        element: <LecturerFeedbackPage />
                    },
                    {
                        path: "queries",
                        element: <LecturerQueriesPage /> }
                ]
            },
            {
                element: <ProtectedRoute allowedRoles={["STUDENT"]} />,
                children: [
                    {
                        path: "student-dashboard",
                        element: <StudentDashboard />
                    },
                    {
                        path: "feedback/:id",
                        element: <StudentFeedbackPage />
                    },
                    {
                        path: "my-modules",
                        element: <StudentModulesPage /> },
                    {
                        path: "my-modules/:moduleId/feedback",
                        element: <StudentModuleFeedbackPage /> },
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