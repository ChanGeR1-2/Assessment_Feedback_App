import {AppShell, Avatar, Burger, Group, Menu, UnstyledButton, Text} from "@mantine/core";
import { useDisclosure } from "@mantine/hooks";
import {Outlet, useNavigate} from "react-router";
import Navbar from "./Navbar";
import "./AppLayout.css";


const AppLayout = () => {
    const [mobileOpened, { toggle: toggleMobile, close: closeMobile }] = useDisclosure();
    const currentUser = JSON.parse(localStorage.getItem("currentUser"));
    const navigate = useNavigate();

    const handleLogout = () => {
        localStorage.removeItem("currentUser");
        navigate("/login");
    };

    return (
        <AppShell
            header={{ height: 60 }}
            navbar={{
                width: 250,
                breakpoint: "sm",
                collapsed: { mobile: !mobileOpened },
            }}
            padding="md"
        >
            <AppShell.Header className="app-shell-header">
                <Group h="100%" px="md" justify="space-between">
                    {/* Left: burger (mobile) + title */}
                    <Group>
                        <Burger opened={mobileOpened} onClick={toggleMobile} hiddenFrom="sm" size="sm" />
                        <Text fw={500}>Assessment Feedback</Text>
                    </Group>

                    {/* Right: user menu */}
                    <Menu position="bottom-end" withArrow>
                        <Menu.Target>
                            <UnstyledButton>
                                <Group gap="xs">
                                    <Avatar size="sm" radius="xl" />
                                    <div>
                                        <Text size="sm" fw={500}>{currentUser?.fullName}</Text>
                                        <Text size="xs" c="dimmed">{currentUser?.role}</Text>
                                    </div>
                                </Group>
                            </UnstyledButton>
                        </Menu.Target>
                        <Menu.Dropdown>
                            <Menu.Item onClick={handleLogout}>Logout</Menu.Item>
                        </Menu.Dropdown>
                    </Menu>
                </Group>
            </AppShell.Header>

            <AppShell.Navbar>
                <Navbar onNavigate={closeMobile} />
            </AppShell.Navbar>

            <AppShell.Main>
                <Outlet />
            </AppShell.Main>
        </AppShell>
    );
};

export default AppLayout;