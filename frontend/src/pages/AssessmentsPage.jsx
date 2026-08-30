import { useEffect, useState } from "react";
import { Link, useNavigate, useParams } from "react-router";
import {
    Anchor, Badge, Breadcrumbs, Group, Loader, Paper, Progress,
    Stack, Table, Text, Title
} from "@mantine/core";
import { notifications } from "@mantine/notifications";
import { getAssessmentsByModuleId, getAllAssessmentStats } from "../api/assessmentsApi.js";
import NotFoundPage from "./NotFoundPage.jsx";
import UnauthorisedPage from "./auth/UnauthorisedPage.jsx";

const AssessmentsPage = () => {
    const navigate = useNavigate();
    const { moduleId } = useParams();

    const [assessments, setAssessments] = useState([]);
    const [loading, setLoading] = useState(true);
    const [notFound, setNotFound] = useState(false);
    const [forbidden, setForbidden] = useState(false);


    useEffect(() => {
        let cancelled = false;

        const load = async () => {
            try {
                const [assessmentData, stats] = await Promise.all([
                    getAssessmentsByModuleId({ moduleId }),
                    getAllAssessmentStats(),
                ]);
                if (cancelled) return;
                setAssessments(assessmentData.map((a) => ({
                    ...a,
                    stats: stats.find((s) => s.assessmentId === a.id),
                })));
            } catch (error) {
                if (!cancelled) {
                    if (error.status === 404) { setNotFound(true); return; }
                    if (error.status === 403) { setForbidden(true); return; }
                    notifications.show({ title: "Error", message: error.message, color: "red" });
                }
            } finally {
                if (!cancelled) setLoading(false);
            }
        };

        load();
        return () => { cancelled = true; };
    }, [moduleId]);

    const formatDate = (value) =>
        value
            ? new Date(value).toLocaleDateString(undefined, {
                day: "numeric", month: "short", year: "numeric",
            })
            : "—";

    // Assessments needing attention first, then chronologically.
    const sorted = [...assessments].sort((a, b) => {
        const outstanding = (s) => (s?.todo ?? 0) + (s?.drafts ?? 0);
        const diff = outstanding(b.stats) - outstanding(a.stats);
        if (diff !== 0) return diff;
        return new Date(a.feedbackDueDate) - new Date(b.feedbackDueDate);
    });

    const moduleTitle = assessments[0]?.moduleTitle;

    if (loading) {
        return <Group justify="center" p="xl"><Loader /></Group>;
    }

    if (notFound) {
        return <NotFoundPage message="That module doesn't exist, or you don't have access to it." />;
    }

    if (forbidden) {
        return <UnauthorisedPage />;
    }

    return (
        <Stack gap="lg">
            <Breadcrumbs>
                <Anchor component={Link} to="/modules" size="sm">Modules</Anchor>
                <Text size="sm" c="dimmed">{moduleTitle ?? "Assessments"}</Text>
            </Breadcrumbs>

            <div>
                <Title order={1}>{moduleTitle ?? "Assessments"}</Title>
                <Text c="dimmed">Marking progress across this module's assessments</Text>
            </div>

            <Paper withBorder>
                {sorted.length === 0 ? (
                    <Text p="md" c="dimmed">
                        This module has no assessments yet.
                    </Text>
                ) : (
                    <Table.ScrollContainer minWidth={700}>
                        <Table striped highlightOnHover verticalSpacing="sm">
                            <Table.Thead>
                                <Table.Tr>
                                    <Table.Th>Assessment</Table.Th>
                                    <Table.Th w={130}>Scheme</Table.Th>
                                    <Table.Th w={170}>Progress</Table.Th>
                                    <Table.Th w={110} ta="right">Marked</Table.Th>
                                </Table.Tr>
                            </Table.Thead>
                            <Table.Tbody>
                                {sorted.map((assessment) => {
                                    const stats = assessment.stats
                                        ?? { enrolled: 0, drafts: 0, published: 0, todo: 0 };
                                    const enrolled = stats.enrolled || 0;
                                    const publishedPct = enrolled ? (stats.published / enrolled) * 100 : 0;
                                    const draftPct = enrolled ? (stats.drafts / enrolled) * 100 : 0;
                                    const complete = enrolled > 0 && stats.published === enrolled;
                                    const outstanding = (stats.todo ?? 0) + (stats.drafts ?? 0);
                                    const overdue =
                                        assessment.feedbackDueDate &&
                                        new Date(assessment.feedbackDueDate) < new Date() &&
                                        outstanding > 0;
                                    const criteria = assessment.markingItems?.length ?? 0;

                                    return (
                                        <Table.Tr
                                            key={assessment.id}
                                            onClick={() =>
                                                navigate(`/modules/${moduleId}/assessments/${assessment.id}/students`)}
                                            style={{ cursor: "pointer" }}
                                        >
                                            <Table.Td>
                                                <Text fw={500} size="sm">{assessment.title}</Text>
                                                <Text size="xs" c="dimmed">
                                                    Due {formatDate(assessment.dueDate)}
                                                </Text>
                                                <Text size="xs" c={overdue ? "red" : "dimmed"}>
                                                    {overdue
                                                        ? "Feedback overdue"
                                                        : `Feedback due ${formatDate(assessment.feedbackDueDate)}`}
                                                </Text>
                                            </Table.Td>

                                            <Table.Td>
                                                {criteria === 0 ? (
                                                    <Anchor
                                                        component={Link}
                                                        to={`/modules/${moduleId}/assessments/${assessment.id}`}
                                                        size="xs"
                                                        c="orange"
                                                        onClick={(e) => e.stopPropagation()}
                                                    >
                                                        Not set up
                                                    </Anchor>
                                                ) : (
                                                    <Anchor
                                                        component={Link}
                                                        to={`/modules/${moduleId}/assessments/${assessment.id}`}
                                                        size="xs"
                                                        onClick={(e) => e.stopPropagation()}
                                                    >
                                                        {criteria} criteria · {assessment.totalMark} marks
                                                    </Anchor>
                                                )}
                                            </Table.Td>

                                            <Table.Td>
                                                <Progress.Root size="sm" radius="xl">
                                                    <Progress.Section value={publishedPct} color="teal" />
                                                    <Progress.Section value={draftPct} color="yellow" />
                                                </Progress.Root>
                                            </Table.Td>

                                            <Table.Td ta="right">
                                                <Text size="sm" fw={500} c={complete ? "teal" : undefined}>
                                                    {stats.published} / {enrolled}
                                                </Text>
                                                {stats.drafts > 0 && (
                                                    <Text size="xs" c="yellow.7">
                                                        {stats.drafts} draft{stats.drafts === 1 ? "" : "s"}
                                                    </Text>
                                                )}
                                            </Table.Td>
                                        </Table.Tr>
                                    );
                                })}
                            </Table.Tbody>
                        </Table>
                    </Table.ScrollContainer>

                )}
            </Paper>
        </Stack>
    );
};

export default AssessmentsPage;