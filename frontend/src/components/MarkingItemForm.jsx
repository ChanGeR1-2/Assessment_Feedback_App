import { Button, NumberInput, Stack, TextInput } from "@mantine/core";
import { useForm } from "@mantine/form";
import { useState } from "react";
import { notifications } from "@mantine/notifications";
import { createMarkingItem, editMarkingItem } from "../api/assessmentsApi.js";

const MarkingItemForm = ({ onSuccess, assessmentId, position, initialValues }) => {
    const [submitting, setSubmitting] = useState(false);
    const isEditing = Boolean(initialValues);

    const form = useForm({
        initialValues: {
            name: initialValues?.name ?? "",
            maxMark: initialValues?.maxMark ?? 0,
        },
        validate: {
            name: (value) => (value.trim().length >= 1 ? null : "Name is required"),
            maxMark: (value) => (value > 0 ? null : "Max mark must be greater than 0"),
        },
    });

    const handleSubmit = async (values) => {
        setSubmitting(true);
        try {
            if (isEditing) {
                await editMarkingItem({
                    assessmentId,
                    markingItemId: initialValues.id,
                    markingItem: values,
                });
            } else {
                await createMarkingItem({
                    assessmentId,
                    markingItem: { ...values, position },
                });
            }

            notifications.show({
                title: isEditing ? "Marking item updated" : "Marking item created",
                message: `"${values.name}" has been saved.`,
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
                    label="Name"
                    placeholder="Q1, Analysis, ..."
                    key={form.key("name")}
                    {...form.getInputProps("name")}
                />
                <NumberInput
                    withAsterisk
                    label="Max mark"
                    placeholder="10"
                    min={1}
                    clampBehavior="strict"
                    allowDecimal={false}
                    key={form.key("maxMark")}
                    {...form.getInputProps("maxMark")}
                />
                <Button type="submit" loading={submitting}>
                    {isEditing ? "Save changes" : "Create marking item"}
                </Button>
            </Stack>
        </form>
    );
};

export default MarkingItemForm;