import { useEffect, useState } from "react";
import {
    Alert,
    Badge, Button,
    Group,
    Loader, Modal,
    Paper,
    Select,
    Stack,
    Table,
    Text,
    Title
} from "@mantine/core";
import { getUsers } from "../api/usersApi.js";
import {useDisclosure} from "@mantine/hooks";
import CreateUserForm from "../components/CreateUserForm.jsx";

const UsersPage = () => {
    const [users, setUsers] = useState([]);
    const [error, setError] = useState("");
    const [loading, setLoading] = useState(true);
    const [role, setRole] = useState("");
    const [opened, {open, close}] = useDisclosure(false);

    useEffect(() => {
        async function loadUsers() {
            try {
                const data = await getUsers();
                setUsers(data);
                setError("");
            } catch (error) {
                setError(error.message);
                setUsers([]);
            } finally {
                setLoading(false);
            }
        }

        loadUsers();
    }, []);

    const filteredUsers = users.filter((user) => {
        return role === "" || user.role === role;
    });

    if (loading) {
        return (
            <Group justify="center" mt="xl">
                <Loader />
            </Group>
        );
    }

    if (error) {
        return (
            <Alert color="red" title="Error">
                {error}
            </Alert>
        );
    }

    return (
        <Stack gap="lg">
            <div>
                <Title order={1}>Users</Title>
                <Text c="dimmed">
                    View and filter registered users.
                </Text>
            </div>

            <Paper withBorder p="md" radius="md">
                <Group justify="space-between" align="end">
                    <Select
                        label="Role"
                        placeholder="All roles"
                        value={role}
                        onChange={(value) => setRole(value || "")}
                        data={[
                            {
                                value: "",
                                label: "All"
                            },
                            {
                                value: "LECTURER",
                                label: "Lecturer"
                            },
                            {
                                value: "ADMIN",
                                label: "Admin"
                            },
                            {
                                value: "STUDENT",
                                label: "Student"
                            }
                        ]}
                        w={240}
                    />
                    <Stack>
                        <Modal opened={opened} onClose={close} title="Create User" centered>
                            <CreateUserForm></CreateUserForm>
                        </Modal>
                        <Button variant="default" onClick={open}>
                            Create User
                        </Button>
                        <Text size="sm" c="dimmed">
                            Showing {filteredUsers.length} of {users.length} users
                        </Text>
                    </Stack>
                </Group>
            </Paper>

            <Paper withBorder radius="md" overflow="hidden">
                {filteredUsers.length === 0 ? (
                    <Text p="md" c="dimmed">
                        No users found.
                    </Text>
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
                            {filteredUsers.map((user) => (
                                <Table.Tr key={user.id}>
                                    <Table.Td>{user.fullName}</Table.Td>
                                    <Table.Td>{user.email}</Table.Td>
                                    <Table.Td>
                                        <Badge variant="light">
                                            {user.role}
                                        </Badge>
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