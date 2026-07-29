import { Button, Stack, Text, Textarea } from "@mantine/core";
import { useState } from "react";
import { useForm } from "@mantine/form";
import { notifications } from "@mantine/notifications";
import { createFeedbackQuery } from "../api/feedbackApi.js";

const CreateFeedbackQueryForm = ({ feedbackId, onSuccess }) => {
    const [submitting, setSubmitting] = useState(false);

    const form = useForm({
        initialValues: { query: "" },
        validate: {
            query: (value) =>
                value.trim().length >= 3 ? null : "Please enter a question of at least 3 characters",
        },
    });

    const handleSubmit = async (values) => {
        setSubmitting(true);
        try {
            const created = await createFeedbackQuery({
                feedbackId,
                query: values.query,
            });
            notifications.show({
                title: "Question sent",
                message: "Your lecturer will be notified.",
                color: "green",
            });
            onSuccess(created);
        } catch (e) {
            if (e.fieldErrors) {
                form.setErrors(e.fieldErrors);
                return;
            }
            notifications.show({
                title: "Couldn't send your question",
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
                <Text size="sm" c="dimmed">
                    Ask your lecturer to clarify anything about this feedback. You can send one question per piece of feedback.
                </Text>
                <Textarea
                    label="Your question"
                    placeholder="What would you like to ask about this feedback?"
                    autosize
                    minRows={4}
                    maxRows={10}
                    required
                    key={form.key("query")}
                    {...form.getInputProps("query")}
                />
                <Button type="submit" loading={submitting}>
                    Send question
                </Button>
            </Stack>
        </form>
    );
};

export default CreateFeedbackQueryForm;