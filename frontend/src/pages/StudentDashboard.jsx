import { useEffect, useState } from "react";
import { Link } from "react-router";
import {
    Anchor, Badge, Card, Group, Loader, Paper, Progress,
    SimpleGrid, Stack, Table, Text, Title
} from "@mantine/core";
import { notifications } from "@mantine/notifications";
import { getCurrentUser } from "./auth/currentUser.js";
import {getStudentModules} from "../api/modulesApi.js";
import { getFeedbackByStudentId } from "../api/feedbackApi.js";

const StudentDashboard = () => {
    const currentUser = getCurrentUser();
    const studentId = currentUser?.id;

    const [modules, setModules] = useState([]);
    const [feedback, setFeedback] = useState([]);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        if (!studentId) return;

        let cancelled = false;

        const load = async () => {
            try {
                const [moduleData, feedbackData] = await Promise.all([
                    getStudentModules({ studentId }),
                    getFeedbackByStudentId({ studentId }),
                ]);
                if (!cancelled) {
                    setModules(moduleData);
                    setFeedback(feedbackData);
                }
            } catch (error) {
                if (!cancelled) {
                    notifications.show({ title: "Error", message: error.message, color: "red" });
                }
            } finally {
                if (!cancelled) setLoading(false);
            }
        };

        load();
        return () => { cancelled = true; };
    }, [studentId]);

    const percentageOf = (f) =>
        f.totalMark ? Math.round((f.mark / f.totalMark) * 100) : 0;

    const average = feedback.length
        ? Math.round(feedback.reduce((sum, f) => sum + percentageOf(f), 0) / feedback.length)
        : null;

    const recent = [...feedback]
        .sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt))
        .slice(0, 5);

    if (loading) {
        return (
            <Group justify="center" p="xl">
                <Loader />
            </Group>
        );
    }

    return (
        <Stack gap="lg">
            <div>
                <Title order={1}>Welcome back, {currentUser?.fullName?.split(" ")[0]}</Title>
                <Text c="dimmed">Your modules and recent feedback</Text>
            </div>

            <SimpleGrid cols={{ base: 1, sm: 3 }} spacing="md">
                <Card withBorder radius="md" padding="md">
                    <Text size="xs" c="dimmed" tt="uppercase" fw={600}>Modules</Text>
                    <Text fw={700} size="xl">{modules.length}</Text>
                </Card>
                <Card withBorder radius="md" padding="md">
                    <Text size="xs" c="dimmed" tt="uppercase" fw={600}>Feedback received</Text>
                    <Text fw={700} size="xl">{feedback.length}</Text>
                </Card>
                <Card withBorder radius="md" padding="md">
                    <Text size="xs" c="dimmed" tt="uppercase" fw={600}>Average</Text>
                    <Text fw={700} size="xl">{average !== null ? `${average}%` : "—"}</Text>
                </Card>
            </SimpleGrid>

            <div>
                <Title order={3} mb="sm">Your modules</Title>
                <Paper withBorder radius="md" style={{ overflow: "hidden" }}>
                    {modules.length === 0 ? (
                        <Text p="md" c="dimmed">You aren't enrolled in any modules yet.</Text>
                    ) : (
                        <Table striped highlightOnHover verticalSpacing="sm">
                            <Table.Thead>
                                <Table.Tr>
                                    <Table.Th>Code</Table.Th>
                                    <Table.Th>Title</Table.Th>
                                    <Table.Th>Academic year</Table.Th>
                                    <Table.Th>Lecturer</Table.Th>
                                </Table.Tr>
                            </Table.Thead>
                            <Table.Tbody>
                                {modules.map((module) => (
                                    <Table.Tr key={module.id}>
                                        <Table.Td fw={500}>{module.code}</Table.Td>
                                        <Table.Td>{module.title}</Table.Td>
                                        <Table.Td>{module.academicYear}</Table.Td>
                                        <Table.Td c={module.lecturerName ? undefined : "dimmed"}>
                                            {module.lecturerName ?? "Not assigned"}
                                        </Table.Td>
                                    </Table.Tr>
                                ))}
                            </Table.Tbody>
                        </Table>
                    )}
                </Paper>
            </div>

            <div>
                <Title order={3} mb="sm">Recent feedback</Title>
                <Stack gap="sm">
                    {recent.length === 0 ? (
                        <Paper withBorder radius="md" p="md">
                            <Text c="dimmed">No feedback yet.</Text>
                        </Paper>
                    ) : (
                        recent.map((f) => {
                            const pct = percentageOf(f);
                            return (
                                <Paper key={f.id} withBorder radius="md" p="md">
                                    <Group justify="space-between" align="flex-start" wrap="nowrap" mb="xs">
                                        <div>
                                            <Anchor component={Link} to={`/feedback/${f.id}`} fw={600}>
                                                {f.assessmentTitle}
                                            </Anchor>
                                            <Text size="xs" c="dimmed">
                                                {new Date(f.createdAt).toLocaleDateString(undefined, {
                                                    day: "numeric", month: "long", year: "numeric",
                                                })}
                                            </Text>
                                        </div>
                                        <Badge size="lg" variant="light" color={pct >= 40 ? "teal" : "red"}>
                                            {f.mark} / {f.totalMark}
                                        </Badge>
                                    </Group>
                                    <Progress value={pct} size="sm" radius="xl" color={pct >= 40 ? "teal" : "red"} />
                                </Paper>
                            );
                        })
                    )}
                </Stack>
            </div>
        </Stack>
    );
};

export default StudentDashboard;