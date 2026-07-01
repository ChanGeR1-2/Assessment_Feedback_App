import { useState } from "react";
import { useNavigate } from "react-router";
import {
    Button,
    Paper,
    PasswordInput,
    Stack,
    Text,
    TextInput,
    Title
} from "@mantine/core";
import { useForm } from "@mantine/form";
import { notifications } from "@mantine/notifications";

import { loginUser } from "../../api/authApi.js";

const LoginPage = () => {
    const navigate = useNavigate();
    const [submitting, setSubmitting] = useState(false);

    const form = useForm({
        initialValues: {
            email: "",
            password: ""
        },

        validate: {
            email: (value) =>
                /^\S+@\S+$/.test(value) ? null : "Enter a valid email",
            password: (value) =>
                value.length > 0 ? null : "Password is required"
        }
    });

    async function handleSubmit(values) {
        setSubmitting(true);

        try {
            const user = await loginUser(values);

            localStorage.setItem("currentUser", JSON.stringify(user));

            notifications.show({
                title: "Login successful",
                message: `Welcome back, ${user.fullName}`
            });

            if (user.role === "ADMIN") {
                navigate("/admin");
            } else if (user.role === "LECTURER") {
                navigate("/lecturer-dashboard");
            } else {
                navigate("/student-dashboard");
            }
        } catch (error) {
            notifications.show({
                title: "Login failed",
                message: error.message,
                color: "red"
            });
        } finally {
            setSubmitting(false);
        }
    }

    return (
        <main className="login-page">
            <Paper withBorder shadow="sm" radius="md" p="xl" maw={420} w="100%">
                <Stack>
                    <div>
                        <Title order={1}>Login</Title>
                        <Text c="dimmed" size="sm">
                            Access your assessment feedback dashboard
                        </Text>
                    </div>

                    <form onSubmit={form.onSubmit(handleSubmit)}>
                        <Stack>
                            <TextInput
                                label="Email"
                                placeholder="name@example.com"
                                required
                                {...form.getInputProps("email")}
                            />

                            <PasswordInput
                                label="Password"
                                placeholder="Your password"
                                {...form.getInputProps("password")}
                            />

                            <Button type="submit" loading={submitting}>
                                Login
                            </Button>
                        </Stack>
                    </form>
                </Stack>
            </Paper>
        </main>
    );
};

export default LoginPage;