import { useEffect, useState } from "react";
import {
    Badge, Button, Card, Group, Loader, Modal, Paper, Stack, Text, Textarea, Title
} from "@mantine/core";
import { useForm } from "@mantine/form";
import { notifications } from "@mantine/notifications";
import { answerFeedbackQuery, getUnansweredQueries } from "../api/feedbackApi.js";

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

const AnswerForm = ({ query, onSuccess }) => {
    const [submitting, setSubmitting] = useState(false);

    const form = useForm({
        initialValues: { answer: "" },
        validate: {
            answer: (v) =>
                v.trim().length >= 3 ? null : "Please enter a response of at least 3 characters",
        },
    });

    const handleSubmit = async (values) => {
        setSubmitting(true);
        try {
            await answerFeedbackQuery({
                feedbackQueryId: query.id,
                answer: values.answer,
            });
            notifications.show({
                title: "Answer sent",
                message: "The student can now see your answer.",
                color: "green",
            });
            onSuccess(query.id);
        } catch (e) {
            if (e.fieldErrors) {
                form.setErrors(e.fieldErrors);
                return;
            }
            notifications.show({
                title: "Couldn't send your answer",
                message: e.message,
                color: "red",
            });
        } finally {
            setSubmitting(false);
        }
    };

    return (
        <form onSubmit={form.onSubmit(handleSubmit)}>
            <Stack gap="md">
                <div>
                    <Text size="xs" c="dimmed" tt="uppercase" fw={600}>
                        {query.studentFullName} asked
                    </Text>
                    <Text style={{ whiteSpace: "pre-wrap", lineHeight: 1.6 }}>{query.query}</Text>
                </div>
                <Textarea
                    label="Your answer"
                    placeholder="Answer the student's question..."
                    autosize
                    minRows={4}
                    maxRows={12}
                    required
                    key={form.key("answer")}
                    {...form.getInputProps("answer")}
                />
                <Button type="submit" loading={submitting}>
                    Send answer
                </Button>
            </Stack>
        </form>
    );
};

export default LecturerQueriesPage;