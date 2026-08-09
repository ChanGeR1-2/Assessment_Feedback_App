import { useEffect, useState } from "react";
import { Link, useNavigate } from "react-router";
import {
    Anchor, Badge, Card, Grid, Group, Loader, Paper, SimpleGrid,
    Stack, Table, Text, Title
} from "@mantine/core";
import { notifications } from "@mantine/notifications";
import { getCurrentUser } from "./auth/currentUser.js";
import {getFeedbackByStudentId, getStudentFeedbackQueries} from "../api/feedbackApi.js";
import {getStudentTagCount} from "../api/tagsApi.js";
import {TagSummary} from "../components/TagSummary.jsx";
import {getAssessments} from "../api/assessmentsApi.js";

const StudentDashboard = () => {
    const currentUser = getCurrentUser();
    const navigate = useNavigate();
    const [feedback, setFeedback] = useState([]);
    const [tagCounts, setTagCounts] = useState([]);
    const [assessments, setAssessments] = useState([]);
    const [queries, setQueries] = useState([]);
    const [loading, setLoading] = useState(true);
    const [lastSeen] = useState(() => localStorage.getItem("feedbackLastSeen"));

    useEffect(() => {
        if (!currentUser?.id) return;
        let cancelled = false;

        Promise.all([
            getFeedbackByStudentId({ studentId: currentUser.id }),
            getStudentTagCount({ studentId: currentUser.id }),
            getAssessments(),
            getStudentFeedbackQueries({studentId: currentUser.id})
        ])
            .then(([feedbackData, tagData, assessmentsData, queriesData]) => {
                if (cancelled) return;
                setFeedback(feedbackData);
                setTagCounts(tagData);
                setAssessments(assessmentsData);
                setQueries(queriesData);
            })
            .catch((e) => {
                if (!cancelled) {
                    notifications.show({ title: "Error", message: e.message, color: "red" });
                }
            })
            .finally(() => { if (!cancelled) setLoading(false); });

        localStorage.setItem("feedbackLastSeen", new Date().toISOString());
        return () => { cancelled = true; };
    }, [currentUser?.id]);

    const percentageOf = (f) =>
        f.totalMark ? Math.round((f.mark / f.totalMark) * 100) : 0;

    const isNew = (f) => !lastSeen || new Date(f.createdAt) > new Date(lastSeen);

    const sorted = [...feedback].sort(
        (a, b) => new Date(b.createdAt) - new Date(a.createdAt)
    );
    const recent = sorted.slice(0, 4);
    const newCount = feedback.filter(isNew).length;

    const average = feedback.length
        ? Math.round(feedback.reduce((sum, f) => sum + percentageOf(f), 0) / feedback.length)
        : null;

    const markedAssessmentIds = new Set(feedback.map((f) => f.assessmentId));
    const awaiting = assessments
        .filter((a) => !markedAssessmentIds.has(a.id))
        .sort((a, b) => new Date(a.dueDate) - new Date(b.dueDate));

    if (loading) {
        return <Group justify="center" p="xl"><Loader /></Group>;
    }

    return (
        <Stack gap="lg">
            <div>
                <Title order={1}>Welcome back, {currentUser?.fullName?.split(" ")[0]}</Title>
                <Text c="dimmed">
                    {newCount > 0
                        ? `You have ${newCount} new ${newCount === 1 ? "piece" : "pieces"} of feedback`
                        : "No new feedback since your last visit"}
                </Text>
            </div>

            <Grid gutter="lg">
                <Grid.Col span={{ base: 12, md: 7 }}>
                    <Group justify="space-between" mb="xs">
                        <Title order={4}>Recent feedback</Title>
                        <Anchor component={Link} to="/my-modules" size="sm">View all</Anchor>
                    </Group>

                    <Paper withBorder radius="md" style={{ overflow: "hidden" }}>
                        {recent.length === 0 ? (
                            <Text p="md" c="dimmed">No feedback yet.</Text>
                        ) : (
                            <Table highlightOnHover verticalSpacing="sm">
                                <Table.Tbody>
                                    {recent.map((f) => {
                                        const pct = percentageOf(f);
                                        return (
                                            <Table.Tr
                                                key={f.id}
                                                onClick={() => navigate(`/feedback/${f.id}`)}
                                                style={{ cursor: "pointer" }}
                                            >
                                                <Table.Td>
                                                    <Group gap="xs" wrap="nowrap">
                                                        <Text fw={500} size="sm">{f.assessmentTitle}</Text>
                                                        {isNew(f) && (
                                                            <Badge size="xs" variant="filled">New</Badge>
                                                        )}
                                                    </Group>
                                                    <Text size="xs" c="dimmed">{f.moduleTitle}</Text>
                                                </Table.Td>
                                                <Table.Td w={80} ta="right">
                                                    <Badge
                                                        variant="light"
                                                        color={pct >= 40 ? "teal" : "red"}
                                                    >
                                                        {pct}%
                                                    </Badge>
                                                </Table.Td>
                                            </Table.Tr>
                                        );
                                    })}
                                </Table.Tbody>
                            </Table>
                        )}
                    </Paper>

                    <SimpleGrid cols={2} spacing="md" mt="md">
                        <Card withBorder radius="md" padding="sm">
                            <Text size="xs" c="dimmed" tt="uppercase" fw={600}>Feedback received</Text>
                            <Text fw={700} size="xl">{feedback.length}</Text>
                        </Card>
                        <Card withBorder radius="md" padding="sm">
                            <Text size="xs" c="dimmed" tt="uppercase" fw={600}>Average</Text>
                            <Text fw={700} size="xl">{average !== null ? `${average}%` : "—"}</Text>
                        </Card>
                    </SimpleGrid>
                </Grid.Col>

                <Grid.Col span={{ base: 12, md: 5 }}>
                    <Stack gap="lg">
                        <div>
                            <Title order={4} mb="xs">Recurring themes</Title>
                            <TagSummary counts={tagCounts} emptyMessage="..." />
                        </div>

                        {awaiting.length > 0 && (
                            <div>
                                <Title order={4} mb="xs">Awaiting feedback</Title>
                                <Paper withBorder radius="md" p="md">
                                    <Stack gap="xs">
                                        {awaiting.slice(0, 4).map((a) => (
                                            <Group key={a.id} justify="space-between" wrap="nowrap">
                                                <div>
                                                    <Text size="sm" fw={500}>{a.title}</Text>
                                                    <Text size="xs" c="dimmed">{a.moduleTitle}</Text>
                                                </div>
                                                <Text size="xs" c="dimmed" style={{ whiteSpace: "nowrap" }}>
                                                    {a.dueDate
                                                        ? new Date(a.dueDate).toLocaleDateString(undefined, {
                                                            day: "numeric", month: "short" })
                                                        : "—"}
                                                </Text>
                                            </Group>
                                        ))}
                                    </Stack>
                                </Paper>
                            </div>
                        )}

                        {queries.length > 0 && (
                            <div>
                                <Title order={4} mb="xs">Your questions</Title>
                                <Paper withBorder radius="md" p="md">
                                    <Stack gap="xs">
                                        {queries.slice(0, 4).map((q) => (
                                            <Group key={q.id} justify="space-between" wrap="nowrap">
                                                <Anchor
                                                    component={Link}
                                                    to={`/feedback/${q.feedbackId}`}
                                                    size="sm"
                                                    lineClamp={1}
                                                >
                                                    {q.query}
                                                </Anchor>
                                                <Badge
                                                    size="sm"
                                                    variant="light"
                                                    color={q.answer ? "teal" : "gray"}
                                                    style={{ flexShrink: 0 }}
                                                >
                                                    {q.answer ? "Answered" : "Pending"}
                                                </Badge>
                                            </Group>
                                        ))}
                                    </Stack>
                                </Paper>
                            </div>
                        )}
                    </Stack>
                </Grid.Col>
            </Grid>
        </Stack>
    );
};

export default StudentDashboard;