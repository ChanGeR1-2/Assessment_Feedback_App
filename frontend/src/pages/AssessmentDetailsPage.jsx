import { useCallback, useEffect, useState } from "react";
import { Link, useParams } from "react-router";
import {deleteMarkingItem, getAssessmentById, reorderMarkingItems} from "../api/assessmentsApi.js";
import { notifications } from "@mantine/notifications";
import {
    ActionIcon, Anchor, Badge, Breadcrumbs, Button, Card, Group, Loader,
    Modal, Paper, Stack, Table, Text, Title, Tooltip
} from "@mantine/core";
import { useDisclosure } from "@mantine/hooks";
import MarkingItemForm from "../components/MarkingItemForm.jsx";

const AssessmentDetailsPage = () => {
    const [assessment, setAssessment] = useState({ markingItems: [] });
    const [loading, setLoading] = useState(true);
    const { moduleId, assessmentId } = useParams();
    const [opened, { open, close }] = useDisclosure(false);
    const [editingItem, setEditingItem] = useState(null);

    const loadAssessment = useCallback(async () => {
        try {
            setLoading(true);
            const data = await getAssessmentById({ assessmentId });
            setAssessment(data);
        } catch (error) {
            notifications.show({ title: "Error", message: error.message, color: "red" });
        } finally {
            setLoading(false);
        }
    }, [assessmentId]);

    useEffect(() => {
        loadAssessment();
    }, [loadAssessment]);

    const handleCreateSuccess = () => {
        handleClose();
        loadAssessment();
    };

    const handleEdit = (item) => {
        setEditingItem(item);
        open();
    };

    const handleAdd = () => {
        setEditingItem(null);
        open();
    };

    const handleClose = () => {
        close();
        setEditingItem(null);
    };

    const markingItems = [...(assessment.markingItems ?? [])].sort(
        (a, b) => a.position - b.position
    );
    const totalMarks = markingItems.reduce((sum, item) => sum + item.maxMark, 0);

    const formattedDueDate = assessment.dueDate
        ? new Date(assessment.dueDate).toLocaleDateString(undefined, {
            day: "numeric", month: "long", year: "numeric"
        })
        : null;

    const handleMove = async (item, direction) => {
        const index = markingItems.findIndex(i => i.id === item.id);
        const target = index + direction;
        if (target < 0 || target >= markingItems.length) return;

        const reordered = [...markingItems];
        [reordered[index], reordered[target]] = [reordered[target], reordered[index]];
        console.log(reordered);
        try {
            await reorderMarkingItems({assessmentId, orderedIds: reordered.map(i => i.id)});
            await loadAssessment();
        } catch (error) {
            notifications.show({
                title: "Error",
                message: error.message,
                color: "red"
            })
        }
    };

    const handleDelete = async (item) => {
        try {
            await deleteMarkingItem({assessmentId, markingItemId: item.id});
            await loadAssessment();
        } catch (error) {
            notifications.show({
                title: "Error",
                message: error.message,
                color: "red"
            })
        }
    }


    return (
        <Stack gap="lg">
            <Breadcrumbs>
                <Anchor component={Link} to="/modules" size="sm">Modules</Anchor>
                <Anchor component={Link} to={`/modules/${moduleId}/assessments`} size="sm">
                    {assessment.moduleTitle ?? "Assessments"}
                </Anchor>
                <Text size="sm" c="dimmed">{assessment.title}</Text>
            </Breadcrumbs>

            <Group justify="space-between" align="flex-start" wrap="nowrap">
                <div>
                    <Title order={1}>{assessment.title}</Title>
                    <Text c="dimmed" size="sm">
                        {formattedDueDate ? `Due ${formattedDueDate}` : "No due date set"}
                    </Text>
                </div>
                <Button onClick={handleAdd}>Add marking item</Button>
            </Group>

            <Group gap="md">
                <Card withBorder radius="md" padding="md" style={{ minWidth: 140 }}>
                    <Text size="xs" c="dimmed" tt="uppercase" fw={600}>Marking items</Text>
                    <Text fw={700} size="xl">{markingItems.length}</Text>
                </Card>
                <Card withBorder radius="md" padding="md" style={{ minWidth: 140 }}>
                    <Text size="xs" c="dimmed" tt="uppercase" fw={600}>Total marks</Text>
                    <Text fw={700} size="xl">{totalMarks}</Text>
                </Card>
            </Group>

            <Modal
                opened={opened}
                onClose={handleClose}
                title={editingItem ? "Edit marking item" : "Create marking item"}
                centered
            >
                <MarkingItemForm
                    key={editingItem?.id ?? "new"}
                    assessmentId={assessmentId}
                    initialValues={editingItem}
                    position={markingItems.length}
                    onSuccess={handleCreateSuccess}
                />
            </Modal>

            <Paper withBorder radius="md" style={{ overflow: "hidden" }}>
                {loading ? (
                    <Group justify="center" p="xl"><Loader /></Group>
                ) : markingItems.length === 0 ? (
                    <Stack align="center" gap="xs" p="xl">
                        <Text c="dimmed">No marking items yet</Text>
                        <Text size="sm" c="dimmed">
                            Add criteria or questions to define how this assessment is marked.
                        </Text>
                    </Stack>
                ) : (
                    <Table striped highlightOnHover verticalSpacing="sm">
                        <Table.Thead>
                            <Table.Tr>
                                <Table.Th w={60}>#</Table.Th>
                                <Table.Th>Name</Table.Th>
                                <Table.Th w={120}>Max mark</Table.Th>
                                <Table.Th w={160}>Actions</Table.Th>
                            </Table.Tr>
                        </Table.Thead>
                        <Table.Tbody>
                            {markingItems.map((item, index) => (
                                <Table.Tr key={item.id}>
                                    <Table.Td>
                                        <Badge variant="default" radius="sm">{index + 1}</Badge>
                                    </Table.Td>
                                    <Table.Td fw={500}>{item.name}</Table.Td>
                                    <Table.Td>{item.maxMark}</Table.Td>
                                    <Table.Td>
                                        <Group gap={4} wrap="nowrap">
                                            <Tooltip label="Move up">
                                                <ActionIcon
                                                    variant="subtle"
                                                    disabled={index === 0}
                                                    onClick={() => handleMove(item, -1)}
                                                >
                                                    ↑
                                                </ActionIcon>
                                            </Tooltip>
                                            <Tooltip label="Move down">
                                                <ActionIcon
                                                    variant="subtle"
                                                    disabled={index === markingItems.length - 1}
                                                    onClick={() => handleMove(item, 1)}
                                                >
                                                    ↓
                                                </ActionIcon>
                                            </Tooltip>
                                            <Tooltip label="Edit">
                                                <ActionIcon variant="subtle" onClick={() => handleEdit(item)}>
                                                    ✎
                                                </ActionIcon>
                                            </Tooltip>
                                            <Tooltip label="Delete">
                                                <ActionIcon
                                                    variant="subtle"
                                                    color="red"
                                                    onClick={() => handleDelete(item)}
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
    );
};

export default AssessmentDetailsPage;