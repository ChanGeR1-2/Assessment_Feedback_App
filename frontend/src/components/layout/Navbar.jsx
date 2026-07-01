import "./Navbar.css";
import { Stack, NavLink as MantineNavLink, Text, Box } from "@mantine/core";
import { NavLink as RouterNavLink } from "react-router";

const adminLinks = [
    {
        ref: "/admin",
        label: "Dashboard"
    },
    {
        ref: "/admin/users",
        label: "Users"
    }
];

const Navbar = () => {
    const currentUser = JSON.parse(localStorage.getItem("currentUser"));

    return (
        <Box component="nav" className="sidebar-navbar">
            <Text className="sidebar-navbar__title">
                Assessment Feedback
            </Text>

            <Stack gap="xs">
                {currentUser?.role === "ADMIN" &&
                    adminLinks.map((link) => (
                        <MantineNavLink
                            key={link.ref}
                            component={RouterNavLink}
                            to={link.ref}
                            label={link.label}
                            className="sidebar-navbar__link"
                        />
                    ))}
            </Stack>
        </Box>
    );
};

export default Navbar;