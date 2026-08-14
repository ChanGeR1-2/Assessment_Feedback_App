import {useEffect, useState} from "react";
import {
    Badge, Group, Loader, Paper, Stack, Table, Text, Title
} from "@mantine/core";
import {notifications} from "@mantine/notifications";
import {getCurrentUser} from "./auth/currentUser.js";
import {getFeedbackByStudentId} from "../api/feedbackApi.js";
import {getStudentTagCountsByYear} from "../api/tagsApi.js";

const StudentProgressPage = () => {
    const currentUser = getCurrentUser();
    const [feedback, setFeedback] = useState([]);
    const [yearTags, setYearTags] = useState([]);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        if (!currentUser?.id) return;
        let cancelled = false;

        Promise.all([
            getFeedbackByStudentId({studentId: currentUser.id}),
            getStudentTagCountsByYear({studentId: currentUser.id}),
        ])
            .then(([feedbackData, tagData]) => {
                if (cancelled) return;
                setFeedback(feedbackData);
                setYearTags(tagData);
            })
            .catch((e) => {
                if (!cancelled) {
                    notifications.show({title: "Error", message: e.message, color: "red"});
                }
            })
            .finally(() => {
                if (!cancelled) setLoading(false);
            });

        return () => {
            cancelled = true;
        };
    }, [currentUser?.id]);

    if (loading) {
        return <Group justify="center" p="xl"><Loader/></Group>;
    }

    // ---- per-year averages, from the feedback we already have ----
    const years = [...new Set(feedback.map((f) => f.academicYear))].sort();

    const yearSummaries = years.map((year) => {
        const yearFeedback = feedback.filter((f) => f.academicYear === year);
        const average = yearFeedback.length
            ? Math.round(
                yearFeedback.reduce(
                    (sum, f) => sum + (f.totalMark ? (f.mark / f.totalMark) * 100 : 0), 0
                ) / yearFeedback.length
            )
            : null;
        return {year, average, count: yearFeedback.length};
    });

    // ---- pivot the tag rows into tag × year ----
    const buildMatrix = (type) => {
        const rows = yearTags.filter((t) => t.tagType === type);
        const names = [...new Set(rows.map((t) => t.name))];
        return names
            .map((name) => ({
                name,
                counts: years.map(
                    (year) =>
                        rows.find((r) => r.name === name && r.academicYear === year)?.count ?? 0
                ),
            }))
            .sort(
                (a, b) =>
                    b.counts.reduce((x, y) => x + y, 0) - a.counts.reduce((x, y) => x + y, 0)
            );
    };

    const improvements = buildMatrix("IMPROVEMENT");
    const strengths = buildMatrix("STRENGTH");

    const resolvedThemes = improvements.filter(
        (r) => r.counts.at(-1) === 0 && r.counts.some((c) => c > 0)
    );
    const persistentThemes = improvements.filter(
        (r) => r.counts.every((c) => c > 0)
    );

    const emergingStrengths = strengths.filter(
        (r) => r.counts.at(-1) > 0 && r.counts[0] === 0
    );
    const consistentStrengths = strengths.filter(
        (r) => r.counts.every((c) => c > 0)
    );

    if (years.length === 0) {
        return (
            <Stack gap="lg">
                <Title order={1}>Your progress</Title>
                <Paper withBorder radius="md" p="xl">
                    <Text c="dimmed" ta="center">
                        Once you've received feedback, your progress will appear here.
                    </Text>
                </Paper>
            </Stack>
        );
    }

    const matrix = (title, rows, color, emptyText, notes = null) => (
        <div>
            <Title order={4} mb="xs">{title}</Title>
            {notes}
            <Paper withBorder radius="md" style={{overflow: "hidden"}}>
                {rows.length === 0 ? (
                    <Text p="md" c="dimmed">{emptyText}</Text>
                ) : (
                    <Table verticalSpacing="sm">
                        <Table.Thead>
                            <Table.Tr>
                                <Table.Th>Theme</Table.Th>
                                {years.map((y) => (
                                    <Table.Th key={y} ta="center" w={110}>{y}</Table.Th>
                                ))}
                            </Table.Tr>
                        </Table.Thead>
                        <Table.Tbody>
                            {rows.map((row) => {
                                const resolved = row.counts.at(-1) === 0 && row.counts.some((c) => c > 0);
                                return (
                                    <Table.Tr key={row.name} style={{ opacity: resolved ? 0.6 : 1 }}>
                                        <Table.Td>
                                            <Group gap="xs" wrap="nowrap">
                                                <Text size="sm" fw={500}>{row.name}</Text>
                                                {resolved && <Badge size="xs" variant="light" color="teal">Resolved</Badge>}
                                            </Group>
                                        </Table.Td>
                                        {row.counts.map((count, i) => (
                                            <Table.Td key={years[i]} ta="center">
                                                {count > 0 ? (
                                                    <Badge variant="light" color={color}>{count}</Badge>
                                                ) : (
                                                    <Text c="dimmed">—</Text>
                                                )}
                                            </Table.Td>
                                        ))}
                                    </Table.Tr>
                                )
                            })}
                        </Table.Tbody>
                    </Table>
                )}
            </Paper>
        </div>
    );

    const multipleYears = years.length > 1;

    const improvementNotes = multipleYears && (resolvedThemes.length > 0 || persistentThemes.length > 0) ? (
        <Stack gap={4} mb="xs">
            {resolvedThemes.length > 0 && (
                <Text size="sm" c="teal">
                    {resolvedThemes.map((r) => r.name).join(", ")} no longer{" "}
                    {resolvedThemes.length === 1 ? "appears" : "appear"} in your most recent feedback.
                </Text>
            )}
            {persistentThemes.length > 0 && (
                <Text size="sm" c="dimmed">
                    {persistentThemes.map((r) => r.name).join(", ")}{" "}
                    {persistentThemes.length === 1 ? "has" : "have"} come up in every year.
                </Text>
            )}
        </Stack>
    ) : null;

    const strengthNotes = multipleYears && (emergingStrengths.length > 0 || consistentStrengths.length > 0) ? (
        <Stack gap={4} mb="xs">
            {emergingStrengths.length > 0 && (
                <Text size="sm" c="teal">
                    {emergingStrengths.map((r) => r.name).join(", ")}{" "}
                    {emergingStrengths.length === 1 ? "has" : "have"} emerged as{" "}
                    {emergingStrengths.length === 1 ? "a strength" : "strengths"} in your recent work.
                </Text>
            )}
            {consistentStrengths.length > 0 && (
                <Text size="sm" c="dimmed">
                    {consistentStrengths.map((r) => r.name).join(", ")}{" "}
                    {consistentStrengths.length === 1 ? "has been" : "have been"} recognised every year.
                </Text>
            )}
        </Stack>
    ) : null;

    return (
        <Stack gap="xl">
            <div>
                <Title order={1}>Your progress</Title>
                <Text c="dimmed">How your marks and feedback themes have changed over time</Text>
            </div>

            <div>
                <Title order={4} mb="xs">Average mark by year</Title>
                <Group gap="md">
                    {yearSummaries.map((y, index) => {
                        const delta = index > 0 ? y.average - yearSummaries[index - 1].average : null;
                        return (
                            <Paper key={y.year} withBorder radius="md" p="md" style={{ flex: 1 }}>
                                <Group justify="space-between" align="flex-start" wrap="nowrap">
                                    <div>
                                        <Text size="xs" c="dimmed" tt="uppercase" fw={600}>{y.year}</Text>
                                        <Text fw={700} size="xl">
                                            {y.average !== null ? `${y.average}%` : "—"}
                                        </Text>
                                        <Text size="xs" c="dimmed">
                                            {y.count} {y.count === 1 ? "assessment" : "assessments"}
                                        </Text>
                                    </div>
                                    {delta !== null && delta !== 0 && (
                                        <Badge variant="light" color={delta > 0 ? "teal" : "red"} size="sm">
                                            {delta > 0 ? "▲" : "▼"} {Math.abs(delta)}
                                        </Badge>
                                    )}
                                </Group>
                            </Paper>
                        )
                    })}
                </Group>
            </div>

            {matrix(
                "Areas for improvement",
                improvements,
                "orange",
                "No improvement themes recorded yet.",
                improvementNotes
            )}

            {matrix(
                "Strengths",
                strengths,
                "teal",
                "No strengths recorded yet.",
                strengthNotes
            )}
        </Stack>
    );
};

export default StudentProgressPage;