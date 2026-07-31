import { useEffect, useState } from "react";
import { Button, Loader, Stack, Text, Textarea, Group } from "@mantine/core";
import { useForm } from "@mantine/form";
import { notifications } from "@mantine/notifications";
import { answerFeedbackQuery } from "../api/feedbackApi.js";
import { getFeedbackById } from "../api/feedbackApi.js";
import StudentFeedbackSection from "./StudentFeedbackSection.jsx";

const AnswerForm = ({ query, onSuccess }) => {
    const [submitting, setSubmitting] = useState(false);
    const [feedback, setFeedback] = useState(null);
    const [feedbackLoading, setFeedbackLoading] = useState(true);

    useEffect(() => {
        let cancelled = false;
        getFeedbackById({ feedbackId: query.feedbackId })
            .then((data) => { if (!cancelled) setFeedback(data); })
            .catch((e) => {
                if (!cancelled) notifications.show({ title: "Couldn't load feedback", message: e.message, color: "red" });
            })
            .finally(() => { if (!cancelled) setFeedbackLoading(false); });
        return () => { cancelled = true; };
    }, [query.feedbackId]);

    const form = useForm({
        initialValues: { response: "" },
        validate: {
            response: (v) => (v.trim().length >= 3 ? null : "Please enter a response of at least 3 characters"),
        },
    });

    const handleSubmit = async (values) => {
        setSubmitting(true);
        try {
            await answerFeedbackQuery({ feedbackQueryId: query.id, response: values.response });
            notifications.show({ title: "Response sent", message: "The student can now see your response.", color: "green" });
            onSuccess(query.id);
        } catch (e) {
            if (e.fieldErrors) { form.setErrors(e.fieldErrors); return; }
            notifications.show({ title: "Couldn't send your response", message: e.message, color: "red" });
        } finally {
            setSubmitting(false);
        }
    };

    return (
        <Stack gap="lg">
            {feedbackLoading ? (
                <Group justify="center" p="md"><Loader size="sm" /></Group>
            ) : feedback ? (
                <StudentFeedbackSection feedback={feedback} />
            ) : (
                <Text c="dimmed" size="sm">Feedback couldn't be loaded.</Text>
            )}

            <form onSubmit={form.onSubmit(handleSubmit)}>
                <Stack gap="md">
                    <div>
                        <Text size="xs" c="dimmed" tt="uppercase" fw={600}>
                            {query.studentFullName} asked
                        </Text>
                        <Text style={{ whiteSpace: "pre-wrap", lineHeight: 1.6 }}>{query.query}</Text>
                    </div>
                    <Textarea
                        label="Your response"
                        placeholder="Answer the student's question..."
                        autosize
                        minRows={4}
                        maxRows={12}
                        required
                        key={form.key("response")}
                        {...form.getInputProps("response")}
                    />
                    <Button type="submit" loading={submitting}>Send response</Button>
                </Stack>
            </form>
        </Stack>
    );
};

export default AnswerForm;