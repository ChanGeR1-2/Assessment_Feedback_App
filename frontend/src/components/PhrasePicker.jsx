import { Button, Menu, Text } from "@mantine/core";

const PhrasePicker = ({ phrases, onInsert }) => {
    if (!phrases?.length) return null;

    return (
        <Menu shadow="md" width={320} position="bottom-end">
            <Menu.Target>
                <Button variant="subtle" size="compact-xs">Insert phrase</Button>
            </Menu.Target>
            <Menu.Dropdown mah={300} style={{ overflowY: "auto" }}>
                {phrases.map((p) => (
                    <Menu.Item key={p.id} onClick={() => onInsert(p.text)}>
                        <Text size="sm" fw={500}>{p.label}</Text>
                        <Text size="xs" c="dimmed" lineClamp={2}>{p.text}</Text>
                    </Menu.Item>
                ))}
            </Menu.Dropdown>
        </Menu>
    );
};

export default PhrasePicker;