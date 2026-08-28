import "./Navbar.css";
import {Stack, NavLink as MantineNavLink, Box} from "@mantine/core";
import {NavLink as RouterNavLink} from "react-router";
import {getCurrentUser} from "../../pages/auth/currentUser.js";


const Navbar = ({onNavigate}) => {
    const currentUser = getCurrentUser();

    const linksByRole = {
        ADMIN: [
            {to: "/admin-dashboard", label: "Dashboard"},
            {to: "/users", label: "Users"},
        ],
        LECTURER: [
            {to: "/lecturer-dashboard", label: "Dashboard"},
            {to: "/modules", label: "Modules"},
            {to: "/queries", label: "Queries"},
            {to: "/phrases", label: "Phrases"},
        ],
        STUDENT: [
            {to: "/student-dashboard", label: "Dashboard"},
            {to: "/my-modules", label: "My Modules"},
            {to: "/my-progress", label: "My Progress"},
        ],
    };
    const links = linksByRole[currentUser?.role] ?? [];

    return (
        <Box component="nav" className="sidebar-navbar">
            <Stack gap="xs">
                {links.map((link) =>
                    (
                        <MantineNavLink
                            key={link.to}
                            component={RouterNavLink}
                            to={link.to}
                            onClick={onNavigate}
                            label={link.label}
                            className="sidebar-navbar__link"
                        />
                    )
                )}
            </Stack>
        </Box>
    );
};

export default Navbar;