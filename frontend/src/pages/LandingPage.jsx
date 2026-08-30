import { Link } from "react-router";
import { Button, Container, Stack, Text, Title } from "@mantine/core";
import { getCurrentUser } from "./auth/currentUser.js";

const LandingPage = () => {
    const currentUser = getCurrentUser();

    return (
        <Container size="sm" py="xl">
            <Stack gap="md" align="center">
                <Title order={1} ta="center">Assessment Feedback</Title>
                <Text c="dimmed" ta="center" size="lg">
                    Structured, criterion-based feedback on university assessments — for
                    lecturers giving it and students receiving it.
                </Text>
                {currentUser ? (
                    <Button component={Link} to={`/${currentUser.role.toLowerCase()}-dashboard`} mt="md">
                        Go to dashboard
                    </Button>
                ) : (
                    <Button component={Link} to="/login" mt="md">
                        Log in
                    </Button>
                )}
            </Stack>
        </Container>
    );
};

export default LandingPage;