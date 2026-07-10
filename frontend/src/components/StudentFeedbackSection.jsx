import { Badge, Card, Divider, Group, Paper, RingProgress, Stack, Text, Title } from "@mantine/core";

const markColor = (mark) => {
    if (mark >= 70) return "teal";
    if (mark >= 50) return "blue";
    if (mark >= 40) return "yellow";
    return "red";
};

const StudentFeedbackSection = ({ feedback }) => {
    if (!feedback) return null;

    const formattedDate = feedback.createdAt
        ? new Date(feedback.createdAt).toLocaleDateString(undefined, {
            year: "numeric",
            month: "long",
            day: "numeric",
        })
        : null;

    const color = markColor(feedback.mark);

    return (
        <Stack gap="lg">
            <Group justify="space-between" align="flex-start" wrap="nowrap">
                <div>
                    <Title order={2}>Feedback</Title>
                    {formattedDate && (
                        <Text size="sm" c="dimmed">
                            Given on {formattedDate}
                        </Text>
                    )}
                </div>
                <RingProgress
                    size={90}
                    thickness={8}
                    roundCaps
                    sections={[{ value: feedback.mark, color }]}
                    label={
                        <Text ta="center" fw={700} size="lg">
                            {feedback.mark}
                        </Text>
                    }
                />
            </Group>

            <Paper withBorder p="md" radius="md" bg="var(--mantine-color-gray-0)">
                <Group gap="xl">
                    <div>
                        <Text size="xs" c="dimmed" tt="uppercase" fw={600}>
                            Student
                        </Text>
                        <Text fw={500}>{feedback.studentFullName ?? `#${feedback.studentId}`}</Text>
                    </div>
                    <div>
                        <Text size="xs" c="dimmed" tt="uppercase" fw={600}>
                            Assessment
                        </Text>
                        <Text fw={500}>{feedback.assessmentTitle ?? `#${feedback.assessmentId}`}</Text>
                    </div>
                </Group>
            </Paper>

            <Stack gap="md">
                <FeedbackField label="Strengths" value={feedback.strengths} />
                <Divider />
                <FeedbackField label="Areas for improvement" value={feedback.improvements} />
                <Divider />
                <FeedbackField label="Recommended actions" value={feedback.actions} />
            </Stack>
        </Stack>
    );
};

const FeedbackField = ({ label, value }) => (
    <div>
        <Text fw={600} mb={4} c="dark">
            {label}
        </Text>
        <Text c={value ? undefined : "dimmed"} style={{ whiteSpace: "pre-wrap", lineHeight: 1.6 }}>
            {value || "None provided"}
        </Text>
    </div>
);

export default StudentFeedbackSection;