import { useEffect, useState } from "react";
import { Link, useParams } from "react-router";
import {Anchor, Badge, Group, Loader, Paper, Progress, RingProgress, Stack, Text, Title} from "@mantine/core";
import { notifications } from "@mantine/notifications";
import { getCurrentUser } from "./auth/currentUser.js";
import { getFeedbackByStudentId } from "../api/feedbackApi.js";
import {getAssessmentsByModuleId} from "../api/assessmentsApi.js";

const StudentModuleFeedbackPage = () => {
    const currentUser = getCurrentUser();
    const { moduleId } = useParams();
    const [loading, setLoading] = useState(true);

    const [assessments, setAssessments] = useState([]);
    const [feedback, setFeedback] = useState([]);

    useEffect(() => {
        let cancelled = false;
        Promise.all([
            getAssessmentsByModuleId({ moduleId }),
            getFeedbackByStudentId({ studentId: currentUser?.id }),
        ])
            .then(([assessmentData, feedbackData]) => {
                if (cancelled) return;
                setAssessments(assessmentData);
                setFeedback(feedbackData.filter((f) => String(f.moduleId) === moduleId));
            })
            .catch((e) => {
                if (!cancelled) notifications.show({ title: "Error", message: e.message, color: "red" });
            })
            .finally(() => { if (!cancelled) setLoading(false); });
        return () => { cancelled = true; };
    }, [moduleId, currentUser?.id]);

    if (loading) return <Group justify="center" p="xl"><Loader /></Group>;

    const moduleTitle = feedback[0]?.moduleTitle;

    const rows = assessments
        .map((a) => ({
            assessment: a,
            feedback: feedback.find((f) => f.assessmentId === a.id) ?? null,
        }))
        .sort((a, b) => new Date(a.assessment.dueDate) - new Date(b.assessment.dueDate));

    const marked = rows.filter((r) => r.feedback && r.assessment.weight != null);

    const weightedScore = marked.reduce(
        (sum, r) => sum + (r.feedback.mark / r.feedback.totalMark) * r.assessment.weight,
        0
    );

    const weightAssessed = marked.reduce((sum, r) => sum + r.assessment.weight, 0);

    const gradeSoFar = weightAssessed > 0
        ? Math.round((weightedScore / weightAssessed) * 100)
        : null;

    return (
        <Stack gap="lg">
            <Anchor component={Link} to="/my-modules" size="sm">← My modules</Anchor>
            <Title order={2}>{moduleTitle ?? "Module feedback"}</Title>

            {gradeSoFar !== null && (
                <Paper withBorder radius="md" p="md" bg="var(--mantine-color-gray-0)">
                    <Group justify="space-between" align="center" wrap="nowrap">
                        <div>
                            <Text size="xs" c="dimmed" tt="uppercase" fw={600}>Module grade so far</Text>
                            <Text size="sm" c="dimmed">
                                Based on {weightAssessed}% of the module assessed
                            </Text>
                        </div>
                        <RingProgress
                            size={90}
                            thickness={8}
                            roundCaps
                            sections={[{ value: gradeSoFar, color: gradeSoFar >= 40 ? "teal" : "red" }]}
                            label={
                                <Text ta="center" fw={700}>{gradeSoFar}%</Text>
                            }
                        />
                    </Group>
                    {weightAssessed < 100 && (
                        <Progress value={weightAssessed} size="xs" radius="xl" mt="sm" color="gray" />
                    )}
                </Paper>
            )}

            {rows.map(({ assessment, feedback: f }) => {
                const pct = f?.totalMark ? Math.round((f.mark / f.totalMark) * 100) : null;
                return (
                    <Paper key={assessment.id} withBorder radius="md" p="md">
                        <Group justify="space-between" wrap="nowrap">
                            <div>
                                {f ? (
                                    <Anchor component={Link} to={`/feedback/${f.id}`} fw={600}>
                                        {assessment.title}
                                    </Anchor>
                                ) : (
                                    <Text fw={600} c="dimmed">{assessment.title}</Text>
                                )}
                                <Text size="xs" c="dimmed">
                                    {assessment.dueDate
                                        ? `Due ${new Date(assessment.dueDate).toLocaleDateString(undefined, {
                                            day: "numeric", month: "long", year: "numeric" })}`
                                        : "No due date"}
                                </Text>
                            </div>
                            {f ? (
                                <Badge size="lg" variant="light" color={pct >= 40 ? "teal" : "red"}>
                                    {pct}%
                                </Badge>
                            ) : (
                                <Badge size="lg" variant="default">Awaiting feedback</Badge>
                            )}
                        </Group>
                    </Paper>
                );
            })}
        </Stack>
    );
};

export default StudentModuleFeedbackPage;