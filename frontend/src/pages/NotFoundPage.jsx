import {Link} from "react-router";
import {Button, Stack, Title, Text} from "@mantine/core";
import {getCurrentUser} from "./auth/currentUser.js";

const NotFoundPage = ({ message }) => {
    const currentUser = getCurrentUser();
    const home = currentUser
        ? `/${currentUser.role.toLowerCase()}-dashboard`
        : "/login";

    return (
        <Stack align="center" gap="sm" py="xl">
            <Title order={2}>Page not found</Title>
            <Text c="dimmed" ta="center">
                {message ?? "That page doesn't exist or may have been moved."}
            </Text>
            <Button component={Link} to={home} mt="sm">Back to dashboard</Button>
        </Stack>
    );
};

export default NotFoundPage;