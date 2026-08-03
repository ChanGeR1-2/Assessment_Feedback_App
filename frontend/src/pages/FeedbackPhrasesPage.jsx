import { useEffect, useState} from "react";
import {useDisclosure} from "@mantine/hooks";
import {deletePhrase, getMyPhrases} from "../api/phrasesApi.js";
import {notifications} from "@mantine/notifications";
import {
    ActionIcon,
    Button,
    Group,
    Loader,
    Modal,
    Paper,
    Stack,
    Table,
    Text,
    Title, Tooltip
} from "@mantine/core";
import FeedbackPhraseForm from "../components/FeedbackPhraseForm.jsx";

const FeedbackPhrasesPage = () => {
    const [phrases, setPhrases] = useState([]);
    const [loading, setLoading] = useState(true);
    const [opened, { open, close }] = useDisclosure(false);
    const [selectedPhrase, setSelectedPhrase] = useState(null);

    const loadPhrases = async () => {
        try {
            const data = await getMyPhrases();
            setPhrases(data);
        } catch(error) {
            notifications.show({
                title: "Error",
                message: error.message,
                color: "red"
            });
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        loadPhrases();
    }, []);

    const handleCreateSuccess = () => {
        handleClose();
        loadPhrases();
    }

    const handleClose = () => {
        close();
        setSelectedPhrase(null);
    };

    const handleEdit = (item) => {
        setSelectedPhrase(item);
        open();
    };

    const handleAdd = () => {
        setSelectedPhrase(null);
        open();
    };

    const handleDelete = async (item) => {
        try {
            await deletePhrase({phraseId: item.id});
            await loadPhrases();
        } catch (e) {
            notifications.show({
                title: "Error",
                message: e.message,
                color: "red"
            })
        }
    }

    return (
        <Stack gap="lg">
            <Group justify="space-between" align="flex-start" wrap="nowrap">
                <div>
                    <Title order={1}>Phrase Bank</Title>
                    <Text c="dimmed" size="sm">
                        Add re-usable phrases to your feedback.
                    </Text>
                </div>
                <Button onClick={handleAdd}>Add phrase</Button>
            </Group>

            <Modal
                opened={opened}
                onClose={handleClose}
                title={selectedPhrase ? "Edit phrase" : "Create phrase"}
                centered
            >
                <FeedbackPhraseForm
                    key={selectedPhrase?.id ?? "new"}
                    phraseId={selectedPhrase?.id}
                    initialValues={selectedPhrase}
                    onSuccess={handleCreateSuccess}
                />
            </Modal>

            <Paper withBorder radius="md" style={{ overflow: "hidden" }}>
                {loading ? (
                    <Group justify="center" p="xl"><Loader /></Group>
                ) : phrases.length === 0 ? (
                    <Stack align="center" gap="xs" p="xl">
                        <Text c="dimmed">No phrases yet</Text>
                        <Text size="sm" c="dimmed">
                            Add phrases to speed up your feedback.
                        </Text>
                    </Stack>
                ) : (
                    <Table striped highlightOnHover verticalSpacing="sm">
                        <Table.Thead>
                            <Table.Tr>
                                <Table.Th>Label</Table.Th>
                                <Table.Th>Text</Table.Th>
                                <Table.Th w={160}>Actions</Table.Th>
                            </Table.Tr>
                        </Table.Thead>
                        <Table.Tbody>
                            {phrases.map((phrase) => (
                                <Table.Tr key={phrase.id}>
                                    <Table.Td fw={500}>{phrase.label}</Table.Td>
                                    <Table.Td><Text lineClamp={2}>{phrase.text}</Text></Table.Td>
                                    <Table.Td>
                                        <Group gap={4} wrap="nowrap">
                                            <Tooltip label="Edit">
                                                <ActionIcon variant="subtle" onClick={() => handleEdit(phrase)}>
                                                    ✎
                                                </ActionIcon>
                                            </Tooltip>
                                            <Tooltip label="Delete">
                                                <ActionIcon
                                                    variant="subtle"
                                                    color="red"
                                                    onClick={() => handleDelete(phrase)}
                                                >
                                                    ✕
                                                </ActionIcon>
                                            </Tooltip>
                                        </Group>
                                    </Table.Td>
                                </Table.Tr>
                            ))}
                        </Table.Tbody>
                    </Table>
                )}
            </Paper>
        </Stack>
    )
};

export default FeedbackPhrasesPage;