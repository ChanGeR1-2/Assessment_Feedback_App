import {Button, Paper, PasswordInput, Select, Stack, TextInput} from "@mantine/core";
import {useForm, isEmail} from "@mantine/form";
import {useState} from "react";
import {notifications} from "@mantine/notifications";
import {createUser} from "../api/usersApi.js";

const CreateUserForm = () => {
    const [submitting, setSubmitting] = useState(false);

    const form = useForm({
        initialValues: {
            firstName: "",
            middleNames: "",
            lastName: "",
            email: "",
            password: "",
            role: ""
        },

        validate: {
            firstName: (value) => value.length > 2 ? null : 'First name must be at least 3 characters',
            lastName: (value) => value.length > 2 ? null : 'Last name must be at least 3 characters',
            email: (value) => isEmail(value) ? null : "Invalid email",
            password: (value) => (value.length > 8 ? null : 'Invalid password - must be at least 8 characters'),
            role: (value) => (value ? null : 'Please select a role'),
        }
    });

    const handleSubmit = async (values) => {
        setSubmitting(true);
        try {
            let fullName = values.firstName.trim() + " " + values.middleNames.trim() + " " + values.lastName.trim();
            await createUser({
                fullName,
                email: values.email,
                password: values.password,
                role: values.role
            });

            notifications.show({
                title: "User created successfully",
                message: "User created successfully"
            });
            form.reset();
        } catch (e) {
            notifications.show({
                title: "User creation failed",
                message: e.message,
                color: "red"
            })
        } finally {
            setSubmitting(false);
        }
    };

    return (

        <form onSubmit={form.onSubmit((values) => handleSubmit(values))}>
            <Stack>
                <TextInput
                    withAsterisk
                    label="Email"
                    placeholder="your@email.com"
                    key={form.key('email')}
                    {...form.getInputProps('email')}
                />
                <TextInput
                    withAsterisk
                    label="First name"
                    placeholder="Andrew"
                    key={form.key('firstName')}
                    {...form.getInputProps('firstName')}
                />
                <TextInput
                    label="Middle names"
                    placeholder="Harry Edward"
                    key={form.key('middleNames')}
                    {...form.getInputProps('middleNames')}
                />
                <TextInput
                    withAsterisk
                    label="Last name"
                    placeholder="Vine"
                    key={form.key('lastName')}
                    {...form.getInputProps('lastName')}
                />
                <PasswordInput
                    withAsterisk
                    label="Password"
                    placeholder="password"
                    key={form.key('password')}
                    {...form.getInputProps('password')}
                />
                <Select
                    withAsterisk
                    label="Role"
                    placeholder="Role"
                    data={['ADMIN', 'STUDENT', 'LECTURER']}
                    key={form.key('role')}
                    {...form.getInputProps('role')}
                />
                <Button
                    type="submit"
                    loading={submitting}
                >
                    Create user
                </Button>
            </Stack>
        </form>

    );
};

export default CreateUserForm;