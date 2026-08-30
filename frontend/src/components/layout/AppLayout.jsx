import {AppShell, Avatar, Box, Burger, Group, Menu, Text, UnstyledButton} from "@mantine/core";
import { useDisclosure } from "@mantine/hooks";
import { Outlet, useNavigate } from "react-router";
import Navbar from "./Navbar.jsx";
import { getCurrentUser } from "../../pages/auth/currentUser.js";

const AppLayout = () => {
    const [mobileOpened, { toggle: toggleMobile, close: closeMobile }] = useDisclosure();
    const currentUser = getCurrentUser();
    const navigate = useNavigate();

    const handleLogout = () => {
        localStorage.removeItem("currentUser");
        localStorage.removeItem("token");
        navigate("/login");
    };

    return (
        <AppShell
            header={{ height: 60 }}
            navbar={{
                width: 250,
                breakpoint: "sm",
                collapsed: { mobile: !mobileOpened, desktop: !currentUser },
            }}
            padding="md"
        >
            <AppShell.Header>
                <Group h="100%" px="md" justify="space-between" wrap="nowrap">
                    <Group>
                        {currentUser && (
                            <Burger
                                opened={mobileOpened}
                                onClick={toggleMobile}
                                hiddenFrom="sm"
                                size="sm"
                                aria-label="Toggle navigation"
                            />
                        )}
                        <Text fw={600}>Assessment Feedback</Text>
                    </Group>

                    {currentUser && (
                        <Menu position="bottom-end" withArrow>
                            <Menu.Target>
                                <UnstyledButton>
                                    <Group gap="xs">
                                        <Avatar size="sm" radius="xl" name={currentUser.fullName} color="initials" />
                                        <Box visibleFrom="sm">
                                            <Text size="sm" fw={500}>{currentUser.fullName}</Text>
                                            <Text size="xs" c="dimmed" tt="capitalize">
                                                {currentUser.role?.toLowerCase()}
                                            </Text>
                                        </Box>
                                    </Group>
                                </UnstyledButton>
                            </Menu.Target>
                            <Menu.Dropdown>
                                <Menu.Item onClick={handleLogout}>Log out</Menu.Item>
                            </Menu.Dropdown>
                        </Menu>
                    )}
                </Group>
            </AppShell.Header>

            <AppShell.Navbar p="md">
                <Navbar onNavigate={closeMobile} />
            </AppShell.Navbar>

            <AppShell.Main>
                <Outlet />
            </AppShell.Main>
        </AppShell>
    );
};

export default AppLayout;