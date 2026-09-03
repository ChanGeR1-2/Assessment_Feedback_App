import { useEffect, useState } from "react";
import {
    Badge, Button, Card, Group, Loader, Modal, Paper, Stack, Text, Textarea, Title
} from "@mantine/core";
import { notifications } from "@mantine/notifications";
import { getUnansweredQueries } from "../api/feedbackApi.js";
import AnswerForm from "../components/AnswerForm.jsx";

const LecturerQueriesPage = () => {
    const [queries, setQueries] = useState([]);
    const [loading, setLoading] = useState(true);
    const [answering, setAnswering] = useState(null);

    useEffect(() => {
        let cancelled = false;
        const load = async () => {
            try {
                const data = await getUnansweredQueries();
                if (!cancelled) setQueries(data);
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
    }, []);

    const handleAnswered = (answeredId) => {
        setQueries((prev) => prev.filter((q) => q.id !== answeredId));
        setAnswering(null);
    };

    queries.sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt));

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
                <Title order={1}>Student questions</Title>
                <Text c="dimmed">Questions from students awaiting your answer</Text>
            </div>

            {queries.length === 0 ? (
                <Paper withBorder radius="md" p="xl">
                    <Text c="dimmed" ta="center">
                        No questions waiting. When a student asks about their feedback, it'll appear here.
                    </Text>
                </Paper>
            ) : (
                <Stack gap="md">
                    {queries.map((query) => (
                        <Card key={query.id} withBorder radius="md" padding="md">
                            <Group justify="space-between" align="flex-start" wrap="nowrap" mb="sm">
                                <div>
                                    <Text fw={600}>{query.studentFullName}</Text>
                                    <Text size="sm" c="dimmed">{query.assessmentTitle}</Text>
                                </div>
                                <Badge variant="light" color="orange">
                                    {new Date(query.createdAt).toLocaleDateString(undefined, {
                                        day: "numeric", month: "short",
                                    })}
                                </Badge>
                            </Group>

                            <Text style={{ whiteSpace: "pre-wrap", lineHeight: 1.6 }} mb="md">
                                {query.query}
                            </Text>

                            <Group justify="flex-end">
                                <Button variant="light" onClick={() => setAnswering(query)}>
                                    Respond
                                </Button>
                            </Group>
                        </Card>
                    ))}
                </Stack>
            )}

            <Modal
                opened={Boolean(answering)}
                onClose={() => setAnswering(null)}
                title="Respond to question"
                centered
            >
                {answering && (
                    <AnswerForm
                        key={answering.id}
                        query={answering}
                        onSuccess={handleAnswered}
                    />
                )}
            </Modal>
        </Stack>
    );
};

export default LecturerQueriesPage;