import {Group, Paper, Progress, Stack, Text} from "@mantine/core";

export const TagSummary = ({ counts, title, emptyMessage }) => {
    const strengths = counts.filter((c) => c.tagType === "STRENGTH");
    const improvements = counts.filter((c) => c.tagType === "IMPROVEMENT");
    const max = Math.max(1, ...counts.map((c) => c.count));

    if (counts.length === 0) {
        return (
            <Paper withBorder radius="md" p="xl">
                <Text c="dimmed" ta="center">{emptyMessage}</Text>
            </Paper>
        );
    }

    const column = (items, label, color) => (
        <Stack gap="xs" style={{ flex: 1 }}>
            <Text size="xs" c="dimmed" tt="uppercase" fw={600}>{label}</Text>
            {items.length === 0 ? (
                <Text size="sm" c="dimmed">None recorded</Text>
            ) : (
                items.map((c) => (
                    <div key={`${c.tagName}-${c.tagType}`}>
                        <Group justify="space-between" mb={2} wrap="nowrap">
                            <Text size="sm">{c.tagName}</Text>
                            <Text size="sm" fw={600}>{c.count}</Text>
                        </Group>
                        <Progress value={(c.count / max) * 100} size="sm" radius="xl" color={color} />
                    </div>
                ))
            )}
        </Stack>
    );

    return (
        <Paper withBorder radius="md" p="md">
            <Text fw={600} mb="md">{title}</Text>
            <Group align="flex-start" gap="xl" grow>
                {column(strengths, "Strengths", "teal")}
                {column(improvements, "Areas to work on", "orange")}
            </Group>
        </Paper>
    );
}