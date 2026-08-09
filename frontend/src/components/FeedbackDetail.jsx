import { Badge, Divider, Group, Paper, Progress, RingProgress, Stack, Text, Title } from "@mantine/core";
import AudioPlayer from "./AudioPlayer.jsx";

const markColor = (percentage) => (percentage >= 40 ? "teal" : "red");

const FeedbackDetail = ({ feedback }) => {
    if (!feedback) return null;

    const formattedDate = feedback.createdAt
        ? new Date(feedback.createdAt).toLocaleDateString(undefined, {
            year: "numeric", month: "long", day: "numeric",
        })
        : null;

    const items = feedback.items ?? [];
    const tags = feedback.tags ?? [];
    const strengths = tags.filter((t) => t.tagType === "STRENGTH");
    const improvements = tags.filter((t) => t.tagType === "IMPROVEMENT");

    const percentage = feedback.totalMark
        ? Math.round((feedback.mark / feedback.totalMark) * 100)
        : 0;
    const color = markColor(percentage);

    return (
        <Stack gap="lg">
            <Group justify="space-between" align="flex-start" wrap="nowrap">
                <div>
                    <Title order={2}>Feedback</Title>
                    {formattedDate && <Text size="sm" c="dimmed">Given on {formattedDate}</Text>}
                </div>
                <Stack gap={4} align="center">
                    <RingProgress
                        size={100}
                        thickness={9}
                        roundCaps
                        sections={[{ value: percentage, color }]}
                        label={
                            <Stack gap={0} align="center">
                                <Text fw={700} size="lg" lh={1}>{feedback.mark}</Text>
                                <Text size="xs" c="dimmed">/ {feedback.totalMark}</Text>
                            </Stack>
                        }
                    />
                    <Text size="sm" fw={600} c={color}>{percentage}%</Text>
                </Stack>
            </Group>

            <Paper withBorder p="md" radius="md" bg="var(--mantine-color-gray-0)">
                <Group gap="xl">
                    <div>
                        <Text size="xs" c="dimmed" tt="uppercase" fw={600}>Student</Text>
                        <Text fw={500}>{feedback.studentFullName ?? `#${feedback.studentId}`}</Text>
                    </div>
                    <div>
                        <Text size="xs" c="dimmed" tt="uppercase" fw={600}>Assessment</Text>
                        <Text fw={500}>{feedback.assessmentTitle ?? `#${feedback.assessmentId}`}</Text>
                    </div>
                    <div>
                        <Text size="xs" c="dimmed" tt="uppercase" fw={600}>Marked by</Text>
                        <Text fw={500}>{feedback.lecturerFullName ?? "—"}</Text>
                    </div>
                </Group>
            </Paper>

            {feedback.summary && (
                <Paper withBorder p="md" radius="md">
                    <Text fw={600} mb={4}>Overall summary</Text>
                    <Text style={{ whiteSpace: "pre-wrap", lineHeight: 1.6 }}>{feedback.summary}</Text>
                </Paper>
            )}

            {tags.length > 0 && (
                <Paper withBorder p="md" radius="md">
                    <Stack gap="sm">
                        {strengths.length > 0 && (
                            <div>
                                <Text size="xs" c="dimmed" tt="uppercase" fw={600} mb={6}>Strengths</Text>
                                <Group gap="xs">
                                    {strengths.map((t) => (
                                        <Badge key={t.id} variant="light" color="teal">{t.tagName}</Badge>
                                    ))}
                                </Group>
                            </div>
                        )}
                        {improvements.length > 0 && (
                            <div>
                                <Text size="xs" c="dimmed" tt="uppercase" fw={600} mb={6}>Areas for improvement</Text>
                                <Group gap="xs">
                                    {improvements.map((t) => (
                                        <Badge key={t.id} variant="light" color="orange">{t.tagName}</Badge>
                                    ))}
                                </Group>
                            </div>
                        )}
                    </Stack>
                </Paper>
            )}

            <Stack gap="md">
                {items.map((item, index) => {
                    const itemPct = (item.awardedMark / item.maxMark) * 100;
                    return (
                        <div key={item.id}>
                            {index > 0 && <Divider mb="md" />}
                            <Group justify="space-between" mb={6} wrap="nowrap">
                                <Text fw={600}>{item.markingItemName}</Text>
                                <Text fw={600} size="sm" style={{ whiteSpace: "nowrap" }}>
                                    {item.awardedMark} / {item.maxMark}
                                </Text>
                            </Group>
                            <Progress value={itemPct} size="sm" radius="xl" mb="xs" color={markColor(itemPct)} />
                            <Text
                                c={item.comment ? undefined : "dimmed"}
                                style={{ whiteSpace: "pre-wrap", lineHeight: 1.6 }}
                            >
                                {item.comment || "No comment"}
                            </Text>
                        </div>
                    );
                })}
            </Stack>

            <AudioPlayer feedbackId={feedback.id} label="Audio feedback" />
        </Stack>
    );
};

export default FeedbackDetail;