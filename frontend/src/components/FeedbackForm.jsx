import {
    Anchor,
    Breadcrumbs,
    Button,
    Group, Modal,
    MultiSelect,
    NumberInput,
    Paper,
    Stack,
    Text,
    Textarea,
    Title
} from "@mantine/core";
import {useEffect, useMemo, useState} from "react";
import {useForm} from "@mantine/form";
import {notifications} from "@mantine/notifications";
import {deleteFeedbackAudio, submitFeedback, updateFeedback} from "../api/feedbackApi.js";
import {Link} from "react-router";
import {useAudioRecorder} from "../hooks/useAudioRecorder.js";
import AudioPlayer from "./AudioPlayer.jsx";
import {getMyPhrases} from "../api/phrasesApi.js";
import PhrasePicker from "./PhrasePicker.jsx";
import {getAllTags} from "../api/tagsApi.js";

const FeedbackForm = ({
    assessmentId,
    studentId,
    onSubmit,
    studentFullName,
    assessmentTitle,
    markingItems,
    feedback,
    moduleId,
    moduleTitle,
}) => {
    const [submitting, setSubmitting] = useState(false);
    const isEditing = Boolean(feedback);
    const { recording, audioBlob, error, start, stop, reset } = useAudioRecorder();
    const previewUrl = useMemo(
        () => (audioBlob ? URL.createObjectURL(audioBlob) : null),
        [audioBlob]
    );
    const [audioVersion, setAudioVersion] = useState(0);
    const [phrases, setPhrases] = useState([]);
    const [tags, setTags] = useState([]);
    const [publishingFeedback, setPublishingFeedback] = useState(null);
    const [hasStoredAudio, setHasStoredAudio] = useState(false);
    useEffect(() => () => { if (previewUrl) URL.revokeObjectURL(previewUrl); }, [previewUrl]);

    useEffect(() => {
        getMyPhrases().then(setPhrases).catch(() => {});
        getAllTags().then(setTags).catch(() => {});
    }, []);

    const form = useForm({
        initialValues: {
            summary: feedback?.summary ?? "",
            items: markingItems.map((mi) => {
                const feedbackItem = feedback?.items?.find(i => i.markingItemId === mi.id);
                return {
                    markingItemId: mi.id,
                    awardedMark: feedbackItem?.awardedMark ?? 0,
                    comment: feedbackItem?.comment ?? ""
                }
            }),
            strengthTagIds: feedback?.tags
                ?.filter((t) => t.tagType === "STRENGTH")
                .map((t) => String(t.tagId)) ?? [],
            improvementTagIds: feedback?.tags
                ?.filter((t) => t.tagType === "IMPROVEMENT")
                .map((t) => String(t.tagId)) ?? [],
        },
        validate: {
            summary: (value) => (value.trim().length >= 10 ? null : "Summary must be at least 10 characters"),
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

    const tagOptions = tags.map((t) => ({ value: String(t.id), label: t.name }));

    const strengthOptions = tagOptions.filter(
        (o) => !form.values.improvementTagIds.includes(o.value)
    );
    const improvementOptions = tagOptions.filter(
        (o) => !form.values.strengthTagIds.includes(o.value)
    );

    const total = form.values.items.reduce((sum, i) => sum + (i.awardedMark || 0), 0);
    const maxTotal = markingItems.reduce((sum, mi) => sum + mi.maxMark, 0);

    const insertPhrase = (field, text) => {
        const current = field.split(".").reduce((obj, key) => obj?.[key], form.getValues()) ?? "";
        form.setFieldValue(field, current ? `${current} ${text}` : text);
    };

    const handleSubmit = async (values, publish) => {
        setSubmitting(true);
        const tags = [
            ...values.strengthTagIds.map((id) => ({ tagId: Number(id), tagType: "STRENGTH" })),
            ...values.improvementTagIds.map((id) => ({ tagId: Number(id), tagType: "IMPROVEMENT" })),
        ];

        const payload = {
            summary: values.summary,
            items: values.items,
            assessmentId,
            studentId,
            tags,
        };
        try {
            const savedFeedback = isEditing
                ? await updateFeedback({
                    feedbackId: feedback.id,
                    feedback: payload,
                    publish
                })
                : await submitFeedback({
                    feedback: payload,
                    publish
                });

            if (audioBlob) {
                try {
                    const formData = new FormData();
                    formData.append("file", audioBlob, "feedback.webm");
                    const res = await fetch(`/api/feedback/${savedFeedback.id}/audio`, {
                        method: "POST",
                        headers: { Authorization: `Bearer ${localStorage.getItem("token")}` },
                        body: formData,
                    });
                    if (!res.ok) throw new Error("Audio upload failed");
                    reset();
                    setAudioVersion((v) => v + 1);
                } catch {
                    notifications.show({
                        title: "Feedback saved, audio didn't upload",
                        message: "The written feedback was saved. You can add the recording later.",
                        color: "yellow",
                    });
                    onSubmit(savedFeedback);
                    return;
                }
            }
            onSubmit(savedFeedback);
        } catch (e) {
            if (e.fieldErrors) {
                form.setErrors(e.fieldErrors);
                return;
            }
            notifications.show({title: "Feedback creation failed", message: e.message, color: "red"});
        } finally {
            setSubmitting(false);
        }
    };

    const confirmPublish = async () => {
        try {
            await handleSubmit(publishingFeedback, true);
            setPublishingFeedback(null);
        } catch (error) {
            notifications.show({ title: "Error", message: error.message, color: "red" });
        }
    };

    const handleAudioDelete = async () => {
        try {
            await deleteFeedbackAudio({feedbackId: feedback.id});
            setAudioVersion((v) => v + 1);
            notifications.show({ title: "Audio deleted", message: "The audio recording has been deleted.", color: "green" });
            reset();
        } catch (error) {
            notifications.show({ title: "Error", message: error.message, color: "red" });
        }
    }

    return (
        <Stack gap="lg">
            <Breadcrumbs>
                <Anchor component={Link} to={`/modules/${moduleId}/assessments`} size="sm">
                    {moduleTitle ?? "Assessments"}
                </Anchor>
                <Anchor component={Link} to={`/modules/${moduleId}/assessments/${assessmentId}/students`} size="sm">
                    {assessmentTitle ?? "Submissions"}
                </Anchor>
                <Text size="sm" c="dimmed">{studentFullName ?? "Student"}</Text>
            </Breadcrumbs>
            <Stack gap="xs" mb="lg">
                <Title order={2}>{isEditing ? "Edit Feedback" : "Create Feedback"}</Title>
                <Text c="dimmed" size="sm">
                    {assessmentTitle} · {studentFullName}
                </Text>
            </Stack>

            <form>
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
                            <Group justify="space-between" mb={4}>
                                <Text size="sm" fw={500}>Comments</Text>
                                <PhrasePicker phrases={phrases} onInsert={(text) => insertPhrase(`items.${index}.comment`, text)} />
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

                    <Group justify="space-between" mb={4}>
                        <Text size="lg" fw={700}>Overall summary</Text>
                        <PhrasePicker phrases={phrases} onInsert={(text) => insertPhrase("summary", text)} />
                    </Group>
                    <Textarea
                        withAsterisk
                        autosize
                        minRows={3}
                        {...form.getInputProps("summary")}
                    />

                    <Paper withBorder p="md" radius="md">
                        <Text fw={600} mb="xs">Themes (optional)</Text>
                        <Stack gap="md">
                            <MultiSelect
                                label="Strengths"
                                placeholder="What did they do consistently well?"
                                data={strengthOptions}
                                searchable
                                clearable
                                {...form.getInputProps("strengthTagIds")}
                            />
                            <MultiSelect
                                label="Areas for improvement"
                                placeholder="What should they focus on?"
                                data={improvementOptions}
                                searchable
                                clearable
                                {...form.getInputProps("improvementTagIds")}
                            />
                        </Stack>
                    </Paper>

                    <Paper withBorder p="md" radius="md">
                        <Text fw={600} mb="xs">Audio feedback (optional)</Text>

                        {isEditing && (
                            <AudioPlayer
                                feedbackId={feedback.id}
                                label="Current recording"
                                refreshKey={audioVersion}
                                onLoaded={setHasStoredAudio}
                            />
                        )}

                        {error && <Text size="sm" c="red" mb="xs">{error}</Text>}

                        {!audioBlob ? (
                            <Group>
                                <Button
                                    variant="light"
                                    color={recording ? "red" : "blue"}
                                    onClick={recording ? stop : start}
                                    mt={isEditing ? "sm" : undefined}
                                >
                                    {recording ? "Stop recording" : (isEditing && hasStoredAudio ? "Record new audio" : "Record audio")}
                                </Button>
                                {isEditing && hasStoredAudio && !recording && (
                                    <Button
                                        variant="subtle"
                                        color="red"
                                        onClick={handleAudioDelete}
                                        mt="sm"
                                    >
                                        Delete recording
                                    </Button>
                                )}
                            </Group>
                        ) : (
                            <Stack gap="sm">
                                {isEditing && (
                                    <Text size="xs" c="dimmed" tt="uppercase" fw={600}>New recording</Text>
                                )}
                                <audio controls src={previewUrl} />
                                <Button variant="subtle" color="red" onClick={reset}>
                                    Discard and re-record
                                </Button>
                            </Stack>
                        )}
                    </Paper>

                    <Modal opened={!!publishingFeedback} onClose={() => setPublishingFeedback(null)} title="Publish Feedback" centered>
                        <Text>Publish Feedback? This cannot be undone.</Text>
                        <Group justify="flex-end" mt="lg">
                            <Button variant="default" onClick={() => setPublishingFeedback(null)}>Cancel</Button>
                            <Button color="red" onClick={confirmPublish}>Publish</Button>
                        </Group>
                    </Modal>


                    <Group justify="flex-end">
                        <Button variant="default" loading={submitting}
                            onClick={() => form.onSubmit((values) => handleSubmit(values, false))()}>
                            Save as draft
                        </Button>
                        <Button loading={submitting}
                            onClick={() => setPublishingFeedback(form.values)}>
                            {isEditing ? "Save and publish" : "Publish"}
                        </Button>
                    </Group>
                </Stack>
            </form>
        </Stack>
    );
};

export default FeedbackForm;