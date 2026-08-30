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
import UnauthorisedPage from "./auth/UnauthorisedPage.jsx";
import NotFoundPage from "./NotFoundPage.jsx";

const AssessmentDetailsPage = () => {
    const [assessment, setAssessment] = useState({ markingItems: [] });
    const [loading, setLoading] = useState(true);
    const { moduleId, assessmentId } = useParams();
    const [opened, { open, close }] = useDisclosure(false);
    const [editingItem, setEditingItem] = useState(null);
    const [deletingItem, setDeletingItem] = useState(null);
    const [notFound, setNotFound] = useState(false);
    const [forbidden, setForbidden] = useState(false);

    const loadAssessment = useCallback(async () => {
        try {
            const data = await getAssessmentById({ assessmentId });
            setAssessment(data);
        } catch (error) {
            if (error.status === 404) { setNotFound(true); return; }
            if (error.status === 403) { setForbidden(true); return; }
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

    const confirmDelete = async () => {
        try {
            await deleteMarkingItem({ assessmentId, markingItemId: deletingItem.id });
            setDeletingItem(null);
            await loadAssessment();
        } catch (error) {
            notifications.show({ title: "Error", message: error.message, color: "red" });
        }
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

    const formattedFeedbackDueDate = assessment.feedbackDueDate
        ? new Date(assessment.feedbackDueDate).toLocaleDateString(undefined, {
            day: "numeric", month: "long", year: "numeric"
        })
        : null;

    const handleMove = async (item, direction) => {
        const index = markingItems.findIndex(i => i.id === item.id);
        const target = index + direction;
        if (target < 0 || target >= markingItems.length) return;

        const reordered = [...markingItems];
        [reordered[index], reordered[target]] = [reordered[target], reordered[index]];
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

    if (loading) {
        return <Group justify="center" p="xl"><Loader /></Group>;
    }

    if (notFound) {
        return <NotFoundPage message="That assessment doesn't exist, or you don't have access to it." />;
    }

    if (forbidden) {
        return <UnauthorisedPage />;
    }

    return (
        <Stack gap="lg">
            <Breadcrumbs>
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
                    <Text c="dimmed" size="sm">
                        {formattedFeedbackDueDate ? `Feedback due ${formattedFeedbackDueDate}` : "No feedback due date set"}
                    </Text>
                </div>
                {!assessment.isRubricLocked && (
                    <Button onClick={handleAdd}>Add marking item</Button>
                )}
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

            <Modal opened={!!deletingItem} onClose={() => setDeletingItem(null)}
                title="Delete marking item" centered>
                <Text>Delete "{deletingItem?.name}"? This cannot be undone.</Text>
                <Group justify="flex-end" mt="lg">
                    <Button variant="default" onClick={() => setDeletingItem(null)}>Cancel</Button>
                    <Button color="red" onClick={confirmDelete}>Delete</Button>
                </Group>
            </Modal>

            {assessment.isRubricLocked && (
                <Paper withBorder p="sm" radius="md" bg="var(--mantine-color-gray-0)">
                    <Text size="sm" c="dimmed">
                        Feedback has been recorded against this marking scheme, so its criteria can no
                        longer be added, edited or removed. Reordering is still permitted.
                    </Text>
                </Paper>
            )}

            <Paper withBorder radius="md" style={{ overflow: "hidden" }}>
                {markingItems.length === 0 ? (
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
                                            {!assessment.isRubricLocked && (
                                                <>
                                                    <Tooltip label="Edit">
                                                        <ActionIcon variant="subtle" onClick={() => handleEdit(item)}>
                                                            ✎
                                                        </ActionIcon>
                                                    </Tooltip>
                                                    <Tooltip label="Delete">
                                                        <ActionIcon
                                                            variant="subtle"
                                                            color="red"
                                                            onClick={() => setDeletingItem(item)}
                                                        >
                                                            ✕
                                                        </ActionIcon>
                                                    </Tooltip>
                                                </>
                                            )}
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