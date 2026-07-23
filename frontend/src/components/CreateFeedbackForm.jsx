import { Button, Group, NumberInput, Paper, Stack, Text, Textarea, Title } from "@mantine/core";
import { useState } from "react";
import { useForm } from "@mantine/form";
import { notifications } from "@mantine/notifications";
import { submitFeedback } from "../api/feedbackApi.js";

const CreateFeedbackForm = ({ assessmentId, studentId, lecturerId, onSubmit, studentFullName, assessmentTitle, markingItems }) => {
    const [submitting, setSubmitting] = useState(false);

    const form = useForm({
        initialValues: {
            summary: "",
            items: markingItems.map((mi) => ({
                markingItemId: mi.id,
                awardedMark: 0,
                comment: "",
            })),
        },
        validate: {
            items: {
                awardedMark: (value, values, path) => {
                    const index = Number(path.split(".")[1]);
                    const max = markingItems[index].maxMark;
                    if (value == null) return "Mark is required";
                    if (value < 0 || value > max) return `Must be between 0 and ${max}`;
                    return null;
                },
            },
        },
    });

    const total = form.values.items.reduce((sum, i) => sum + (i.awardedMark || 0), 0);
    const maxTotal = markingItems.reduce((sum, mi) => sum + mi.maxMark, 0);

    const handleSubmit = async (values) => {
        setSubmitting(true);
        try {
            const savedFeedback = await submitFeedback({
                feedback: {
                    ...values,
                    assessmentId,
                    studentId
                },
                lecturerId,
            });
            onSubmit(savedFeedback);
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
                <Stack gap="lg">
                    {markingItems.map((markingItem, index) => (
                        <Paper key={markingItem.id} withBorder p="md" radius="md">
                            <Group justify="space-between" mb="sm">
                                <Text fw={600}>{markingItem.name}</Text>
                                <NumberInput
                                    w={120}
                                    min={0}
                                    max={markingItem.maxMark}
                                    clampBehavior="strict"
                                    allowDecimal={false}
                                    suffix={` / ${markingItem.maxMark}`}
                                    {...form.getInputProps(`items.${index}.awardedMark`)}
                                />
                            </Group>
                            <Textarea
                                placeholder="Comments on this criterion"
                                autosize
                                minRows={2}
                                {...form.getInputProps(`items.${index}.comment`)}
                            />
                        </Paper>
                    ))}

                    <Group justify="flex-end">
                        <Text fw={700}>Total: {total} / {maxTotal}</Text>
                    </Group>

                    <Textarea
                        label="Overall summary"
                        autosize
                        minRows={3}
                        {...form.getInputProps("summary")}
                    />

                    <Button type="submit" loading={submitting}>Save feedback</Button>
                </Stack>
            </form>
        </Paper>
    );
};

export default CreateFeedbackForm;