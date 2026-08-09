import { Button, Divider, Group, Modal, Stack, Text } from "@mantine/core";
import { useDisclosure } from "@mantine/hooks";
import { useEffect, useState } from "react";
import { getFeedbackQueryByFeedbackId } from "../api/feedbackApi.js";
import CreateFeedbackQueryForm from "./CreateFeedbackQueryForm.jsx";

const FeedbackQuerySection = ({ feedbackId }) => {
    const [query, setQuery] = useState(null);
    const [askOpened, { open: openAsk, close: closeAsk }] = useDisclosure(false);
    const [answerOpened, { open: openAnswer, close: closeAnswer }] = useDisclosure(false);

    useEffect(() => {
        if (!feedbackId) return;
        let cancelled = false;
        getFeedbackQueryByFeedbackId({ feedbackId })
            .then((data) => { if (!cancelled) setQuery(data); })
            .catch(() => {});
        return () => { cancelled = true; };
    }, [feedbackId]);

    const handleCreated = (created) => {
        setQuery(created);
        closeAsk();
    };

    return (
        <>
            <Divider />
            {!query ? (
                <Group justify="space-between">
                    <Text size="sm" c="dimmed">Have a question about this feedback?</Text>
                    <Button variant="light" onClick={openAsk}>Ask a question</Button>
                </Group>
            ) : query.answer ? (
                <Group justify="space-between">
                    <Text size="sm" c="dimmed">Your lecturer has responded.</Text>
                    <Button variant="light" onClick={openAnswer}>View answer</Button>
                </Group>
            ) : (
                <Group justify="space-between">
                    <Text size="sm" c="dimmed">Your question is awaiting an answer.</Text>
                    <Button variant="light" disabled>Awaiting answer</Button>
                </Group>
            )}

            <Modal opened={askOpened} onClose={closeAsk} title="Ask a question" centered>
                <CreateFeedbackQueryForm
                    key={feedbackId}
                    feedbackId={feedbackId}
                    onSuccess={handleCreated}
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
        </>
    );
};

export default FeedbackQuerySection;