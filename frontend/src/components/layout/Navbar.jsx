import "./Navbar.css";
import {Stack, NavLink as MantineNavLink, Box} from "@mantine/core";
import {NavLink as RouterNavLink} from "react-router";
import {useEffect, useState} from "react";
import {getLecturerModules} from "../../api/modulesApi.js";
import {notifications} from "@mantine/notifications";
import {getCurrentUser} from "../../pages/auth/currentUser.js";


const Navbar = ({onNavigate}) => {
    const currentUser = getCurrentUser();
    const [modules, setModules] = useState([]);

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

    useEffect(() => {
        if (!currentUser?.id) return;
        if (currentUser.role !== "LECTURER") return;

        let cancelled = false;
        const loadModules = async () => {
            try {
                const data = await getLecturerModules();
                if (!cancelled) setModules(data);
            } catch (error) {
                if (!cancelled) {
                    notifications.show({title: "Error", message: error.message, color: "red"});
                }
            }
        };
        loadModules();
        return () => {
            cancelled = true;
        };
    }, [currentUser?.id, currentUser?.role]);

    return (
        <Box component="nav" className="sidebar-navbar">
            <Stack gap="xs">
                {links.map((link) =>
                    link.label === "Modules"? (
                        <MantineNavLink
                            key={link.to}
                            label={link.label}
                            className="sidebar-navbar__link"
                        >
                            {modules.map((module) => (
                                <MantineNavLink
                                    key={module.id}
                                    component={RouterNavLink}
                                    to={`/modules/${module.id}/assessments`}
                                    label={module.title}
                                    onClick={onNavigate}
                                    className="sidebar-navbar__link"
                                />
                            ))}
                        </MantineNavLink>
                    ) : (
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