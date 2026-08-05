import {
    Anchor, Badge,
    Breadcrumbs,
    Button,
    Divider,
    Group,
    Modal,
    Paper,
    Progress,
    RingProgress,
    Stack,
    Text,
    Title
} from "@mantine/core";
import { useDisclosure } from "@mantine/hooks";
import { useEffect, useState } from "react";
import { getCurrentUser } from "../pages/auth/currentUser.js";
import { getFeedbackQueryByFeedbackId } from "../api/feedbackApi.js";
import CreateFeedbackQueryForm from "./CreateFeedbackQueryForm.jsx";
import {Link} from "react-router";
import AudioPlayer from "./AudioPlayer.jsx";

const markColor = (percentage) => (percentage >= 40 ? "teal" : "red");

const StudentFeedbackSection = ({ feedback }) => {
    const currentUser = getCurrentUser();
    const [query, setQuery] = useState(null);
    const [opened, { open, close }] = useDisclosure(false);
    const [answerOpened, { open: openAnswer, close: closeAnswer }] = useDisclosure(false);

    const isOwningStudent =
        currentUser?.role === "STUDENT" && currentUser?.id === feedback?.studentId;

    useEffect(() => {
        if (!feedback?.id) return;
        let cancelled = false;
        getFeedbackQueryByFeedbackId({ feedbackId: feedback.id })
            .then((data) => {
                if (!cancelled) setQuery(data);
            })
            .catch(() => {});
        return () => { cancelled = true; };
    }, [feedback?.id]);

    if (!feedback) return null;

    const handleQueryCreated = (created) => {
        setQuery(created);
        close();
    };

    const formattedDate = feedback.createdAt
        ? new Date(feedback.createdAt).toLocaleDateString(undefined, {
            year: "numeric", month: "long", day: "numeric",
        })
        : null;

    const items = feedback.items ?? [];
    const percentage = feedback.totalMark
        ? Math.round((feedback.mark / feedback.totalMark) * 100)
        : 0;
    const color = markColor(percentage);

    return (
        <Stack gap="lg">
            {currentUser?.role === "LECTURER" && (
                <Breadcrumbs>
                    <Anchor component={Link} to={`/modules/${feedback.moduleId}/assessments`} size="sm">
                        {feedback.moduleTitle ?? "Assessments"}
                    </Anchor>
                    <Anchor component={Link} to={`/modules/${feedback.moduleId}/assessments/${feedback.assessmentId}/students`} size="sm">
                        {feedback.assessmentTitle ?? "Submissions"}
                    </Anchor>
                    <Text size="sm" c="dimmed">{feedback.studentFullName ?? "Student"}</Text>
                </Breadcrumbs>
            )}
            {currentUser?.role === "STUDENT" && (
                <Breadcrumbs>
                    <Anchor component={Link} to={`/my-modules`} size="sm">
                        Modules
                    </Anchor>
                    <Anchor component={Link} to={`/my-modules/${feedback.moduleId}/feedback`} size="sm">
                        {feedback.moduleTitle ?? "Assessments"}
                    </Anchor>
                    <Text size="sm" c="dimmed">{feedback.assessmentTitle}</Text>
                </Breadcrumbs>
            )}
            <Group justify="space-between" align="flex-start" wrap="nowrap">
                <div>
                    <Title order={2}>Feedback</Title>
                    {formattedDate && <Text size="sm" c="dimmed">Given on {formattedDate}</Text>}
                </div>
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

            {feedback.tags?.length > 0 && (
                <Paper withBorder p="md" radius="md">
                    <Stack gap="sm">
                        {feedback.tags.some((t) => t.tagType === "STRENGTH") && (
                            <div>
                                <Text size="xs" c="dimmed" tt="uppercase" fw={600} mb={6}>Strengths</Text>
                                <Group gap="xs">
                                    {feedback.tags
                                        .filter((t) => t.tagType === "STRENGTH")
                                        .map((t) => (
                                            <Badge key={t.id} variant="light" color="teal">{t.tagName}</Badge>
                                        ))}
                                </Group>
                            </div>
                        )}
                        {feedback.tags.some((t) => t.tagType === "IMPROVEMENT") && (
                            <div>
                                <Text size="xs" c="dimmed" tt="uppercase" fw={600} mb={6}>Areas for improvement</Text>
                                <Group gap="xs">
                                    {feedback.tags
                                        .filter((t) => t.tagType === "IMPROVEMENT")
                                        .map((t) => (
                                            <Badge key={t.id} variant="light" color="orange">{t.tagName}</Badge>
                                        ))}
                                </Group>
                            </div>
                        )}
                    </Stack>
                </Paper>
            )}

            <Stack gap="md">
                {items.map((item, index) => (
                    <div key={item.id}>
                        {index > 0 && <Divider mb="md" />}
                        <Group justify="space-between" mb={6} wrap="nowrap">
                            <Text fw={600}>{item.markingItemName}</Text>
                            <Text fw={600} size="sm" style={{ whiteSpace: "nowrap" }}>
                                {item.awardedMark} / {item.maxMark}
                            </Text>
                        </Group>
                        <Progress
                            value={(item.awardedMark / item.maxMark) * 100}
                            size="sm"
                            radius="xl"
                            mb="xs"
                            color={markColor((item.awardedMark / item.maxMark) * 100)}
                        />
                        <Text
                            c={item.comment ? undefined : "dimmed"}
                            style={{ whiteSpace: "pre-wrap", lineHeight: 1.6 }}
                        >
                            {item.comment || "No comment"}
                        </Text>
                    </div>
                ))}
            </Stack>

            <AudioPlayer feedbackId={feedback.id} label="Audio feedback" />

            {isOwningStudent && (
                <>
                    <Divider />
                    {!query ? (
                        <Group justify="space-between">
                            <Text size="sm" c="dimmed">
                                Have a question about this feedback?
                            </Text>
                            <Button variant="light" onClick={open}>Ask a question</Button>
                        </Group>
                    ) : query.answer ? (
                        <Group justify="space-between">
                            <Text size="sm" c="dimmed">Your lecturer has responded.</Text>
                            <Button variant="light" onClick={openAnswer}>View answer</Button>
                        </Group>
                    ) : (
                        <Group justify="space-between">
                            <Text size="sm" c="dimmed">Your question is awaiting a answer.</Text>
                            <Button variant="light" disabled>Awaiting answer</Button>
                        </Group>
                    )}
                </>
            )}

            <Modal opened={opened} onClose={close} title="Ask a question" centered>
                <CreateFeedbackQueryForm
                    key={feedback.id}
                    feedbackId={feedback.id}
                    onSuccess={handleQueryCreated}
                />
            </Modal>

            <Modal opened={answerOpened} onClose={closeAnswer} title="Question and answer" centered>
                {query && (
                    <Stack gap="md">
                        <div>
                            <Text size="xs" c="dimmed" tt="uppercase" fw={600}>Your question</Text>
                            <Text style={{ whiteSpace: "pre-wrap", lineHeight: 1.6 }}>{query.query}</Text>
                        </div>
                        <Divider />
                        <div>
                            <Text size="xs" c="dimmed" tt="uppercase" fw={600}>
                                Answer from {query.answer?.lecturerFullName ?? "your lecturer"}
                            </Text>
                            <Text style={{ whiteSpace: "pre-wrap", lineHeight: 1.6 }}>
                                {query.answer?.answer}
                            </Text>
                        </div>
                    </Stack>
                )}
            </Modal>
        </Stack>
    );
};

export default StudentFeedbackSection;