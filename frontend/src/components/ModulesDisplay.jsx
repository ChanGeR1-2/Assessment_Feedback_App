import {Badge, Card, Divider, Group, Paper, SimpleGrid, Stack, Text, Title} from "@mantine/core";
import {Link} from "react-router";

const ModulesDisplay = ({modules, currentUser}) => {
    const years = new Map();
    for (const m of modules) {
        const y = m.academicYear ?? "Unknown";
        if (!years.has(y)) years.set(y, []);
        years.get(y).push(m);
    }
    const yearList = [...years.entries()].sort((a, b) => b[0].localeCompare(a[0]));

    return (
        <Stack gap="xl">
            <div>
                <Title order={1}>My modules</Title>
                <Text c="dimmed">
                    {currentUser.role === "STUDENT" ? "Select a module to see your feedback" : "Select a module to view/edit assessments"}
                </Text>
            </div>

            {yearList.length === 0 ? (
                <Paper withBorder radius="md" p="xl">
                    <Text c="dimmed" ta="center">
                        {currentUser.role === "STUDENT" ? "You aren't enrolled in any modules yet." : "You haven't been assigned to any modules yet."}
                    </Text>
                </Paper>
            ) : (
                yearList.map(([year, mods]) => (
                    <div key={year}>
                        <Group mb="sm">
                            <Title order={3}>{year}</Title>
                            <Badge variant="light">{mods.length}</Badge>
                        </Group>
                        <Divider mb="md"/>
                        <SimpleGrid cols={{base: 1, sm: 2, lg: 3}} spacing="md">
                            {mods.map((m) => {
                                const link = currentUser.role === "STUDENT" ? `/my-modules/${m.id}/feedback` : `/modules/${m.id}/assessments`;
                                return (
                                    <Card key={m.id} component={Link} to={link}
                                        withBorder radius="md" padding="lg" style={{textDecoration: "none"}}>
                                        <Text fw={600} c="dark">{m.code}</Text>
                                        <Text size="sm">{m.title}</Text>
                                        <Text size="xs" c="dimmed" mt={4}>
                                            {m.lecturerName ?? "No lecturer assigned"}
                                        </Text>
                                    </Card>)
                            })}
                        </SimpleGrid>
                    </div>
                ))
            )}
        </Stack>
    );
}
export default ModulesDisplay;