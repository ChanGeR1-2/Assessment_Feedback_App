import { NavLink as MantineNavLink, Stack } from "@mantine/core";
import { NavLink as RouterNavLink, useLocation } from "react-router";
import { getCurrentUser } from "../../pages/auth/currentUser.js";

const LINKS_BY_ROLE = {
    ADMIN: [
        { to: "/admin-dashboard", label: "Dashboard" },
        { to: "/users", label: "Users" },
    ],
    LECTURER: [
        { to: "/lecturer-dashboard", label: "Dashboard" },
        { to: "/modules", label: "Modules" },
        { to: "/queries", label: "Questions" },
        { to: "/phrases", label: "Phrase bank" },
    ],
    STUDENT: [
        { to: "/student-dashboard", label: "Dashboard" },
        { to: "/my-modules", label: "My modules" },
        { to: "/my-progress", label: "My progress" },
    ],
};

const Navbar = ({ onNavigate }) => {
    const currentUser = getCurrentUser();
    const location = useLocation();
    const links = LINKS_BY_ROLE[currentUser?.role] ?? [];

    return (
        <Stack gap="xs" component="nav">
            {links.map((link) => (
                <MantineNavLink
                    key={link.to}
                    component={RouterNavLink}
                    to={link.to}
                    label={link.label}
                    onClick={onNavigate}
                    active={location.pathname.startsWith(link.to)}
                />
            ))}
        </Stack>
    );
};

export default Navbar;