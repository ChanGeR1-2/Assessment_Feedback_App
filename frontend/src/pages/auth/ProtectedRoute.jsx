import { Navigate, Outlet } from "react-router";
import { getCurrentUser } from "./currentUser.js";

const ProtectedRoute = ({ allowedRoles }) => {
    const currentUser = getCurrentUser();

    if (!currentUser) {
        return <Navigate to="/login" replace />;
    }

    if (allowedRoles && !allowedRoles.includes(currentUser.role)) {
        return <Navigate to="/unauthorised" replace />;
    }

    return <Outlet />;
};

export default ProtectedRoute;