import { Navigate, Outlet } from "react-router";
import { getCurrentUser } from "./currentUser.js";

const ProtectedRoute = ({ allowedRoles }) => {
    const currentUser = getCurrentUser();
    const token = localStorage.getItem("token");

    if (!currentUser || !token) return <Navigate to="/login" replace />;
    if (allowedRoles && !allowedRoles.includes(currentUser.role)) {
        return <Navigate to="/" replace />;
    }
    return <Outlet />;
};

export default ProtectedRoute;