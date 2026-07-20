import {useEffect, useState} from "react";
import {useParams} from "react-router";
import {getAssessmentsByModuleId} from "../api/assessmentsApi.js";
import {notifications} from "@mantine/notifications";
import {Badge, Button, Group, Loader, Modal, Paper, Select, Stack, Table, Text, Title} from "@mantine/core";
import { useNavigate } from "react-router";

const AssessmentsPage = () => {
    const navigate = useNavigate();
    const params = useParams();
    const { moduleId } = params;
    const [assessments, setAssessments] = useState([]);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        const loadAssessments = async (moduleId) => {
            setLoading(true);
            try {
                const data = await getAssessmentsByModuleId({moduleId});
                setAssessments(data);
                setLoading(false);
            } catch (error) {
                notifications.show({
                    title: "Error",
                    message: error.message,
                    color: "red"
                })
            }
        };
        loadAssessments(moduleId);

    }, [moduleId]);

    return (
        <Stack gap="lg">
            <div>
                <Title order={1}>Assessments</Title>
                <Text c="dimmed">Access assessment submissions</Text>
            </div>

            <Paper withBorder radius="md" style={{overflow: "hidden"}}>
                {loading ? (
                    <Group justify="center" p="xl">
                        <Loader/>
                    </Group>
                ) : assessments.length === 0 ? (
                    <Text p="md" c="dimmed">No assessments found.</Text>
                ) : (
                    <Table striped highlightOnHover>
                        <Table.Thead>
                            <Table.Tr>
                                <Table.Th>Title</Table.Th>
                                <Table.Th>Due date</Table.Th>
                            </Table.Tr>
                        </Table.Thead>
                        <Table.Tbody>
                            {assessments.map((assessment) => (
                                <Table.Tr
                                    key={assessment.id}
                                >
                                    <Table.Td>{assessment.title}</Table.Td>
                                    <Table.Td>{new Date(assessment.dueDate).toLocaleDateString()}</Table.Td>
                                    <Table.Td>
                                        <Button
                                            onClick={() => navigate(`/modules/${moduleId}/assessments/${assessment.id}`)}
                                        >
                                            Edit
                                        </Button>
                                    </Table.Td>
                                    <Table.Td>
                                        <Button
                                            onClick={() => navigate(`/modules/${moduleId}/assessments/${assessment.id}/students`)}
                                        >
                                            View
                                        </Button>
                                    </Table.Td>
                                </Table.Tr>
                            ))}
                        </Table.Tbody>
                    </Table>
                )}
            </Paper>
        </Stack>
    )
}

export default AssessmentsPage;