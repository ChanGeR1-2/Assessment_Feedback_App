import { useEffect, useState } from "react";
import { Link } from "react-router";
import { Anchor, Badge, Card, Divider, Group, Loader, Paper, SimpleGrid, Stack, Text, Title } from "@mantine/core";
import { notifications } from "@mantine/notifications";
import { getCurrentUser } from "./auth/currentUser.js";
import {getStudentModules} from "../api/modulesApi.js";

const StudentModulesPage = () => {
    const currentUser = getCurrentUser();
    const [modules, setModules] = useState([]);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        let cancelled = false;
        getStudentModules({ studentId: currentUser?.id })
            .then((data) => { if (!cancelled) setModules(data); })
            .catch((e) => { if (!cancelled) notifications.show({ title: "Error", message: e.message, color: "red" }); })
            .finally(() => { if (!cancelled) setLoading(false); });
        return () => { cancelled = true; };
    }, [currentUser?.id]);

    if (loading) return <Group justify="center" p="xl"><Loader /></Group>;

    const years = new Map();
    for (const m of modules) {
        const y = m.academicYear ?? "Unknown";
        if (!years.has(y)) years.set(y, []);
        years.get(y).push(m);
    }
    const yearList = [...years.entries()].sort((a, b) => b[0].localeCompare(a[0]));

    return (
        <Stack gap="xl">
            <div>
                <Title order={1}>My modules</Title>
                <Text c="dimmed">Select a module to see your feedback</Text>
            </div>

            {yearList.length === 0 ? (
                <Paper withBorder radius="md" p="xl">
                    <Text c="dimmed" ta="center">You aren't enrolled in any modules yet.</Text>
                </Paper>
            ) : (
                yearList.map(([year, mods]) => (
                    <div key={year}>
                        <Group mb="sm">
                            <Title order={3}>{year}</Title>
                            <Badge variant="light">{mods.length}</Badge>
                        </Group>
                        <Divider mb="md" />
                        <SimpleGrid cols={{ base: 1, sm: 2, lg: 3 }} spacing="md">
                            {mods.map((m) => (
                                <Card key={m.id} component={Link} to={`/my-modules/${m.id}/feedback`}
                                    withBorder radius="md" padding="lg" style={{ textDecoration: "none" }}>
                                    <Text fw={600} c="dark">{m.code}</Text>
                                    <Text size="sm">{m.title}</Text>
                                    <Text size="xs" c="dimmed" mt={4}>
                                        {m.lecturerName ?? "No lecturer assigned"}
                                    </Text>
                                </Card>
                            ))}
                        </SimpleGrid>
                    </div>
                ))
            )}
        </Stack>
    );
};

export default StudentModulesPage;