import { useEffect, useState } from "react";
import { Link, useParams } from "react-router";
import { Anchor, Badge, Group, Loader, Paper, Stack, Text, Title } from "@mantine/core";
import { notifications } from "@mantine/notifications";
import { getCurrentUser } from "./auth/currentUser.js";
import { getFeedbackByStudentId } from "../api/feedbackApi.js";

const StudentModuleFeedbackPage = () => {
    const currentUser = getCurrentUser();
    const { moduleId } = useParams();
    const [feedback, setFeedback] = useState([]);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        let cancelled = false;
        getFeedbackByStudentId({ studentId: currentUser?.id })
            .then((data) => {
                if (!cancelled) setFeedback(data.filter((f) => String(f.moduleId) === moduleId));
            })
            .catch((e) => { if (!cancelled) notifications.show({ title: "Error", message: e.message, color: "red" }); })
            .finally(() => { if (!cancelled) setLoading(false); });
        return () => { cancelled = true; };
    }, [currentUser?.id, moduleId]);

    if (loading) return <Group justify="center" p="xl"><Loader /></Group>;

    const moduleTitle = feedback[0]?.moduleTitle;

    return (
        <Stack gap="lg">
            <Anchor component={Link} to="/my-modules" size="sm">← My modules</Anchor>
            <Title order={2}>{moduleTitle ?? "Module feedback"}</Title>

            {feedback.length === 0 ? (
                <Paper withBorder radius="md" p="xl">
                    <Text c="dimmed" ta="center">No feedback for this module yet.</Text>
                </Paper>
            ) : (
                <Stack gap="sm">
                    {feedback.map((f) => {
                        const pct = f.totalMark ? Math.round((f.mark / f.totalMark) * 100) : 0;
                        return (
                            <Paper key={f.id} withBorder radius="md" p="md">
                                <Group justify="space-between" wrap="nowrap">
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
                            </Paper>
                        );
                    })}
                </Stack>
            )}
        </Stack>
    );
};

export default StudentModuleFeedbackPage;