import { Navigate, Outlet } from "react-router";
import { getCurrentUser } from "./currentUser.js";
import UnauthorisedPage from "./UnauthorisedPage.jsx";

const ProtectedRoute = ({ allowedRoles }) => {
    const currentUser = getCurrentUser();
    const token = localStorage.getItem("token");

    if (!currentUser || !token) return <Navigate to="/login" replace />;
    if (allowedRoles && !allowedRoles.includes(currentUser.role)) {
        return <UnauthorisedPage />;
    }
    return <Outlet />;
};

export default ProtectedRoute;