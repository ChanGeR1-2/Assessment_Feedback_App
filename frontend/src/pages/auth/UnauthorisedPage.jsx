import { Link } from "react-router";
import { Button, Stack, Text, Title } from "@mantine/core";
import { getCurrentUser } from "./currentUser.js";

const UnauthorisedPage = () => {
    const currentUser = getCurrentUser();
    const home = currentUser
        ? `/${currentUser.role.toLowerCase()}-dashboard`
        : "/login";

    return (
        <Stack align="center" gap="sm" py="xl">
            <Title order={2}>You don't have access to this page</Title>
            <Text c="dimmed" ta="center">
                This page is only available to users with a different role.
            </Text>
            <Button component={Link} to={home} mt="sm">
                {currentUser ? "Back to dashboard" : "Go to login"}
            </Button>
        </Stack>
    );
};

export default UnauthorisedPage;