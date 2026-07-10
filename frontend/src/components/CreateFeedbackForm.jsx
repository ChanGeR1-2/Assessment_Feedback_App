import { Button, Group, NumberInput, Paper, Stack, Text, Textarea, Title } from "@mantine/core";
import { useState } from "react";
import { useForm } from "@mantine/form";
import { notifications } from "@mantine/notifications";
import { submitFeedback } from "../api/feedbackApi.js";

const CreateFeedbackForm = ({ assessmentId, studentId, lecturerId, onSubmit, studentFullName, assessmentTitle }) => {
    const [submitting, setSubmitting] = useState(false);

    const form = useForm({
        initialValues: { mark: 0, strengths: "", improvements: "", actions: "" },
        validate: {
            strengths: (v) => (v.trim().length > 2 ? null : "Strengths must be at least 3 characters"),
            improvements: (v) => (v.trim().length > 2 ? null : "Improvements must be at least 3 characters"),
            actions: (v) => (v.trim().length > 2 ? null : "Actions must be at least 3 characters"),
            mark: (v) => (v >= 0 && v <= 100 ? null : "Mark must be between 0 and 100"),
        },
    });

    const handleSubmit = async (values) => {
        setSubmitting(true);
        try {
            const savedFeedback = await submitFeedback({
                feedback: {
                    assessmentId,
                    studentId,
                    mark: Number(values.mark),
                    strengths: values.strengths,
                    improvements: values.improvements,
                    actions: values.actions,
                },
                lecturerId,
            });
            onSubmit(savedFeedback);
            form.reset();
        } catch (e) {
            notifications.show({ title: "Feedback creation failed", message: e.message, color: "red" });
        } finally {
            setSubmitting(false);
        }
    };

    return (
        <Paper withBorder shadow="sm" radius="md" p="xl">
            <Stack gap="xs" mb="lg">
                <Title order={2}>Create Feedback</Title>
                <Text c="dimmed" size="sm">
                    {assessmentTitle} · {studentFullName}
                </Text>
            </Stack>

            <form onSubmit={form.onSubmit(handleSubmit)}>
                <Stack gap="md">
                    <NumberInput
                        label="Mark"
                        description="Out of 100"
                        placeholder="0–100"
                        min={0}
                        max={100}
                        clampBehavior="strict"
                        allowDecimal={false}
                        w={160}
                        required
                        key={form.key("mark")}
                        {...form.getInputProps("mark")}
                    />
                    <Textarea
                        label="Strengths"
                        placeholder="What did the student do well?"
                        autosize
                        minRows={3}
                        required
                        key={form.key("strengths")}
                        {...form.getInputProps("strengths")}
                    />
                    <Textarea
                        label="Areas for improvement"
                        placeholder="What could be improved?"
                        autosize
                        minRows={3}
                        required
                        key={form.key("improvements")}
                        {...form.getInputProps("improvements")}
                    />
                    <Textarea
                        label="Recommended actions"
                        placeholder="What should the student do next?"
                        autosize
                        minRows={3}
                        required
                        key={form.key("actions")}
                        {...form.getInputProps("actions")}
                    />
                    <Group justify="flex-end" mt="sm">
                        <Button type="submit" loading={submitting}>
                            Save feedback
                        </Button>
                    </Group>
                </Stack>
            </form>
        </Paper>
    );
};

export default CreateFeedbackForm;