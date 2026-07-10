import { useCallback, useEffect, useState } from "react";
import {
    Badge,
    Button,
    Group,
    Loader,
    Modal,
    Paper,
    Select,
    Stack,
    Table,
    Text,
    Title
} from "@mantine/core";
import { getUsers } from "../api/usersApi.js";
import { useDisclosure } from "@mantine/hooks";
import CreateUserForm from "../components/CreateUserForm.jsx";
import { notifications } from "@mantine/notifications";
import { useSearchParams } from "react-router";

const UsersPage = () => {
    const [users, setUsers] = useState([]);
    const [loading, setLoading] = useState(true);
    const [opened, { open, close }] = useDisclosure(false);

    const [searchParams, setSearchParams] = useSearchParams();
    const role = searchParams.get("role") || "";

    const loadUsers = useCallback(async () => {
        setLoading(true);
        try {
            const data = await getUsers({ role: role || null });
            setUsers(data);
        } catch (error) {
            notifications.show({
                title: "Error",
                message: error.message,
                color: "red"
            });
        } finally {
            setLoading(false);
        }
    }, [role]);

    useEffect(() => {
        let cancelled = false;

        async function load() {
            setLoading(true);
            try {
                const data = await getUsers({ role: role || null });
                if (!cancelled) setUsers(data);
            } catch (error) {
                if (!cancelled) {
                    notifications.show({
                        title: "Error",
                        message: error.message,
                        color: "red"
                    });
                }
            } finally {
                if (!cancelled) setLoading(false);
            }
        }

        load();
        return () => { cancelled = true; };
    }, [role]);

    const handleRoleChange = (value) => {
        setSearchParams(value ? { role: value } : {});
    };

    const handleCreateSuccess = () => {
        close();
        loadUsers();
    };

    return (
        <Stack gap="lg">
            <div>
                <Title order={1}>Users</Title>
                <Text c="dimmed">View and filter registered users.</Text>
            </div>

            <Paper withBorder p="md" radius="md">
                <Group justify="space-between" align="end">
                    <Select
                        label="Role"
                        placeholder="All roles"
                        value={role}
                        onChange={handleRoleChange}
                        data={[
                            { value: "", label: "All" },
                            { value: "LECTURER", label: "Lecturer" },
                            { value: "ADMIN", label: "Admin" },
                            { value: "STUDENT", label: "Student" }
                        ]}
                        w={240}
                    />
                    <Stack gap="xs">
                        <Button variant="default" onClick={open}>
                            Create User
                        </Button>
                        <Text size="sm" c="dimmed">
                            Showing {users.length} users
                        </Text>
                    </Stack>
                </Group>
            </Paper>

            <Modal opened={opened} onClose={close} title="Create User" centered>
                <CreateUserForm onSuccess={handleCreateSuccess} />
            </Modal>

            <Paper withBorder radius="md" style={{ overflow: "hidden" }}>
                {loading ? (
                    <Group justify="center" p="xl">
                        <Loader />
                    </Group>
                ) : users.length === 0 ? (
                    <Text p="md" c="dimmed">No users found.</Text>
                ) : (
                    <Table striped highlightOnHover>
                        <Table.Thead>
                            <Table.Tr>
                                <Table.Th>Name</Table.Th>
                                <Table.Th>Email</Table.Th>
                                <Table.Th>Role</Table.Th>
                            </Table.Tr>
                        </Table.Thead>
                        <Table.Tbody>
                            {users.map((user) => (
                                <Table.Tr key={user.id}>
                                    <Table.Td>{user.fullName}</Table.Td>
                                    <Table.Td>{user.email}</Table.Td>
                                    <Table.Td>
                                        <Badge variant="light">{user.role}</Badge>
                                    </Table.Td>
                                </Table.Tr>
                            ))}
                        </Table.Tbody>
                    </Table>
                )}
            </Paper>
        </Stack>
    );
};

export default UsersPage;