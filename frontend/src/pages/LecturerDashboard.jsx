import {useEffect, useState} from "react";
import {Link, useNavigate} from "react-router";
import {getCurrentUser} from "./auth/currentUser.js";
import {getLecturerModules} from "../api/modulesApi.js";
import {getAllAssessmentStats, getAssessments} from "../api/assessmentsApi.js";
import {getLecturerTagCount} from "../api/tagsApi.js";
import {getUnansweredQueries} from "../api/feedbackApi.js";
import {notifications} from "@mantine/notifications";
import {
    Button,
    Grid,
    Group,
    Loader,
    Paper,
    Progress,
    ScrollArea,
    Select,
    Stack,
    Table,
    Text,
    Title
} from "@mantine/core";
import {TagSummary} from "../components/TagSummary.jsx";

const LecturerDashboard = () => {
    const currentUser = getCurrentUser();
    const [loading, setLoading] = useState(true);
    const [modules, setModules] = useState([]);
    const [assessments, setAssessments] = useState([]);
    const [tagCounts, setTagCounts] = useState([]);
    const [queries, setQueries] = useState([]);
    const navigate = useNavigate();

    const [selectedModule, setSelectedModule] = useState(null);

    useEffect(() => {
        getLecturerTagCount({ moduleId: selectedModule }).then(setTagCounts).catch(() => {});
    }, [selectedModule]);

    useEffect(() => {
        if (!currentUser?.id) return;
        let cancelled = false;

        Promise.all([
            getLecturerModules(),
            getAssessments(),
            getAllAssessmentStats(),
            getUnansweredQueries()
        ])
            .then(([modulesData, assessmentsData, assessmentsStats, queriesData]) => {
                if (cancelled) return;
                setModules(modulesData);
                const withStats = assessmentsData.map((a) => ({
                    ...a,
                    stats: assessmentsStats.find((s) => s.assessmentId === a.id),
                }));
                setAssessments(withStats);
                setQueries(queriesData);
            })
            .catch((e) => {
                if (!cancelled) {
                    notifications.show({ title: "Error", message: e.message, color: "red" });
                }
            })
            .finally(() => { if (!cancelled) setLoading(false); });

        return () => { cancelled = true; };
    }, [currentUser?.id]);

    const latestYear = assessments.length
        ? [...new Set(assessments.map((a) => a.academicYear))].sort().at(-1)
        : null;

    const currentAssessments = assessments.filter((a) => a.academicYear === latestYear);

    const sortedAssessments = [...currentAssessments].sort((a, b) => {
        const outstanding = (s) => (s?.todo ?? 0) + (s?.drafts ?? 0);
        const isOverdue = (x) =>
            x.feedbackDueDate &&
            new Date(x.feedbackDueDate) < new Date() &&
            outstanding(x.stats) > 0;

        const overdueDiff = Number(isOverdue(b)) - Number(isOverdue(a));
        if (overdueDiff !== 0) return overdueDiff;

        const diff = outstanding(b.stats) - outstanding(a.stats);
        if (diff !== 0) return diff;

        return new Date(a.feedbackDueDate) - new Date(b.feedbackDueDate);
    });

    const sortedModules = [...modules].sort((a, b) => {
        const yearDiff = b.academicYear.localeCompare(a.academicYear);
        return yearDiff !== 0 ? yearDiff : a.code.localeCompare(b.code);
    });

    const totalOutstanding = currentAssessments.reduce(
        (sum, a) => sum + (a.stats?.todo ?? 0) + (a.stats?.drafts ?? 0), 0
    );

    if (loading) {
        return <Group justify="center" p="xl"><Loader /></Group>;
    }

    return (
        <Stack gap="lg">
            <div>
                <Title order={1}>Welcome back, {currentUser?.fullName?.split(" ")[0]}</Title>
            </div>

            <Grid gutter="lg">
                <Grid.Col span={{base: 12, md: 7}}>
                    <Title order={4}>Marking Progress - {latestYear}</Title>
                    <Text size="sm" c="dimmed" mb="xs">
                        {totalOutstanding === 0
                            ? "All marking up to date"
                            : `${totalOutstanding} students awaiting feedback`}
                    </Text>

                    <Paper withBorder radius="md" style={{overflow: "hidden"}}>
                        {sortedAssessments.length === 0 ? (
                            <Text p="md" c="dimmed">No assessments this year.</Text>
                        ) : (
                            <ScrollArea.Autosize mah={400}>
                                <Table highlightOnHover verticalSpacing="sm">
                                    <Table.Tbody>
                                        {sortedAssessments.map((a) => {
                                            const stats = a.stats ?? { enrolled: 0, drafts: 0, published: 0, todo: 0 };
                                            const enrolled = stats.enrolled || 0;
                                            const publishedPct = enrolled ? (stats.published / enrolled) * 100 : 0;
                                            const draftPct = enrolled ? (stats.drafts / enrolled) * 100 : 0;
                                            const complete = enrolled > 0 && stats.published === enrolled;
                                            const outstanding = (stats.todo ?? 0) + (stats.drafts ?? 0);
                                            const overdue =
                                                a.feedbackDueDate &&
                                                new Date(a.feedbackDueDate) < new Date() &&
                                                outstanding > 0;

                                            return (
                                                <Table.Tr
                                                    key={a.id}
                                                    onClick={() => navigate(`/modules/${a.moduleId}/assessments/${a.id}/students`)}
                                                    style={{ cursor: "pointer" }}
                                                >
                                                    <Table.Td>
                                                        <Text fw={500} size="sm">{a.title}</Text>
                                                        <Text size="xs" c="dimmed">{a.moduleTitle}</Text>
                                                        {a.feedbackDueDate && (
                                                            <Text size="xs" c={overdue ? "red" : "dimmed"}>
                                                                {overdue
                                                                    ? "Overdue"
                                                                    : `Due ${new Date(a.feedbackDueDate).toLocaleDateString(undefined, {
                                                                        day: "numeric", month: "short" })}`}
                                                            </Text>
                                                        )}
                                                    </Table.Td>

                                                    <Table.Td w={160}>
                                                        <Progress.Root size="sm" radius="xl">
                                                            <Progress.Section value={publishedPct} color="teal" />
                                                            <Progress.Section value={draftPct} color="yellow" />
                                                        </Progress.Root>
                                                    </Table.Td>

                                                    <Table.Td w={110} ta="right">
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
                            </ScrollArea.Autosize>
                        )}
                    </Paper>
                </Grid.Col>

                <Grid.Col span={{base: 12, md: 5}}>
                    <Stack gap="lg">
                        <div>
                            <Group justify="space-between">
                                <Title order={4} mb="xs">Recurring themes</Title>
                                <Select
                                    size="xs"
                                    w={180}
                                    placeholder="All modules"
                                    data={[
                                        { value: "", label: "All modules" },
                                        ...sortedModules.map((m) => ({ value: String(m.id), label: `${m.code} (${m.academicYear})` })),
                                    ]}
                                    value={selectedModule ?? ""}
                                    onChange={(value) => setSelectedModule(value || null)}
                                    allowDeselect={false}
                                />
                            </Group>
                            <TagSummary counts={tagCounts} emptyMessage="Tag your feedback to see which themes come up across your cohort." limit={4} />
                        </div>

                        {queries.length > 0 && (
                            <Paper withBorder radius="md" p="md" bg="var(--mantine-color-yellow-0)">
                                <Group justify="space-between" wrap="nowrap">
                                    <div>
                                        <Text fw={600}>
                                            {queries.length} unanswered {queries.length === 1 ? "question" : "questions"}
                                        </Text>
                                        <Text size="sm" c="dimmed">Students are waiting on a reply</Text>
                                    </div>
                                    <Button variant="light" component={Link} to="/queries">Respond</Button>
                                </Group>
                            </Paper>
                        )}
                    </Stack>
                </Grid.Col>
            </Grid>
        </Stack>
    )
};

export default LecturerDashboard;