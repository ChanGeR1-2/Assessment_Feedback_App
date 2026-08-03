import {useState} from "react";
import {useForm} from "@mantine/form";
import {notifications} from "@mantine/notifications";
import {Button, Stack, Textarea, TextInput} from "@mantine/core";
import {createPhrase, updatePhrase} from "../api/phrasesApi.js";

const FeedbackPhraseForm = ({onSuccess, phraseId, initialValues}) => {
    const [submitting, setSubmitting] = useState(false);
    const isEditing = Boolean(initialValues);

    const form = useForm({
        initialValues: {
            label: initialValues?.label ?? "",
            text: initialValues?.text ?? "",
        },
        validate: {
            label: (value) => (value.trim().length >= 1 ? null : "Label is required"),
            text: (value) => (value.trim().length >= 1 ? null : "Text is required"),
        },
    });

    const handleSubmit = async (values) => {
        setSubmitting(true);
        try {
            if (isEditing) {
                await updatePhrase({
                    phraseId,
                    phrase: values,
                })
            } else {
                await createPhrase({
                    phrase: values,
                })
            }

            notifications.show({
                title: isEditing ? "Phrase updated" : "Phrase created",
                message: `"${values.label}" has been saved.`,
            });
            onSuccess();
        } catch (e) {
            if (e.fieldErrors) {
                form.setErrors(e.fieldErrors);
                return;
            }
            notifications.show({
                title: isEditing ? "Update failed" : "Creation failed",
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
                <TextInput
                    withAsterisk
                    label="Label"
                    placeholder="Critical thinking, ..."
                    key={form.key("label")}
                    {...form.getInputProps("label")}
                />
                <Textarea
                    withAsterisk
                    label="Text"
                    description="Inserted into the comment box when selected"
                    placeholder="Your argument would be stronger with more engagement with primary sources."
                    autosize
                    minRows={3}
                    key={form.key("text")}
                    {...form.getInputProps("text")}
                />
                <Button type="submit" loading={submitting}>
                    {isEditing ? "Save changes" : "Create phrase"}
                </Button>
            </Stack>
        </form>
    );
};

export default FeedbackPhraseForm;